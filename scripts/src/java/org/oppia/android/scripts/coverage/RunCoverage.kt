package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.BazelTestTarget
import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoveredLine
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Entry point function for running coverage analysis for a source file.
 *
 * Usage:
 *   bazel run //scripts:run_coverage_for_test_target -- <path_to_root> <relative_path_to_file>
 *
 * Arguments:
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - relative_path_to_file: the relative path to the file to analyse coverage
 * - reportFormat: the format of the coverage report. Defaults to MARKDOWN if not specified.
 *   Available options: MARKDOWN, HTML.
 *
 * Example:
 *     bazel run //scripts:run_coverage -- $(pwd)
 *     utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt format=HTML
 * Example with custom process timeout:
 *     bazel run //scripts:run_coverage -- $(pwd)
 *     utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt processTimeout=15
 *
 */
fun main(vararg args: String) {
  val repoRoot = args[0]
  val filePath = args[1]

  val format = args.find { it.startsWith("format=", ignoreCase = true) }
    ?.substringAfter("=")
    ?.uppercase() ?: "MARKDOWN"

  val reportFormat = when (format) {
    "HTML" -> ReportFormat.HTML
    "MARKDOWN" -> ReportFormat.MARKDOWN
    else -> throw IllegalArgumentException("Unsupported report format: $format")
  }

  val reportOutputPath = getReportOutputPath(repoRoot, filePath, reportFormat)

  check(File(repoRoot, filePath).exists()) {
    "File doesn't exist: $filePath."
  }

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 5

    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )

    RunCoverage(
      repoRoot,
      filePath,
      reportFormat,
      reportOutputPath,
      commandExecutor,
      scriptBgDispatcher
    ).execute()
  }
}

/**
 * Class responsible for executing coverage on a given file.
 *
 * @param repoRoot the root directory of the repository
 * @param filePath the relative path to the file to analyse coverage
 * @param commandExecutor executes the specified command in the specified working directory
 * @param scriptBgDispatcher the [ScriptBackgroundCoroutineDispatcher] to be used for running the coverage command
 */
class RunCoverage(
  private val repoRoot: String,
  private val filePath: String,
  private val reportFormat: ReportFormat,
  private val reportOutputPath: String,
  private val commandExecutor: CommandExecutor,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  private val bazelClient by lazy { BazelClient(File(repoRoot), commandExecutor) }

  private val rootDirectory = File(repoRoot).absoluteFile
  private val testFileExemptionTextProto = "scripts/assets/test_file_exemptions"

  /**
   * Executes coverage analysis for the specified file.
   *
   * Loads test file exemptions and checks if the specified file is exempted. If exempted,
   * prints a message indicating no coverage analysis is performed. Otherwise, initializes
   * a Bazel client, finds potential test file paths, retrieves Bazel targets, and initiates
   * coverage analysis for each test target found.
   *
   * @return a list of lists containing coverage data for each requested test target, if
   *     the file is exempted from having a test file, an empty list is returned
   */
  fun execute() {
    val testFileExemptionList = loadTestFileExemptionsProto(testFileExemptionTextProto)
      .testFileExemptionList
      .filter { it.testFileNotRequired }
      .map { it.exemptedFilePath }

    if (filePath in testFileExemptionList) {
      println("This file is exempted from having a test file; skipping coverage check.")
    } else {
      val testFilePaths = findTestFiles(repoRoot, filePath)
      check(testFilePaths.isNotEmpty()) {
        "No appropriate test file found for $filePath"
      }

      val testTargets = bazelClient.retrieveBazelTargets(testFilePaths)

      val coverageReports = testTargets.map { testTarget ->
        CoverageRunner(rootDirectory, scriptBgDispatcher, commandExecutor)
          .retrieveCoverageDataForTestTarget(testTarget.removeSuffix(".kt"))
      }

      val aggregatedCoverageReport = calculateAggregateCoverageReport(coverageReports)
      val reporter = CoverageReporter(repoRoot, aggregatedCoverageReport, reportFormat)
      val (computedCoverageRatio, reportText) = reporter.generateRichTextReport()

      File(reportOutputPath).apply {
        parentFile?.mkdirs()
        writeText(reportText)
      }

      if (File(reportOutputPath).exists()) {
        println("\nComputed Coverage Ratio is: $computedCoverageRatio")
        println("\nGenerated report at: $reportOutputPath\n")
      }

      println("COVERAGE ANALYSIS COMPLETED.")
    }
  }
}

private fun calculateAggregateCoverageReport(
  coverageReports: List<CoverageReport>
): CoverageReport {
  fun aggregateCoverage(coverages: List<Coverage>): Coverage {
    return coverages.find { it == Coverage.FULL } ?: Coverage.NONE
  }

  val groupedCoverageReports = coverageReports.groupBy {
    Pair(it.filePath, it.fileSha1Hash)
  }

  val (key, reports) = groupedCoverageReports.entries.single()
  val (filePath, fileSha1Hash) = key

  val allBazelTestTargets = reports.flatMap { it.bazelTestTargetsList }
  val allCoveredLines = reports.flatMap { it.coveredLineList }
  val groupedCoveredLines = allCoveredLines.groupBy { it.lineNumber }
  val aggregatedCoveredLines = groupedCoveredLines.map { (lineNumber, coveredLines) ->
    CoveredLine.newBuilder()
      .setLineNumber(lineNumber)
      .setCoverage(aggregateCoverage(coveredLines.map { it.coverage }))
      .build()
  }

  val totalLinesFound = aggregatedCoveredLines.size
  val totalLinesHit = aggregatedCoveredLines.count { it.coverage == Coverage.FULL }


  return CoverageReport.newBuilder()
    .addAllBazelTestTargets(allBazelTestTargets)
    .setFilePath(filePath)
    .setFileSha1Hash(fileSha1Hash)
    .addAllCoveredLine(aggregatedCoveredLines)
    .setLinesFound(totalLinesFound)
    .setLinesHit(totalLinesHit)
    .build()
}

private fun findTestFiles(repoRoot: String, filePath: String): List<String> {
  val possibleTestFilePaths = when {
    filePath.startsWith("scripts/") -> {
      listOf(filePath.replace("/java/", "/javatests/").replace(".kt", "Test.kt"))
    }
    filePath.startsWith("app/") -> {
      listOf(
        filePath.replace("/main/", "/sharedTest/").replace(".kt", "Test.kt"),
        filePath.replace("/main/", "/test/").replace(".kt", "Test.kt"),
        filePath.replace("/main/", "/test/").replace(".kt", "LocalTest.kt")
      )
    }
    else -> {
      listOf(filePath.replace("/main/", "/test/").replace(".kt", "Test.kt"))
    }
  }

  val repoRootFile = File(repoRoot).absoluteFile

  return possibleTestFilePaths
    .map { File(repoRootFile, it) }
    .filter(File::exists)
    .map { it.relativeTo(repoRootFile).path }
}

private fun getReportOutputPath(
  repoRoot: String,
  filePath: String,
  reportFormat: ReportFormat
): String {
  val fileWithoutExtension = filePath.substringBeforeLast(".")
  val defaultFilename = when (reportFormat) {
    ReportFormat.HTML -> "coverage.html"
    ReportFormat.MARKDOWN -> "coverage.md"
  }
  return "$repoRoot/coverage_reports/$fileWithoutExtension/$defaultFilename"
}

private fun loadTestFileExemptionsProto(testFileExemptiontextProto: String): TestFileExemptions {
  return File("$testFileExemptiontextProto.pb").inputStream().use { stream ->
    TestFileExemptions.newBuilder().also { builder ->
      builder.mergeFrom(stream)
    }.build()
  }
}
