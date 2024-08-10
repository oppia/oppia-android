package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageDetails
import org.oppia.android.scripts.proto.CoverageExemption
import org.oppia.android.scripts.proto.CoverageFailure
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoverageReportContainer
import org.oppia.android.scripts.proto.CoveredLine
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File
import java.util.concurrent.TimeUnit

/** ANSI escape codes for colors. */
/** Green text. */
const val GREEN = "\u001B[32m"
/** Red text. */
const val RED = "\u001B[31m"
/** Default text. */
const val RESET = "\u001B[0m"
/** Bold text. */
const val BOLD = "\u001B[1m"

/**
 * Entry point function for running coverage analysis for a source file.
 *
 * Usage:
 *    bazel run //scripts:run_coverage_for_test_target -- <path_to_root> <list_of_relative_path_to_files>
 *
 * Arguments:
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - list_of_relative_path_to_files: the list of relative path to the files to analyse coverage
 * - reportFormat: the format of the coverage report. Defaults to HTML if not specified.
 *    Available options: MARKDOWN, HTML, PROTO.
 * - processTimeout: The amount of time that should be waited before considering a process as 'hung',
 *    in minutes.
 * - path_to_output_file: path to the file in which the collected coverage reports will be printed.
 *
 * Example:
 *    bazel run //scripts:run_coverage -- $(pwd)
 *    utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt --format=HTML
 *
 * Example with list of files:
 *    bazel run //scripts:run_coverage -- $(pwd)
 *    utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt
 *    utility/src/main/java/org/oppia/android/util/math/MathTokenizer.kt --format=MARKDOWN
 *
 * Example with custom process timeout:
 *    bazel run //scripts:run_coverage -- $(pwd)
 *    utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt --processTimeout=15
 *
 * Example with output path to save the collected coverage proto:
 *    bazel run //scripts:run_coverage -- $(pwd)
 *    utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt --format=PROTO
 *    --protoOutputPath=/tmp/coverage_report.pb
 */
fun main(vararg args: String) {
  val repoRoot = args[0]

  val filePathList = args.drop(1)
    .takeWhile { !it.startsWith("--") }
    .map { it.trim(',', '[', ']') }
    .flatMap { filePath ->
      when {
        filePath.endsWith("Test.kt") -> {
          findSourceFile(repoRoot, filePath)
        }
        filePath.endsWith(".kt") -> listOf(filePath)
        else -> emptyList()
      }
    }

  println("Running coverage analysis for the files: $filePathList")

  val format = args.find { it.startsWith("--format=", ignoreCase = true) }
    ?.substringAfter("=")
    ?.uppercase() ?: "HTML"

  val reportFormat = when (format) {
    "HTML" -> ReportFormat.HTML
    "MARKDOWN", "MD" -> ReportFormat.MARKDOWN
    "PROTO" -> ReportFormat.PROTO
    else -> throw IllegalArgumentException("Unsupported report format: $format")
  }
  println("Using format: $reportFormat")

  /*val protoOutputPath = args.find { it.startsWith("--protoOutputPath") }
    ?.substringAfter("=")*/

  for (filePath in filePathList) {
    check(File(repoRoot, filePath).exists()) {
      "File doesn't exist: $filePath."
    }
  }

//  val testFileExemptionTextProto = "scripts/assets/test_file_exemptions"

  ScriptBackgroundCoroutineDispatcher().use { scriptBgDispatcher ->
    val processTimeout: Long = args.find { it.startsWith("--processTimeout=") }
      ?.substringAfter("=")
      ?.toLongOrNull() ?: 5

    val commandExecutor: CommandExecutor = CommandExecutorImpl(
      scriptBgDispatcher, processTimeout = processTimeout, processTimeoutUnit = TimeUnit.MINUTES
    )

    RunCoverage(
      repoRoot,
      filePathList,
      reportFormat,
      commandExecutor,
      scriptBgDispatcher,
//      testFileExemptionTextProto,
//      protoOutputPath
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
  private val filePathList: List<String>,
  private val reportFormat: ReportFormat,
  private val commandExecutor: CommandExecutor,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher,
  private val testFileExemptionTextProtoPath: String = "scripts/assets/test_file_exemptions.pb",
//  private val protoOutputPath: String? = null
) {
  private val bazelClient by lazy { BazelClient(File(repoRoot), commandExecutor) }

  private val rootDirectory = File(repoRoot).absoluteFile

  private val testFileExemptionList by lazy {
    loadTestFileExemptionsProto(testFileExemptionTextProtoPath)
      .testFileExemptionList
      .associateBy { it.exemptedFilePath }
  }

/*<<<<<<< HEAD
  private val testFileExemptionList by lazy {
    loadTestFileExemptionsProto(testFileExemptionTextProto)
      .testFileExemptionList
      .associateBy { it.exemptedFilePath }
  }
=======
>>>>>>> d0d1839d0dbb90d35fcaaf449ab7f168ce0f4640*/

//  val testFileExemptions = loadTestFileExemptionsProto(testFileExemptionTextProtoPath)
  /*val filesNotNeedingTests =
    testFileExemptions
      .testFileExemptionList.filter { it.testFileNotRequired }
      .map { it.exemptedFilePath }
      .associateBy { it.exemptedFilePath }
  val filesIncompatibleWithCodeCoverage =
    testFileExemptions
      .testFileExemptionList
      .filter { it.sourceFileIsIncompatibleWithCodeCoverage }
      .map { it.exemptedFilePath }
      .associateBy { it.exemptedFilePath }*/

  /**
   * Executes coverage analysis for the specified file.
   *
   * Loads test file exemptions and checks if the specified file is exempted. If exempted,
   * prints a message indicating no coverage analysis is performed. Otherwise, initializes
   * a Bazel client, finds potential test file paths, retrieves Bazel targets, and initiates
   * coverage analysis for each test target found.
   */
  fun execute() {
//<<<<<<< HEAD
    val coverageResults = filePathList.map { filePath ->
      runCoverageForFile(filePath)
    }

    val coverageReportContainer = combineCoverageReports(coverageResults)

    if (reportFormat == ReportFormat.PROTO) {
      /*protoOutputPath?.let { path ->
        val file = File(path)
        file.parentFile?.mkdirs()
        file.outputStream().use { stream ->
          coverageReportContainer.writeTo(stream)
        }
      }*/

      // Exit without generating text reports if the format is PROTO
      return
    }

    val reporter = CoverageReporter(
      repoRoot,
      coverageReportContainer,
      reportFormat,
      testFileExemptionTextProtoPath
    )
    val coverageStatus = reporter.generateRichTextReport()

    when (coverageStatus) {
      CoverageCheck.PASS -> println("Coverage Analysis$BOLD$GREEN PASSED$RESET")
      CoverageCheck.FAIL -> error("Coverage Analysis$BOLD$RED FAILED$RESET")
    }
  }

  private fun runCoverageForFile(filePath: String): CoverageReport {

    val exemption = testFileExemptionList[filePath]
    return when {
      exemption?.testFileNotRequired == true -> {
        CoverageReport.newBuilder()
          .setExemption(
            CoverageExemption.newBuilder()
              .setFilePath(filePath)
              .setExemptionReason("This file is exempted from having a test file; " +
                "skipping coverage check.")
              .build()
          ).build()
      }
      exemption?.sourceFileIsIncompatibleWithCodeCoverage == true -> {
        CoverageReport.newBuilder()
          .setExemption(
            CoverageExemption.newBuilder()
              .setFilePath(filePath)
              .setExemptionReason("This file is incompatible with code coverage tooling; " +
                "skipping coverage check.")
              .build()
          ).build()
      }
      else -> {
        val testFilePaths = findTestFiles(repoRoot, filePath)
        when {
          testFilePaths.isEmpty() -> {
            return CoverageReport.newBuilder()
              .setFailure(
                CoverageFailure.newBuilder()
                  .setFilePath(filePath)
                  .setFailureMessage("No appropriate test file found for $filePath.")
                  .build()
              ).build()
          }
          else -> {
            val testTargets = bazelClient.retrieveBazelTargets(testFilePaths)
            when {
              testTargets.isEmpty() -> {
                CoverageReport.newBuilder()
                  .setFailure(
                    CoverageFailure.newBuilder()
                      .setFilePath(filePath)
                      .setFailureMessage(
                        "Missing test declaration(s) for existing test file(s): $testFilePaths."
                      )
                      .build()
                  ).build()
              }
              else -> {
                val coverageReports = testTargets.flatMap { testTarget ->
                  CoverageRunner(rootDirectory, scriptBgDispatcher, commandExecutor)
                    .retrieveCoverageDataForTestTarget(testTarget.removeSuffix(".kt"))
                }

                coverageReports.find { it.hasFailure() }?.let { failingReport ->
                  CoverageReport.newBuilder()
                    .setFailure(failingReport.failure)
                    .build()
                } ?: calculateAggregateCoverageReport(coverageReports)
              }
            }
          }
        }
      }

      /*val exemption = testFileExemptionList[filePath]
    if (exemption != null && exemption.testFileNotRequired) {
      return CoverageReport.newBuilder()
        .setExemption(
          CoverageExemption.newBuilder()
            .setFilePath(filePath)
            .build()
        ).build()
    } else {
      val testFilePaths = findTestFiles(repoRoot, filePath)
      if (testFilePaths.isEmpty()) {
        return CoverageReport.newBuilder()
          .setFailure(
            CoverageFailure.newBuilder()
              .setFilePath(filePath)
              .setFailureMessage("No appropriate test file found for $filePath.")
              .build()
          ).build()
=======
    val testFileExemptions = loadTestFileExemptionsProto(testFileExemptionTextProtoPath)
    val filesNotNeedingTests =
      testFileExemptions
        .testFileExemptionList.filter { it.testFileNotRequired }.map { it.exemptedFilePath }
    val filesIncompatibleWithCodeCoverage =
      testFileExemptions
        .testFileExemptionList
        .filter { it.sourceFileIsIncompatibleWithCodeCoverage }
        .map { it.exemptedFilePath }

    if (filePath in filesNotNeedingTests || filePath in filesIncompatibleWithCodeCoverage) {
      if (filePath in filesIncompatibleWithCodeCoverage) {
        println("This file is incompatible with code coverage tooling; skipping coverage check.")
      } else println("This file is exempted from having a test file; skipping coverage check.")
    } else {
      val testFilePaths = findTestFiles(repoRoot, filePath)
      check(testFilePaths.isNotEmpty()) {
        "No appropriate test file found for $filePath."
>>>>>>> d0d1839d0dbb90d35fcaaf449ab7f168ce0f4640
      }

      val testTargets = bazelClient.retrieveBazelTargets(testFilePaths)
      if (testTargets.isEmpty()) {
        return CoverageReport.newBuilder()
          .setFailure(
            CoverageFailure.newBuilder()
              .setFilePath(filePath)
              .setFailureMessage(
                "Missing test declaration(s) for existing test file(s): $testFilePaths."
              )
              .build()
          ).build()
      }

      val coverageReports = testTargets.flatMap { testTarget ->
        CoverageRunner(rootDirectory, scriptBgDispatcher, commandExecutor)
          .retrieveCoverageDataForTestTarget(testTarget.removeSuffix(".kt"))
      }

      coverageReports.forEach { report ->
        if (report.hasFailure()) {
          return CoverageReport.newBuilder()
            .setFailure(report.failure)
            .build()
        }
      }

      val aggregatedCoverageReport = calculateAggregateCoverageReport(coverageReports)
      return aggregatedCoverageReport
    }*/
    }
  }

  private fun combineCoverageReports(
    coverageResultList: List<CoverageReport>
  ): CoverageReportContainer {
    return CoverageReportContainer.newBuilder().apply {
      addAllCoverageReport(coverageResultList)
    }.build()
  }

  private fun calculateAggregateCoverageReport(
    coverageReports: List<CoverageReport>
  ): CoverageReport {
    fun aggregateCoverage(coverages: List<Coverage>): Coverage {
      return coverages.find { it == Coverage.FULL } ?: Coverage.NONE
    }

    val groupedCoverageReports = coverageReports.groupBy {
      Pair(it.details.filePath, it.details.fileSha1Hash)
    }

    val (key, reports) = groupedCoverageReports.entries.single()
    val (filePath, fileSha1Hash) = key

    val allBazelTestTargets = reports.flatMap { it.details.bazelTestTargetsList }
    val allCoveredLines = reports.flatMap { it.details.coveredLineList }
    val groupedCoveredLines = allCoveredLines.groupBy { it.lineNumber }
    val aggregatedCoveredLines = groupedCoveredLines.map { (lineNumber, coveredLines) ->
      CoveredLine.newBuilder()
        .setLineNumber(lineNumber)
        .setCoverage(aggregateCoverage(coveredLines.map { it.coverage }))
        .build()
    }

    val totalLinesFound = aggregatedCoveredLines.size
    val totalLinesHit = aggregatedCoveredLines.count { it.coverage == Coverage.FULL }

    val coverageDetails = CoverageDetails.newBuilder()
      .addAllBazelTestTargets(allBazelTestTargets)
      .setFilePath(filePath)
      .setFileSha1Hash(fileSha1Hash)
      .addAllCoveredLine(aggregatedCoveredLines)
      .setLinesFound(totalLinesFound)
      .setLinesHit(totalLinesHit)
      .build()

    return CoverageReport.newBuilder()
      .setDetails(coverageDetails)
      .build()
  }
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

private fun findSourceFile(repoRoot: String, filePath: String): List<String> {
  val possibleSourceFilePaths = when {
    filePath.startsWith("scripts/") -> {
      listOf(filePath.replace("/javatests/", "/java/").replace("Test.kt", ".kt"))
    }
    filePath.startsWith("app/") -> {
      when {
        filePath.contains("/sharedTest/") -> {
          listOf(filePath.replace("/sharedTest/", "/main/").replace("Test.kt", ".kt"))
        }
        filePath.contains("/test/") -> {
          listOf(
            filePath.replace("/test/", "/main/").replace("Test.kt", ".kt"),
            filePath.replace("/test/", "/main/").replace("LocalTest.kt", ".kt")
          )
        }
        else -> {
          emptyList()
        }
      }
    }
    else -> {
      listOf(filePath.replace("/test/", "/main/").replace("Test.kt", ".kt"))
    }
  }

  val repoRootFile = File(repoRoot).absoluteFile

  return possibleSourceFilePaths
    .map { File(repoRootFile, it) }
    .filter(File::exists)
    .map { it.relativeTo(repoRootFile).path }
}

private fun loadTestFileExemptionsProto(testFileExemptionProtoPath: String): TestFileExemptions {
  return File(testFileExemptionProtoPath).inputStream().use { stream ->
    TestFileExemptions.newBuilder().also { builder ->
      builder.mergeFrom(stream)
    }.build()
  }
}
