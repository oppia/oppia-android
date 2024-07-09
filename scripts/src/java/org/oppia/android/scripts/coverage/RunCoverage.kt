package org.oppia.android.scripts.coverage

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
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
  println("FilePath = $filePath")

  //TODO: once the file list is received (git client), it need to be filtered to just have
  // .kt files and also not Test.kt files
  val filePaths = listOf(
    "utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt",
    "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt",
    "utility/src/main/java/org/oppia/android/util/math/NumericExpressionEvaluator.kt",
    "utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt",
    "utility/src/main/java/org/oppia/android/util/math/RealExtensions.kt",
//    "utility/src/main/java/org/oppia/android/util/logging/ConsoleLogger.kt",
//    "domain/src/main/java/org/oppia/android/domain/auth/FirebaseAuthWrapperImpl.kt"
  )

  val format = args.find { it.startsWith("format=", ignoreCase = true) }
    ?.substringAfter("=")
    ?.uppercase() ?: "MARKDOWN"

  val reportFormat = when (format) {
    // TODO: (default to HTML) as it would be much simpler for local development
    "HTML" -> ReportFormat.HTML
    "MARKDOWN" -> ReportFormat.MARKDOWN
    else -> throw IllegalArgumentException("Unsupported report format: $format")
  }

  val reportOutputPath = getReportOutputPath(repoRoot, filePath, reportFormat)

  if (!File(repoRoot, filePath).exists()) {
    error("File doesn't exist: $filePath.")
  }

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 10

    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )

    RunCoverage(
      repoRoot,
      filePaths,
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
  private val filePaths: List<String>,
  private val reportFormat: ReportFormat,
  private val reportOutputPath: String,
  private val commandExecutor: CommandExecutor,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  private val bazelClient by lazy { BazelClient(File(repoRoot), commandExecutor) }

  private val rootDirectory = File(repoRoot).absoluteFile
  private val testFileExemptionTextProto = "scripts/assets/test_file_exemptions"
  private val testFileExemptionList by lazy {
    loadTestFileExemptionsProto(testFileExemptionTextProto)
      .testFileExemptionList
      .associateBy { it.exemptedFilePath }
  }

  private val MIN_THRESHOLD = 10 // Example threshold, yet to be decided on a value
  private var coverageCheckState = CoverageCheck.PASS

  /**
   * Executes coverage analysis for the specified file.
   *
   * Loads test file exemptions and checks if the specified file is exempted. If exempted,
   * prints a message indicating no coverage analysis is performed. Otherwise, initializes
   * a Bazel client, finds potential test file paths, retrieves Bazel targets, and initiates
   * coverage analysis for each test target found.
   */
  fun execute() = runBlocking {
    val coverageResults = filePaths.map { filePath ->
      async {
        runCoverageForFile(filePath)
      }
    }.awaitAll()

    if (reportFormat == ReportFormat.MARKDOWN) generateFinalMdReport(coverageResults)

//    println("Coverage Results: $coverageResults")
    println("\nCOVERAGE ANALYSIS COMPLETED.")
  }

  private suspend fun runCoverageForFile(filePath: String): String {
    val exemption = testFileExemptionList[filePath]
    if (exemption != null && exemption.testFileNotRequired) {
      return "The file: $filePath is exempted from having a test file; skipping coverage check.".also {
        println(it)
      }
    } else {
      val testFilePaths = findTestFile(repoRoot, filePath)

      if (testFilePaths.isEmpty()) {
        return "No appropriate test file found for $filePath".also {
          println(it)
        }
      }

      val testTargets = bazelClient.retrieveBazelTargets(testFilePaths)
      val deferredCoverageReports = testTargets.map { testTarget ->
        CoverageRunner(rootDirectory, scriptBgDispatcher, commandExecutor)
          .runWithCoverageAsync(testTarget.removeSuffix(".kt"))
      }

      val coverageReports = deferredCoverageReports.awaitAll()

      // Check if the coverage reports are successfully generated else return failure message.
      // TODO: (yet to decide) if this too needs to be set as coverage state -> FAIL.
      coverageReports.firstOrNull()?.let {
        if (!it.isGenerated) {
          return "Failed to generate coverage report for the file: $filePath.".also {
            println(it)
          }
        }
      }

      val aggregatedCoverageReport = calculateAggregateCoverageReport(coverageReports)
      val reporter = CoverageReporter(repoRoot, aggregatedCoverageReport, reportFormat)
      var (computedCoverageRatio, reportText) = reporter.generateRichTextReport()

      val coverageCheckThreshold = exemption?.overrideMinCoveragePercentRequired ?: MIN_THRESHOLD

//      println("**************************Coverage threshold : $coverageCheckThreshold")
      if (computedCoverageRatio*100 < coverageCheckThreshold) {
        coverageCheckState = CoverageCheck.FAIL
        reportText += "|:x:|"
      } else {
        reportText += "|:white_check_mark:|"
      }
//      println("***************Coverage check state: $coverageCheckState")

      File(reportOutputPath).apply {
        parentFile?.mkdirs()
        writeText(reportText)
      }

      if (File(reportOutputPath).exists()) {
        println("\nGenerated report at: $reportOutputPath\n")
      }

      return reportText
    }
  }

  private enum class CoverageCheck {
    PASS,
    FAIL
  }
}

private fun generateFinalMdReport(coverageResults: List<String>) {
  /*val coverageTableHeader = "| Covered File | Percentage | Line Coverage | Status |\n" +
    "|--------------|------------|---------------|--------|\n"

  println("Coverage table header: $coverageTableHeader")

  *//*val coverageFailures = coverageResults.map { result ->
    result.split("|").fil{it}
  }*//*
  println(coverageResults[0].split("|")[4])
  println("Coverage Failures: $coverageResults")*/

  val coverageTableHeader = "| Covered File | Percentage | Line Coverage | Status |\n" +
    "|--------------|------------|---------------|--------|\n"

  val coverageFailures = coverageResults.filter { result ->
    result.contains("|") && result.split("|")[4].trim() == ":x:"
  }

  val coverageSuccesses = coverageResults.filter { result ->
    result.contains("|") && result.split("|")[4].trim() == ":white_check_mark:"
  }

  val exemptedCases = coverageResults.filterNot { it.contains("|") }

  val coverageFailuresRows = coverageFailures.joinToString(separator = "\n")
  val coverageSuccessesRows = coverageSuccesses.joinToString(separator = "\n")

  val failureMarkdownTable = "## Coverage Report\n\n" +
    "Total covered files: ${coverageResults.size}\n" +
    "Coverage Status: FAIL\n" +
    "Min Coverage Required: 10%\n\n" + // make use of MIN_THRESHOLD
    coverageTableHeader +
    coverageFailuresRows

  val successMarkdownTable = "<details>\n" +
    "<summary>Succeeded Coverages</summary>\n\n" +
    coverageTableHeader +
    coverageSuccessesRows +
    "\n</details>"

  val exemptedCasesList = exemptedCases.joinToString(separator = "\n") { "- $it" }

  val finalReportText = failureMarkdownTable + "\n\n" + successMarkdownTable + "\n\n" + "### Exempted Cases\n" + exemptedCasesList
/*

  println("""
      ## Coverage Report
      $coverageResults
  """.trimIndent())
*/

  println(finalReportText)
}

private fun calculateAggregateCoverageReport(
  coverageReports: List<CoverageReport>
): CoverageReport {
  fun aggregateCoverage(coverages: List<Coverage>): Coverage {
    return if (coverages.contains(Coverage.FULL)) Coverage.FULL
    else Coverage.NONE
  }

  val allCoveredLines = coverageReports.flatMap { it.coveredLineList }

  val groupedCoveredLines = allCoveredLines.groupBy { it.lineNumber }

  val aggregatedCoveredLines = groupedCoveredLines.map { (lineNumber, coveredLines) ->
    CoveredLine.newBuilder()
      .setLineNumber(lineNumber)
      .setCoverage(aggregateCoverage(coveredLines.map { it.coverage }))
      .build()
  }

  val totalLinesFound = aggregatedCoveredLines.size
  val totalLinesHit = aggregatedCoveredLines.count { it.coverage == Coverage.FULL }

  val aggregatedTargetList = coverageReports.joinToString(separator = ", ") { it.bazelTestTarget }

  return CoverageReport.newBuilder()
    .setBazelTestTarget(aggregatedTargetList)
    .setFilePath(coverageReports.first().filePath)
    .setFileSha1Hash(coverageReports.first().fileSha1Hash)
    .addAllCoveredLine(aggregatedCoveredLines)
    .setLinesFound(totalLinesFound)
    .setLinesHit(totalLinesHit)
    .setIsGenerated(true)
    .build()
}

private fun findTestFile(repoRoot: String, filePath: String): List<String> {
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
