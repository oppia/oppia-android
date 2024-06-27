package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import java.io.File

/**
 * Class responsible for generating rich text coverage report.
 *
 * @param repoRoot the root directory of the repository
 * @param coverageReportList the list of coverage data proto
 * @param reportFormat the format in which the report will be generated
 */
class CoverageReporter(
  private val repoRoot: String,
  private val coverageReportList: List<CoverageReport>,
  private val reportFormat: ReportFormat,
) {
  private val computedCoverageRatio = computeCoverageRatio()
  private val formattedCoveragePercentage = "%.2f".format(computedCoverageRatio * 100)

  private val filePath = coverageReportList.firstOrNull()?.filePath ?: "Unknown"

  private val totalLinesFound = coverageReportList.getOrNull(0)?.linesFound ?: 0
  private val totalLinesHit = coverageReportList.getOrNull(0)?.linesHit ?: 0

  /**
   * Generates a rich text report for the analysed coverage data based on the specified format.
   * It supports Markdown and HTML formats.
   *
   * @return a pair where the first value is the computed coverage ratio represented in [0, 1]
   *     and the second value is the generated report text
   */
  fun generateRichTextReport(): Pair<Float, String> {
    println("report format: $reportFormat")
    return when (reportFormat) {
      ReportFormat.MARKDOWN -> generateMarkdownReport()
      ReportFormat.HTML -> generateHtmlReport()
    }
  }

  private fun generateMarkdownReport(): Pair<Float, String> {
    val markdownContent =
      """
        ## Coverage Report

        - **Covered File:** $filePath
        - **Coverage percentage:** $formattedCoveragePercentage% covered
        - **Line coverage:** $totalLinesHit / $totalLinesFound lines covered
      """.trimIndent()

    println("\n$markdownContent")

    return Pair(computedCoverageRatio, markdownContent)
  }

  private fun generateHtmlReport(): Pair<Float, String> {
    var htmlContent =
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
            border-bottom: 1px solid #fdfdfd;
        }
        .line-number-col {
            width: 5%;
        }
        .source-code-col {
            width: 95%;
        }
        .covered-line, .not-covered-line, .uncovered-line {
            white-space: pre-wrap;
            word-wrap: break-word;
            box-sizing: border-box;
            border-radius: 4px;
            padding: 2px 8px 2px 4px;
            display: inline-block;
        }
        .covered-line {
            background-color: #c8e6c9; /* Light green */
        }
        .not-covered-line {
            background-color: #ffcdd2; /* Light red */
        }
        .uncovered-line {
            background-color: #fafafa; /* Half white */
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
            background-color: #f0f0f0;
            border: 1px solid #ccc;
            border-radius: 8px;
            padding: 10px;
            margin-bottom: 20px;
            text-align: center;
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
        <ul>
          <li><strong>Covered File:</strong> $filePath</li>
          <li><strong>Coverage percentage:</strong> $formattedCoveragePercentage% covered</li>
          <li><strong>Line coverage:</strong> $totalLinesHit covered / $totalLinesFound found</li>
        </ul>
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

    val fileContent = File(repoRoot, filePath).readLines()
    val coverageMap = coverageReportList
      .firstOrNull()?.coveredLineList?.associateBy { it.lineNumber }

    fileContent.forEachIndexed { index, line ->
      val lineNumber = index + 1
      val lineClass = when (coverageMap?.get(lineNumber)?.coverage) {
        Coverage.FULL -> "covered-line"
        Coverage.NONE -> "not-covered-line"
        else -> "uncovered-line"
      }
      htmlContent += """
        <tr>
            <td>${lineNumber.toString().padStart(4, ' ')}</td>
            <td class="$lineClass">$line</td>
        </tr>
      """.trimIndent()
    }

    htmlContent += """
        </tbody>
      </table>
    </body>
    </html>
    """.trimIndent()

    return Pair(computedCoverageRatio, htmlContent)
  }

  private fun computeCoverageRatio(): Float {
    val report = coverageReportList.getOrNull(0)
    return if (report != null && report.linesFound != 0) {
      report.linesHit.toFloat() / report.linesFound.toFloat()
    } else {
      0f
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
