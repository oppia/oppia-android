package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.common.BazelClient
import org.oppia.android.scripts.common.CommandExecutor
import org.oppia.android.scripts.common.CommandExecutorImpl
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.toCompressedBase64
import org.oppia.android.scripts.common.ProtoStringEncoder.Companion.mergeFromCompressedBase64
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

private val MIN_THRESHOLD = 10 // yet to be decided on a value

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
 *    Available options: MARKDOWN, HTML.
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
    else -> throw IllegalArgumentException("Unsupported report format: $format")
  }

  for (filePath in filePathList) {
    check(File(repoRoot, filePath).exists()) {
      "File doesn't exist: $filePath."
    }
  }

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
  private val filePathList: List<String>,
  private val reportFormat: ReportFormat,
  private val commandExecutor: CommandExecutor,
  private val scriptBgDispatcher: ScriptBackgroundCoroutineDispatcher
) {
  private val bazelClient by lazy { BazelClient(File(repoRoot), commandExecutor) }
  private var coverageCheckState = CoverageCheck.PASS

  private val rootDirectory = File(repoRoot).absoluteFile
  private val testFileExemptionTextProto = "scripts/assets/test_file_exemptions"
  private val testFileExemptionList by lazy {
    loadTestFileExemptionsProto(testFileExemptionTextProto)
      .testFileExemptionList
      .associateBy { it.exemptedFilePath }
  }

  var coverageReportContainer2 = CoverageReportContainer.newBuilder()

  /**
   * Executes coverage analysis for the specified file.
   *
   * Loads test file exemptions and checks if the specified file is exempted. If exempted,
   * prints a message indicating no coverage analysis is performed. Otherwise, initializes
   * a Bazel client, finds potential test file paths, retrieves Bazel targets, and initiates
   * coverage analysis for each test target found.
   */
  fun execute() {
    val coverageResults = filePathList.map { filePath ->
        runCoverageForFile(filePath)
    }

    println("Coverage Results: $coverageResults")

    /*At this point we will/should be having a container of coverage reports
    * have generate text report() here in one unified space
    * generate -> val reporter =
        CoverageReporter(repoRoot, coverageReportContainer, reportFormat)
      var (computedCoverageRatio, reportText) = reporter.generateRichTextReport()
      *
      * generateRichTextReport() ->
      *   HTML -> container: for each -> generate html report
      *   MD   -> container: combined -> each ; add md report
      *
      *  This could be standard for local -> put in a base cmd -> run cov -> collects protos ->
      *  gets one proto container -> saves proto to path (both html and md) -> generates report
      *  for HTML -> generates f1.html, f2.html, f3.html (its own path)
      *  for MD   -> generates one common cov.md report at coverage_reports/cov.md
      * (this md report is basically unnecessary for local dev unless for debugging,
      *  but to keep things consistent in the workflow we us this approach)
    * */

    //
    if (reportFormat == ReportFormat.MARKDOWN) {
      val cov = coverageReportContainer2.build()
      println("Type: ${coverageReportContainer2::class}")
      println("Type of cov: ${cov::class}")

      val covDirectoryPath = "${repoRoot}/coverage_reports/"
      val covFilePath = "${repoRoot}/coverage_reports/coverage_report.proto64"

      val covDirectory = File(covDirectoryPath)
      if (!covDirectory.exists()) {
        covDirectory.mkdirs()
      }

      val covFile = File(covFilePath)
      if (!covFile.exists()) {
        covFile.createNewFile()
      }

      println("File: ${covFile.absolutePath}")
      println("Exists? -> ${covFile.exists()}")

      val serialized = cov.toCompressedBase64()
      println("Serialized: $serialized")

      val deserialize = CoverageReportContainer.getDefaultInstance().mergeFromCompressedBase64(serialized)
      println("Deserialized: $deserialize")

      /*File("/coverage_reports/coverage_report.proto64").printWriter().use { writer ->
        writer.println(serialized)
      }*/

      covFile.printWriter().use { writer ->
        writer.println(serialized)
      }
    }

//    if (reportFormat == ReportFormat.MARKDOWN) generateFinalMdReport(coverageResults)

    if (coverageCheckState == CoverageCheck.FAIL) {
      error(
        "\nCoverage Analysis Failed as minimum coverage threshold not met!" +
          "\nMinimum Coverage Threshold = $MIN_THRESHOLD%"
      )
    } else {
      println("\nCoverage Analysis Completed Succesffully!")
    }
  }

  private fun runCoverageForFile(filePath: String): CoverageReport {
    val exemption = testFileExemptionList[filePath]
    if (exemption != null && exemption.testFileNotRequired) {
      // add as cov rep for cov con
      /*return "The file: $filePath is exempted from having a test file; skipping coverage check."
        .also {
          println(it)
        }*/

      return CoverageReport.newBuilder()
        .setExemption(
          CoverageExemption.newBuilder()
            .setFilePath(filePath)
            .build()
        ).build()
    } else {
      val testFilePaths = findTestFiles(repoRoot, filePath)
      if (testFilePaths.isEmpty()) {
        // add as cov rep for cov con
        /*return "No appropriate test file found for $filePath".also {
          println(it)
        }*/
        return CoverageReport.newBuilder()
          .setFailure(
            CoverageFailure.newBuilder()
              .setFilePath(filePath)
              .setFailureMessage("No appropriate test file found for $filePath")
              .build()
          ).build()
      }

      val testTargets = bazelClient.retrieveBazelTargets(testFilePaths)

      val coverageReports = testTargets.map { testTarget ->
        CoverageRunner(rootDirectory, scriptBgDispatcher, commandExecutor)
          .retrieveCoverageDataForTestTarget(testTarget.removeSuffix(".kt"))
      }
      // Check if the coverage reports are successfully generated else return failure message.
//      coverageReports.firstOrNull()?.let {
        /*if (!it.isGenerated) {
          // add the generated (failed) cov rep straight into cov con
          return "Failed to generate coverage report for the file: $filePath.".also {
            println(it)
          }
        }*/
//      }

      coverageReports.forEach { report ->
        if (report.hasFailure()) {
          // (may be) add file path here
          return CoverageReport.newBuilder()
            .setFailure(report.failure)
            .build()
        }
      }

      val aggregatedCoverageReport = calculateAggregateCoverageReport(coverageReports)
      println("Aggregated Coverage Report: $aggregatedCoverageReport")

      // may be just combine coverage reports into containers here
      /* container.add(aggregatedCoverageReport)
      *  we will have a container of coverage reports or call a combine coverage report to keep things clean
      *  and keep this just for a single file run
      *  combineCoverageReport()
      * */


      val reportText = generateAggregatedCoverageReport(aggregatedCoverageReport)
      println("Report Text: $reportText")

//      return reportText
      return aggregatedCoverageReport
    }
  }

  // move it to coverage reporter
  /*private fun generateFinalMdReport(coverageResults: List<String>) {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"

    val coverageTableHeader = "| Covered File | Percentage | Line Coverage | Status |\n" +
      "|--------------|------------|---------------|--------|\n"

    val coverageFailures = coverageResults.filter { result ->
      result.contains("|") && result.split("|")[4].trim() == ":x:"
    }

    val coverageSuccesses = coverageResults.filter { result ->
      result.contains("|") && result.split("|")[4].trim() == ":white_check_mark:"
    }

    val anomalyCases = coverageResults
      .filterNot { it.contains("|") }
      .map {
        it.replace(Regex("""([\w/]+\.kt)""")) { matchResult ->
          "[${matchResult.value.substringAfterLast("/").trim()}]" +
            "($oppiaDevelopGitHubLink/${matchResult.value})"
        }
      }

    println("Anomalycases: $anomalyCases")
    val coverageFailuresRows = coverageFailures.joinToString(separator = "\n")
    val coverageSuccessesRows = coverageSuccesses.joinToString(separator = "\n")

    val failureMarkdownTable = coverageFailuresRows.takeIf { it.isNotEmpty() }?.let {
      "### Failed Coverages\n" +
        "Min Coverage Required: $MIN_THRESHOLD%\n\n" +
        coverageTableHeader +
        it
    } ?: ""

    val successMarkdownTable = coverageSuccessesRows.takeIf { it.isNotEmpty() }?.let {
      "<details>\n" +
        "<summary>Succeeded Coverages</summary><br>\n\n" +
        coverageTableHeader +
        it +
        "\n</details>"
    } ?: ""

    val anomalyCasesList = anomalyCases.joinToString(separator = "\n") { "- $it" }
    val anomalySection = anomalyCases.takeIf { it.isNotEmpty() }?.let {
      "\n\n### Anomaly Cases\n$anomalyCasesList"
    } ?: ""

    val finalReportText = "## Coverage Report\n\n" +
      "- No of files assessed: ${coverageResults.size}\n" +
      "- Coverage Status: **$coverageCheckState**\n" +
      failureMarkdownTable +
      "\n\n" + successMarkdownTable +
      anomalySection

    val finalReportOutputPath = "$repoRoot/coverage_reports/CoverageReport.md"
    File(finalReportOutputPath).apply {
      parentFile?.mkdirs()
      writeText(finalReportText)
    }
  }*/

  private fun generateAggregatedCoverageReport(aggregatedCoverageReport: CoverageReport): String {
    var pubReportText = ""
    if (reportFormat == ReportFormat.MARKDOWN) {

//      val coverageReportContainer = CoverageReportContainer.newBuilder()

      val coverageReportContainer = CoverageReportContainer.newBuilder()
        .addCoverageReport(aggregatedCoverageReport)
        .build()
      println("Coverage Report Container: $coverageReportContainer")

      println("Type Coverage Reporter Container: ${coverageReportContainer::class}")

      coverageReportContainer2
        .addCoverageReport(aggregatedCoverageReport)
//        .build()

      println("************************")
      println("Coverage Report Container 2: $coverageReportContainer2")

      val reporter =
        CoverageReporter(repoRoot, aggregatedCoverageReport, coverageReportContainer, reportFormat)
      var (computedCoverageRatio, reportText) = reporter.generateRichTextReport()

      val coverageCheckThreshold = testFileExemptionList[aggregatedCoverageReport.details.filePath]
        ?.overrideMinCoveragePercentRequired
        ?: MIN_THRESHOLD

      if (computedCoverageRatio * 100 < coverageCheckThreshold) {
        coverageCheckState = CoverageCheck.FAIL
      }

      reportText += if (reportFormat == ReportFormat.MARKDOWN) {
        computedCoverageRatio.takeIf { it * 100 < coverageCheckThreshold }
          ?.let { "|:x:|" } ?: "|:white_check_mark:|"
      } else ""

      val reportOutputPath = getReportOutputPath(
        repoRoot, aggregatedCoverageReport.details.filePath, reportFormat
      )
      File(reportOutputPath).apply {
        parentFile?.mkdirs()
        writeText(reportText)
      }

      if (File(reportOutputPath).exists()) {
        println("\nGenerated report at: $reportOutputPath\n")
      }

      pubReportText = reportText

      return pubReportText
//      return reportText
    }
    if (reportFormat == ReportFormat.HTML) {
      val coverageReportContainer = CoverageReportContainer.newBuilder()
        .addCoverageReport(aggregatedCoverageReport)
        .build()
      println("Coverage Report Container: $coverageReportContainer")

      val reporter =
        CoverageReporter(repoRoot, aggregatedCoverageReport, coverageReportContainer, reportFormat)
      var (computedCoverageRatio, reportText) = reporter.generateRichTextReport()

      val coverageCheckThreshold = testFileExemptionList[aggregatedCoverageReport.details.filePath]
        ?.overrideMinCoveragePercentRequired
        ?: MIN_THRESHOLD

      if (computedCoverageRatio * 100 < coverageCheckThreshold) {
        coverageCheckState = CoverageCheck.FAIL
      }

      reportText += if (reportFormat == ReportFormat.MARKDOWN) {
        computedCoverageRatio.takeIf { it * 100 < coverageCheckThreshold }
          ?.let { "|:x:|" } ?: "|:white_check_mark:|"
      } else ""

      val reportOutputPath = getReportOutputPath(
        repoRoot, aggregatedCoverageReport.details.filePath, reportFormat
      )
      File(reportOutputPath).apply {
        parentFile?.mkdirs()
        writeText(reportText)
      }

      if (File(reportOutputPath).exists()) {
        println("\nGenerated report at: $reportOutputPath\n")
      }

      pubReportText = reportText

      return pubReportText
//      return reportText
    }
    // temp
    return pubReportText
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

    val singleCoverageReport = groupedCoverageReports.entries.single()
    val (key, reports) = singleCoverageReport
    val (filePath, fileSha1Hash) = key

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
    val aggregatedTargetList = reports.joinToString(separator = ", ") { it.details.bazelTestTarget }

    val coverageDetails = CoverageDetails.newBuilder()
      .setBazelTestTarget(aggregatedTargetList)
      .setFilePath(filePath)
      .setFileSha1Hash(fileSha1Hash)
      .addAllCoveredLine(aggregatedCoveredLines)
      .setLinesFound(totalLinesFound)
      .setLinesHit(totalLinesHit)
      .build()

    return CoverageReport.newBuilder()
      .setDetails(coverageDetails)
      .build()

    /*return CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setBazelTestTarget(aggregatedTargetList)
          .setFilePath(filePath)
          .setFileSha1Hash(fileSha1Hash)
          .addAllCoveredLine(aggregatedCoveredLines)
          .setLinesFound(totalLinesFound)
          .setLinesHit(totalLinesHit)
//          .setIsGenerated(true)
          .build()
    ).build()*/
  }

  /** Corresponds to status of the coverage analysis. */
  private enum class CoverageCheck {
    /** Indicates successful generation of coverage retrieval for a specified file. */
    PASS,
    /** Indicates failure or anomaly during coverage retrieval for a specified file. */
    FAIL
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
