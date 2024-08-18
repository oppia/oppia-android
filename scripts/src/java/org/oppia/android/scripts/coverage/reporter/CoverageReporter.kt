package org.oppia.android.scripts.coverage.reporter

import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoverageReportContainer
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File

/** Minimum coverage percentage required. */
const val MIN_THRESHOLD = 70

/* ANSI escape codes for colors. */

/** Green text. */
const val GREEN = "\u001B[32m"
/** Red text. */
const val RED = "\u001B[31m"
/** Default text. */
const val RESET = "\u001B[0m"
/** Bold text. */
const val BOLD = "\u001B[1m"

/**
 * Function for generating coverage report for a list of proto files.
 *
 * Usage:
 *    bazel run //scripts:coverage_runner -- <path_to_root>
 *    <text_file_with_list_of_coverage_data_proto_paths>
 *
 * Arguments:
 * - path_to_root: directory path to the root of the Oppia Android repository.
 * - text_file_with_list_of_coverage_data_proto_paths: the text file that contains
 *     the list of relative path to the proto files containing coverage report data
 *     separated by spaces to analyse coverage.
 *     Sample `coverage_proto_list.txt` content:
 *     ```
 *     coverage_reports/coverage_report1.pb coverage_reports/coverage_report2.pb
 *     ```
 *
 * Example:
 *     bazel run //scripts:coverage_reporter -- $(pwd) coverage_proto_list.txt
 */
fun main(vararg args: String) {
  val repoRoot = args[0]
  val pbTxtFile = File(repoRoot, args[1])

  pbTxtFile.takeIf { it.exists() }?.let {
    val pbList = pbTxtFile.readText()
    val filePathList = pbList.split(" ")
      .filter { it.isNotBlank() }
      .map { it.trim() }

    val coverageResultList = filePathList.mapNotNull { filePath ->
      try {
        println("Filepath: $filePath")
        File(repoRoot, filePath).inputStream().use { stream ->
          CoverageReport.newBuilder().also { builder ->
            builder.mergeFrom(stream)
          }.build()
        }
      } catch (e: Exception) {
        error("Error processing file $filePath: ${e.message}")
      }
    }

    val coverageReportContainer = CoverageReportContainer.newBuilder().apply {
      addAllCoverageReport(coverageResultList)
    }.build()

    val coverageStatus = CoverageReporter(
      repoRoot,
      coverageReportContainer,
      ReportFormat.MARKDOWN
    ).generateRichTextReport()

    when (coverageStatus) {
      CoverageCheck.PASS -> println("Coverage Analysis$BOLD$GREEN PASSED$RESET")
      CoverageCheck.FAIL -> error("Coverage Analysis$BOLD$RED FAILED$RESET")
    }
  } ?: run {
    error("File not found: ${pbTxtFile.absolutePath}")
  }
}

/**
 * Class responsible for generating rich text coverage report.
 *
 * @param repoRoot the root directory of the repository
 * @param coverageReportContainer the list of coverage data proto
 * @param reportFormat the format in which the report will be generated
 * @param mdReportOutputPath optional path to save the final markdown report
 *     default location is $repoRoot/coverage_reports/CoverageReport.md
 */
class CoverageReporter(
  private val repoRoot: String,
  private val coverageReportContainer: CoverageReportContainer,
  private val reportFormat: ReportFormat,
  private val testFileExemptionTextProtoPath: String = "scripts/assets/test_file_exemptions.pb",
  private val mdReportOutputPath: String? = null
) {
  private val testFileExemptionList by lazy {
    loadTestFileExemptionsProto(testFileExemptionTextProtoPath)
      .testFileExemptionList
      .associateBy { it.exemptedFilePath }
  }

  /**
   * Generates a rich text report for the analysed coverage data based on the specified format.
   * It supports Markdown and HTML formats.
   *
   * @return a pair where the first value is the computed coverage ratio represented in [0, 1]
   *     and the second value is the generated report text
   */
  fun generateRichTextReport(): CoverageCheck {
    val coverageStatus = checkCoverageStatus()
    when (reportFormat) {
      ReportFormat.MARKDOWN -> generateMarkdownReport(coverageStatus)
      ReportFormat.HTML -> generateHtmlReport()
      else -> error("Invalid report format to generate report.")
    }
    logCoverageReport()
    return coverageStatus
  }

  private fun generateHtmlReport() {
    println()
    coverageReportContainer.coverageReportList.forEach { report ->
      when {
        report.hasDetails() -> {
          val details = report.details
          val filePath = details.filePath
          val totalLinesFound = details.linesFound
          val totalLinesHit = details.linesHit
          val coveragePercentage = calculateCoveragePercentage(
            totalLinesHit, totalLinesFound
          )
          val formattedCoveragePercentage = "%.2f".format(coveragePercentage)

          val htmlContent = buildString {
            append(
              """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>Coverage Report</title>
                  <style>
                    body {
                      font-family: Arial, sans-serif;
                      font-size: 12px;
                      line-height: 1.6;
                      padding: 20px;
                    }
                    table {
                      width: 100%;
                      border-collapse: collapse;
                      margin-bottom: 20px;
                    }
                    th, td {
                      padding: 8px;
                      text-align: left;
                      white-space: pre-wrap;
                      border-bottom: 1px solid #e3e3e3;
                    }
                    .line-number-col {
                      width: 4%;
                    }
                    .source-code-col {
                      width: 96%;
                    }
                    .covered-line, .not-covered-line, .uncovered-line {
                      white-space: pre-wrap;
                    }
                    .covered-line {
                      background-color: #c8e6c9; /* Light green */
                    }
                    .not-covered-line {
                      background-color: #ffcdd2; /* Light red */
                    }
                    .uncovered-line {
                      background-color: #f7f7f7; /* light gray */
                    }
                    .coverage-summary {
                      margin-bottom: 20px;
                    }
                    h2 {
                      text-align: center;
                    }
                    ul {
                      list-style-type: none;
                      padding: 0;
                      text-align: center;
                    }
                    .summary-box {
                      border: 1px solid #ccc;
                      border-radius: 8px;
                      padding: 10px;
                      margin-bottom: 20px;
                      display: flex;
                      justify-content: space-between;
                      align-items: flex-start;
                    }
                    .summary-left {
                      text-align: left;
                    }
                    .summary-right {
                      text-align: right;
                    }
                    .legend {
                      display: flex;
                      align-items: center;
                    }
                    .legend-item {
                      width: 20px;
                      height: 10px;
                      margin-right: 5px;
                      border-radius: 2px;
                      display: inline-block;
                    }
                    .legend .covered {
                      background-color: #c8e6c9; /* Light green */
                    }
                    .legend .not-covered {
                      margin-left: 4px;
                      background-color: #ffcdd2; /* Light red */
                    }
                    @media screen and (max-width: 768px) {
                      body {
                        padding: 10px;
                      }
                      table {
                        width: auto;
                      }
                    }
                  </style>
                </head>
                <body>
                  <h2>Coverage Report</h2>
                  <div class="summary-box">
                    <div class="summary-left">
                      <strong>Covered File:</strong> $filePath <br>
                      <div class="legend">
                        <div class="legend-item covered"></div>
                        <span>Covered</span>
                        <div class="legend-item not-covered"></div>
                        <span>Uncovered</span>
                      </div>
                    </div>
                    <div class="summary-right">
                      <div><strong>Coverage percentage:</strong> $formattedCoveragePercentage%</div>
                      <div><strong>Line coverage:</strong> $totalLinesHit / $totalLinesFound covered</div>
                    </div>
                  </div>
                  <table>
                    <thead>
                      <tr>
                        <th class="line-number-col">Line No</th>
                        <th class="source-code-col">Source Code</th>
                      </tr>
                    </thead>
                    <tbody>
              """.trimIndent()
            )

            val fileContent = File(repoRoot, filePath).readLines()
            val coverageMap = details.coveredLineList.associateBy { it.lineNumber }

            fileContent.forEachIndexed { index, line ->
              val lineNumber = index + 1
              val lineClass = when (coverageMap[lineNumber]?.coverage) {
                Coverage.FULL -> "covered-line"
                Coverage.NONE -> "not-covered-line"
                else -> "uncovered-line"
              }
              append(
                """
                  <tr>
                    <td class="line-number-row">${lineNumber.toString().padStart(4, ' ')}</td>
                    <td class="$lineClass">$line</td>
                  </tr>
                """.trimIndent()
              )
            }

            append(
              """
                      </tbody>
                    </table>
                  </body>
                  </html>
              """.trimIndent()
            )
          }

          val reportOutputPath = getReportOutputPath(repoRoot, filePath, ReportFormat.HTML)
          File(reportOutputPath).apply {
            parentFile?.mkdirs()
            writeText(htmlContent)
          }

          val fileName = filePath.substringAfterLast("/")
          println("-> HTML report for $fileName generated at: $reportOutputPath \n")
        }
        report.hasFailure() -> {
          val failure = report.failure
          println(
            "-> The coverage analysis for ${failure.bazelTestTarget} failed " +
              "- reason: ${failure.failureMessage} \n"
          )
        }
        report.hasExemption() -> {
          val exemption = report.exemption
          println("-> ${exemption.filePath} - ${exemption.exemptionReason} \n")
        }
        else -> {
          println("Unknown Coverage Report Type")
        }
      }
    }
  }

  private fun generateMarkdownReport(coverageStatus: CoverageCheck) {
    val status = when (coverageStatus) {
      CoverageCheck.PASS -> "**PASS** :white_check_mark:"
      CoverageCheck.FAIL -> "**FAIL** :x:"
    }

    val failureCases = coverageReportContainer.coverageReportList.filter { it.hasFailure() }

    val failureTableRows = failureCases.mapNotNull { report ->
      report.failure?.let { failure ->
        val failurePath = failure.filePath
          ?.takeIf { it.isNotEmpty() }
          ?.let { getFilenameAsDetailsSummary(it) }
          ?: failure.bazelTestTarget
        "| $failurePath | ${failure.failureMessage} | :x: |"
      }
    }.joinToString(separator = "\n")

    var successes = listOf<CoverageReport>()
    var failuresBelowThreshold = listOf<CoverageReport>()
    var exemptedSuccesses = listOf<CoverageReport>()
    var exemptedFailures = listOf<CoverageReport>()

    val detailsCases = coverageReportContainer.coverageReportList.filter { it.hasDetails() }

    detailsCases.forEach { report ->
      val details = report.details
      val totalLinesFound = details.linesFound
      val totalLinesHit = details.linesHit
      val coveragePercentage = calculateCoveragePercentage(
        totalLinesHit, totalLinesFound
      )

      val exemptedFile = testFileExemptionList[details.filePath]
      if (exemptedFile != null) {
        val overridePercentage = exemptedFile.overrideMinCoveragePercentRequired
        if (coveragePercentage >= overridePercentage) {
          exemptedSuccesses = exemptedSuccesses + report
        } else {
          exemptedFailures = exemptedFailures + report
        }
      } else {
        if (coveragePercentage >= MIN_THRESHOLD) {
          successes = successes + report
        } else {
          failuresBelowThreshold = failuresBelowThreshold + report
        }
      }
    }

    val failureBelowThresholdTableRows = generateTableRows(
      reports = failuresBelowThreshold,
      statusSymbol = ":x:"
    )

    val exemptedFailureTableRows = generateTableRows(
      reports = exemptedFailures,
      statusSymbol = ":x:"
    )

    val successTableRows = generateTableRows(
      reports = successes,
      statusSymbol = ":white_check_mark:"
    )

    val exemptedSuccessTableRows = generateTableRows(
      reports = exemptedSuccesses,
      statusSymbol = ":white_check_mark:"
    )

    val testFileExemptedCasesList = coverageReportContainer.coverageReportList
      .filter { it.hasExemption() }
      .map { exemption ->
        val filePath = exemption.exemption.filePath
        val exemptionReason = exemption.exemption.exemptionReason
        "| ${getFilenameAsDetailsSummary(filePath)} | $exemptionReason |"
      }.joinToString(separator = "\n") { "$it" }

    val tableHeader = buildString {
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
    }

    val failureMarkdownTable = buildString {
      if (failureTableRows.isNotEmpty()) {
        append("\n\n")
        append("### Failure Cases\n\n")
        append("| File | Failure Reason | Status |\n")
        append("|------|----------------|--------|\n")
        append(failureTableRows)
      }
    }

    val failureMarkdownEntries = buildString {
      if (failureBelowThresholdTableRows.isNotEmpty() || exemptedFailureTableRows.isNotEmpty()) {
        append("\n\n")
        append("### Failing coverage")
        append("\n\n")
        append(tableHeader)
        if (failureBelowThresholdTableRows.isNotEmpty()) {
          append(failureBelowThresholdTableRows)
          append('\n')
        }
        if (exemptedFailureTableRows.isNotEmpty()) {
          append(exemptedFailureTableRows)
          append(
            "\n\n>**_*_** represents tests with custom overridden " +
              "pass/fail coverage thresholds"
          )
        }
      } else if (exemptedFailureTableRows.isNotEmpty()) {
        append("\n\n")
        append("### Failing coverage")
        append("\n\n")
        append(tableHeader)
        append(exemptedFailureTableRows)
        append(
          "\n\n>**_*_** represents tests with custom overridden " +
            "pass/fail coverage thresholds"
        )
      }
    }

    val successMarkdownEntries = if (successTableRows.isNotEmpty() ||
      exemptedSuccessTableRows.isNotEmpty()
    ) {
      val detailsContent = buildString {
        append("\n### Passing coverage")
        append("\n\n")
        append("<details>\n")
        append("<summary>Files with passing code coverage</summary><br>\n\n")
        if (successTableRows.isNotEmpty()) {
          append(tableHeader)
          if (successTableRows.isNotEmpty()) {
            append(successTableRows)
            append('\n')
          }
          if (exemptedSuccessTableRows.isNotEmpty()) {
            append(exemptedSuccessTableRows)
            append(
              "\n\n>**_*_** represents tests with custom overridden " +
                "pass/fail coverage thresholds"
            )
          }
        } else if (exemptedSuccessTableRows.isNotEmpty()) {
          append(tableHeader)
          append(exemptedSuccessTableRows)
          append(
            "\n\n>**_*_** represents tests with custom overridden " +
              "pass/fail coverage thresholds"
          )
        }
        append("\n</details>")
      }
      detailsContent
    } else ""

    val testFileExemptedSection = buildString {
      val exemptionsReferenceNote = ">Refer [test_file_exemptions.textproto]" +
        "(https://github.com/oppia/oppia-android/blob/develop/" +
        "scripts/assets/test_file_exemptions.textproto) for the comprehensive " +
        "list of file exemptions and their required coverage percentages."
      if (testFileExemptedCasesList.isNotEmpty()) {
        append("\n\n")
        append("### Exempted coverage\n")
        append("<details><summary>Files exempted from coverage</summary><br>")
        append("\n\n")
        append("| File | Exemption Reason |\n")
        append("|------|------------------|\n")
        append(testFileExemptedCasesList)
        append("\n\n")
        append(exemptionsReferenceNote)
        append("</details>")
      }
    }

    val skipCoverageReportText = buildString {
      append("## Coverage Report\n")
      append("### Results\n")
      append("Coverage Analysis: **SKIP** :next_track_button:\n\n")
      append("_This PR did not introduce any changes to Kotlin source or test files._\n\n")
      append("#\n")
      append("> To learn more, visit the [Oppia Android Code Coverage](https://github.com/oppia/oppia-android/wiki/Oppia-Android-Code-Coverage) wiki page")
    }

    val wikiPageLinkNote = buildString {
      val wikiPageReferenceNote = ">To learn more, visit the [Oppia Android Code Coverage]" +
        "(https://github.com/oppia/oppia-android/wiki/Oppia-Android-Code-Coverage) wiki page"
      append("\n\n")
      append("#")
      append("\n")
      append(wikiPageReferenceNote)
    }

    val finalReportText = coverageReportContainer.coverageReportList.takeIf { it.isNotEmpty() }
      ?.let {
        "## Coverage Report\n\n" +
          "### Results\n" +
          "Number of files assessed: ${coverageReportContainer.coverageReportList.size}\n" +
          "Overall Coverage: **${"%.2f".format(calculateOverallCoveragePercentage())}%**\n" +
          "Coverage Analysis: $status\n" +
          "##" +
          failureMarkdownTable +
          failureMarkdownEntries +
          successMarkdownEntries +
          testFileExemptedSection +
          wikiPageLinkNote
      } ?: skipCoverageReportText

    val finalReportOutputPath = mdReportOutputPath
      ?.let { it }
      ?: "$repoRoot/coverage_reports/CoverageReport.md"

    File(finalReportOutputPath).apply {
      parentFile?.mkdirs()
      writeText(finalReportText)
    }

    println("MARKDOWN report for the coverage analysis is generated at: $finalReportOutputPath")
  }

  private fun checkCoverageStatus(): CoverageCheck {
    coverageReportContainer.coverageReportList.forEach { report ->
      if (report.hasFailure()) return CoverageCheck.FAIL

      if (report.hasDetails()) {
        val details = report.details
        val filePath = details.filePath
        val totalLinesFound = details.linesFound
        val totalLinesHit = details.linesHit

        val coveragePercentage = calculateCoveragePercentage(
          totalLinesHit, totalLinesFound
        )

        val threshold = testFileExemptionList[filePath]
          ?.overrideMinCoveragePercentRequired
          ?: MIN_THRESHOLD
        if (coveragePercentage < threshold) return CoverageCheck.FAIL
      }
    }
    return CoverageCheck.PASS
  }

  private fun calculateOverallCoveragePercentage(): Float {
    val reports = coverageReportContainer.coverageReportList
    val totalLinesFound = reports.sumOf { it.details?.linesFound ?: 0 }.toFloat()
    val totalLinesHit = reports.sumOf { it.details?.linesHit ?: 0 }.toFloat()
    return if (totalLinesFound > 0) (totalLinesHit * 100.0f) / totalLinesFound else 0.0f
  }

  private fun logCoverageReport() {
    val failureReports = StringBuilder()

    coverageReportContainer.coverageReportList.forEach { report ->
      when {
        report.hasFailure() -> {
          val failure = report.failure
          failureReports.appendLine(
            """
            |Coverage Report Failure:
            |------------------------
            |Test Target: ${failure.bazelTestTarget}
            |Failure Message: ${failure.failureMessage}
            """.trimMargin().prependIndent("  ")
          )
        }
        report.hasDetails() -> {
          val details = report.details
          val filePath = details.filePath
          val totalLinesFound = details.linesFound
          val totalLinesHit = details.linesHit
          val coveragePercentage = calculateCoveragePercentage(
            totalLinesHit, totalLinesFound
          )

          val formattedCoveragePercentage = "%.2f".format(coveragePercentage)

          val exemption = testFileExemptionList[filePath]
          val minRequiredCoverage = if (exemption != null) {
            exemption.overrideMinCoveragePercentRequired
          } else {
            MIN_THRESHOLD
          }

          if (coveragePercentage < minRequiredCoverage) {
            val exemptionText = exemption?.let { "(exemption)" } ?: ""
            failureReports.appendLine(
              """
              |Covered File: $filePath
              |Coverage percentage: $formattedCoveragePercentage% covered
              |Line coverage: $totalLinesHit / $totalLinesFound lines covered
              |Minimum Required: $minRequiredCoverage% $exemptionText
              |------------------------
              """.trimMargin().prependIndent("  ")
            )
          }
        }
      }
    }

    if (failureReports.isNotEmpty()) {
      println(
        """
        |
        |COVERAGE FAILURE REPORT:
        |-----------------------
        """.trimMargin().prependIndent("  ")
      )
      println(failureReports)
    }
  }

  private fun generateTableRows(
    reports: List<CoverageReport>,
    statusSymbol: String
  ): String {
    return reports
      .mapNotNull { report ->
        val details = report.details
        val filePath = details.filePath
        val totalLinesFound = details.linesFound
        val totalLinesHit = details.linesHit
        val exemptionPercentage = testFileExemptionList[filePath]
          ?.overrideMinCoveragePercentRequired
          ?.let { "$it% _*_" }
          ?: "$MIN_THRESHOLD%"
        val coveragePercentage = calculateCoveragePercentage(
          totalLinesHit, totalLinesFound
        )
        val formattedCoveragePercentage = "%.2f".format(coveragePercentage)

        "| ${getFilenameAsDetailsSummary(filePath)} | $formattedCoveragePercentage% | " +
          "$totalLinesHit / $totalLinesFound | $statusSymbol | $exemptionPercentage |"
      }
      .joinToString(separator = "\n")
  }
}

/** Corresponds to status of the coverage analysis. */
enum class CoverageCheck {
  /** Indicates successful generation of coverage retrieval for a specified file. */
  PASS,
  /** Indicates failure or anomaly during coverage retrieval for a specified file. */
  FAIL
}

/** Represents the different types of formats available to generate code coverage reports. */
enum class ReportFormat {
  /** Indicates that the report should be formatted in .md format. */
  MARKDOWN,
  /** Indicates that the report should be formatted in .html format. */
  HTML,
  /** Indicates to store the collected coverage data as protos. */
  PROTO
}

private fun calculateCoveragePercentage(linesHit: Int, linesFound: Int): Float {
  return linesFound.takeIf { it > 0 }
    ?.let { (linesHit.toFloat() / it * 100).toFloat() }
    ?: 0f
}

private fun getReportOutputPath(
  repoRoot: String,
  filePath: String,
  reportFormat: ReportFormat
): String {
  val fileWithoutExtension = filePath.substringBeforeLast(".")
  val defaultFilename = when (reportFormat) {
    ReportFormat.HTML -> "coverage.html"
    else -> error("Invalid report format to get report output path.")
  }
  return "$repoRoot/coverage_reports/$fileWithoutExtension/$defaultFilename"
}

private fun getFilenameAsDetailsSummary(filePath: String, additionalData: String? = null): String {
  val fileName = filePath.substringAfterLast("/")
  val additionalDataPart = additionalData?.let { " - $it" } ?: ""

  return "<details><summary><b>$fileName</b>$additionalDataPart</summary>$filePath</details>"
}

private fun loadTestFileExemptionsProto(
  testFileExemptionTextProtoPath: String
): TestFileExemptions {
  return File(testFileExemptionTextProtoPath).inputStream().use { stream ->
    TestFileExemptions.newBuilder().apply {
      mergeFrom(stream)
    }.build()
  }
}
