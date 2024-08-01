package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoverageReportContainer
import org.oppia.android.scripts.proto.TestFileExemptions
import java.io.File

private const val MIN_THRESHOLD = 99 // to be decided and moved to a better place

/**
 * Class responsible for generating rich text coverage report.
 *
 * @param repoRoot the root directory of the repository
 * @param coverageReport the coverage data proto
 * @param reportFormat the format in which the report will be generated
 */

/*fun main(vararg args: String) {
  // add later checks
  val repoRoot = args[0]
  val coverageReportContainer = args[1]
  val reportFormat = args[2]
  val mdReportOutputPath = args[3]

  CoverageReporter(
    repoRoot,
    coverageReportContainer,
    reportFormat,
    mdReportOutputPath
  )
}*/

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

  fun generateRichTextReport() {
    when (reportFormat) {
      ReportFormat.MARKDOWN -> generateMarkdownReport()
      ReportFormat.HTML -> generateHtmlReport()
    }
    logCoverageReport(coverageReportContainer)
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
          val coveragePercentage = if (totalLinesFound > 0) {
            (totalLinesHit.toDouble() / totalLinesFound * 100).toInt()
          } else {
            0
          }
          val formattedCoveragePercentage = "%02d".format(coveragePercentage)

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
            """.trimIndent())

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
              """.trimIndent())
            }

            append(
              """
                      </tbody>
                    </table>
                  </body>
                  </html>
              """.trimIndent())
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
          println("-> The coverage analysis for ${failure.filePath} failed - reason: ${failure.failureMessage} \n")
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

  private fun generateMarkdownReport() {
    val coverageTableHeader = "| Covered File | Percentage | Line Coverage | Status |\n" +
      "|--------------|------------|---------------|--------|\n"

    val (failures, rest) = coverageReportContainer.coverageReportList.partition { it.hasFailure() }
    val (belowThreshold, successes) = rest.partition { report ->
      report.hasDetails() && report.details.let {
        val totalLinesFound = it.linesFound
        val totalLinesHit = it.linesHit
        val coveragePercentage = if (totalLinesFound > 0) {
          (totalLinesHit.toDouble() / totalLinesFound * 100).toInt()
        } else {
          0
        }
        coveragePercentage < MIN_THRESHOLD
      }
    }

    val failureTableRows = failures.filter { it.hasFailure() }.joinToString(separator = "\n") { report ->
      val failure = report.failure
      "| ${failure.filePath} | ${failure.failureMessage} |"
    }

    val belowThresholdTableRows = belowThreshold.filter { it.hasDetails() }.joinToString(separator = "\n") { report ->
      val details = report.details
      val filePath = details.filePath
      val totalLinesFound = details.linesFound
      val totalLinesHit = details.linesHit
      val coveragePercentage = if (totalLinesFound > 0) {
        (totalLinesHit.toDouble() / totalLinesFound * 100).toInt()
      } else {
        0
      }
      val formattedCoveragePercentage = "%02d".format(coveragePercentage)
      "| ${getFilenameAsLink(filePath)} | $formattedCoveragePercentage% | $totalLinesHit / $totalLinesFound | :x: |"
    }

    val successTableRows = successes.filter { it.hasDetails() }.joinToString(separator = "\n") { report ->
      val details = report.details
      val filePath = details.filePath
      val totalLinesFound = details.linesFound
      val totalLinesHit = details.linesHit
      val coveragePercentage = if (totalLinesFound > 0) {
        (totalLinesHit.toDouble() / totalLinesFound * 100).toInt()
      } else {
        0
      }
      val formattedCoveragePercentage = "%02d".format(coveragePercentage)
      "| ${getFilenameAsLink(filePath)} | $formattedCoveragePercentage% | $totalLinesHit / $totalLinesFound | :white_check_mark: |"
    }

    val anomalyCasesList = coverageReportContainer.coverageReportList
      .filter { it.hasExemption() }
      .map { exemption ->
        val filePath = exemption.exemption.filePath
        "${getFilenameAsLink(filePath)}"
      }.joinToString(separator = "\n") { "- $it" }

    val failureMarkdownTable = if (failureTableRows.isNotEmpty()) {
      "### Failed Cases\n" +
        "| File | Failure Reason |\n" +
        "|------|----------------|\n" +
        failureTableRows
    } else ""

    val belowThresholdMarkdownTable = if (belowThresholdTableRows.isNotEmpty()) {
      "### Coverage Below Minimum Threshold\n" +
        "Min Coverage Required: $MIN_THRESHOLD%\n\n" +
        coverageTableHeader +
        belowThresholdTableRows
    } else ""

    val successMarkdownTable = if (successTableRows.isNotEmpty()) {
      "<details>\n" +
        "<summary>Succeeded Coverages</summary><br>\n\n" +
        coverageTableHeader +
        successTableRows +
        "\n</details>"
    } else ""

    val anomalySection = if (anomalyCasesList.isNotEmpty()) {
      "\n\n### Exempted Files\n$anomalyCasesList"
    } else ""

    val finalReportText = "## Coverage Report\n\n" +
      "- Number of files assessed: ${coverageReportContainer.coverageReportList.size}\n" +
//      "- Coverage Status: **$coverageCheckState**\n" +
      failureMarkdownTable +
      "\n\n" +
      belowThresholdMarkdownTable +
      "\n\n" +
      successMarkdownTable +
      anomalySection

    println("Final report text: $finalReportText")

    val finalReportOutputPath = "$repoRoot/coverage_reports/CoverageReport.md"
    File(finalReportOutputPath).apply {
      parentFile?.mkdirs()
      writeText(finalReportText)
    }
  }


  private fun logCoverageReport(container: CoverageReportContainer) {
    println(
      """
    |COVERAGE REPORT:
    |---------------- ${"\n"}
    """.trimMargin().prependIndent("  ")
    )
    container.coverageReportList.forEach { coverageReport ->
      when {
        coverageReport.hasDetails() -> {
          val details = coverageReport.details
          val filePath = details.filePath
          val totalLinesFound = details.linesFound
          val totalLinesHit = details.linesHit
          val coveragePercentage = if (totalLinesFound > 0) {
            (totalLinesHit.toDouble() / totalLinesFound * 100).toInt()
          } else {
            0
          }
          val formattedCoveragePercentage = "%02d".format(coveragePercentage)

          val logReportText =
            """
            |Coverage Report Success:
            |------------------------
            |Covered File: $filePath
            |Coverage percentage: $formattedCoveragePercentage% covered
            |Line coverage: $totalLinesHit / $totalLinesFound lines covered ${"\n"}
            """.trimMargin().prependIndent("  ")

          println(logReportText)
        }
        coverageReport.hasFailure() -> {
          val failure = coverageReport.failure
          val logReportText =
            """
            |Coverage Report Failure:
            |------------------------
            |Covered File: ${failure.filePath}
            |Test Target: ${failure.bazelTestTarget}
            |Failure Message: ${failure.failureMessage} ${"\n"}
            """.trimMargin().prependIndent("  ")

          println(logReportText)
        }
        coverageReport.hasExemption() -> {
          val exemption = coverageReport.exemption
          val logReportText =
            """
            |Coverage Report Exemption:
            |--------------------------
            |Exempted File: ${exemption.filePath} ${"\n"}
            """.trimMargin().prependIndent("  ")

          println(logReportText)
        }
        else -> {
          println("Unknown Coverage Report Type")
        }
      }
    }
  }
}

/** Represents the different types of formats available to generate code coverage reports. */
enum class ReportFormat {
  /** Indicates that the report should be formatted in .md format. */
  MARKDOWN,
  /** Indicates that the report should be formatted in .html format. */
  HTML
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
