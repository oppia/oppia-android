package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoverageReportContainer
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File

/** Minimum coverage percentage required. */
const val MIN_THRESHOLD = 10 // to be decided

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
  private val mdReportOutputPath: String? = null
) {
  private val testFileExemptionTextProto = "scripts/assets/test_file_exemptions"
  private val testFileExemptionList by lazy {
    loadTestFileExemptionsProto(testFileExemptionTextProto)
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
          val formattedCoveragePercentage = "%2.2f".format(coveragePercentage)

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
          println("-> The file ${exemption.filePath} is exempted from coverage analysis \n")
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
          ?.let { getFilenameAsLink(it) }
          ?: failure.bazelTestTarget
        "| $failurePath | ${failure.failureMessage} |"
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
        "${getFilenameAsLink(filePath)}"
      }.joinToString(separator = "\n") { "- $it" }

    val tableHeader = buildString {
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
    }

    val failureMarkdownTable = buildString {
      if (failureTableRows.isNotEmpty()) {
        append("\n\n")
        append("### Failure Cases\n")
        append("| File | Failure Reason |\n")
        append("|------|----------------|\n")
        append(failureTableRows)
      }
    }

    val failureMarkdownEntries = buildString {
      if (failureBelowThresholdTableRows.isNotEmpty() || exemptedFailureTableRows.isNotEmpty()) {
        append("\n\n")
        append(tableHeader)
        append(failureBelowThresholdTableRows)
        if (exemptedFailureTableRows.isNotEmpty()) {
          append("\n|Exempted :small_red_triangle_down:|\n")
          append(exemptedFailureTableRows)
        }
      } else if (exemptedFailureTableRows.isNotEmpty()) {
        append("\n\n")
        append(tableHeader)
        append("\n|Exempted :small_red_triangle_down:|\n")
        append(exemptedFailureTableRows)
      }
    }

    val successMarkdownEntries = if (successTableRows.isNotEmpty() ||
      exemptedSuccessTableRows.isNotEmpty()
    ) {
      val detailsContent = buildString {
        append("\n\n")
        append("<details>\n")
        append("<summary>Succeeded Coverages</summary><br>\n\n")
        if (successTableRows.isNotEmpty()) {
          append(tableHeader)
          append(successTableRows)
          if (exemptedSuccessTableRows.isNotEmpty()) {
            append("\n|Exempted :small_red_triangle_down:|\n")
            append(exemptedSuccessTableRows)
          }
        } else if (exemptedSuccessTableRows.isNotEmpty()) {
          append(tableHeader)
          append("\n|Exempted :small_red_triangle_down:|\n")
          append(exemptedSuccessTableRows)
        }
        append("\n</details>")
      }
      detailsContent
    } else ""

    val testFileExemptedSection = buildString {
      if (testFileExemptedCasesList.isNotEmpty()) {
        append("\n\n")
        append("### Test File Exempted Cases\n")
        append(testFileExemptedCasesList)
      }
    }

    val finalReportText = "## Coverage Report\n\n" +
      "- Number of files assessed: ${coverageReportContainer.coverageReportList.size}\n" +
      "- Coverage Analysis: $status" +
      failureMarkdownTable +
      failureMarkdownEntries +
      successMarkdownEntries +
      testFileExemptedSection

    val finalReportOutputPath = mdReportOutputPath?.let {
      it
    } ?: "$repoRoot/coverage_reports/CoverageReport.md"

    File(finalReportOutputPath).apply {
      parentFile?.mkdirs()
      writeText(finalReportText)
    }
  }

  private fun checkCoverageStatus(): CoverageCheck {
    coverageReportContainer.coverageReportList.forEach { report ->
      if (report.hasFailure()) { return CoverageCheck.FAIL }

      if (report.hasDetails()) {
        val details = report.details
        val filePath = details.filePath
        val totalLinesFound = details.linesFound
        val totalLinesHit = details.linesHit

        val coveragePercentage = calculateCoveragePercentage(
          totalLinesHit, totalLinesFound
        )

        val exemption = testFileExemptionList[filePath]
        if (coveragePercentage < MIN_THRESHOLD) {
          if (exemption != null) {
            val ovveriddenMinCoverage = exemption.overrideMinCoveragePercentRequired
            if (coveragePercentage < ovveriddenMinCoverage) {
              return CoverageCheck.FAIL
            }
          } else {
            return CoverageCheck.FAIL
          }
        }
      }
    }
    return CoverageCheck.PASS
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

          val formattedCoveragePercentage = "%2.2f".format(coveragePercentage)

          val exemption = testFileExemptionList[filePath]
          val minRequiredCoverage = if (exemption != null) {
            exemption.overrideMinCoveragePercentRequired
          } else {
            MIN_THRESHOLD
          }

          if (coveragePercentage < minRequiredCoverage) {
            failureReports.appendLine(
              """
              |Covered File: $filePath
              |Coverage percentage: $formattedCoveragePercentage% covered
              |Line coverage: $totalLinesHit / $totalLinesFound lines covered
              |Minimum Required: $minRequiredCoverage% ${if (exemption != null) "(exemption)" else ""}
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
          ?: MIN_THRESHOLD
        val coveragePercentage = calculateCoveragePercentage(
          totalLinesHit, totalLinesFound
        )
        val formattedCoveragePercentage = "%2.2f".format(coveragePercentage)

        "| ${getFilenameAsLink(filePath)} | $formattedCoveragePercentage% | " +
          "$totalLinesHit / $totalLinesFound | $statusSymbol | $exemptionPercentage% |"
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
  HTML
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
    ReportFormat.MARKDOWN -> "coverage.md"
  }
  return "$repoRoot/coverage_reports/$fileWithoutExtension/$defaultFilename"
}

private fun getFilenameAsLink(filePath: String): String {
  val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
  val filename = filePath.substringAfterLast("/").trim()
  val filenameAsLink = "[$filename]($oppiaDevelopGitHubLink/$filePath)"
  return filenameAsLink
}

private fun loadTestFileExemptionsProto(testFileExemptiontextProto: String): TestFileExemptions {
  return File("$testFileExemptiontextProto.pb").inputStream().use { stream ->
    TestFileExemptions.newBuilder().also { builder ->
      builder.mergeFrom(stream)
    }.build()
  }
}
