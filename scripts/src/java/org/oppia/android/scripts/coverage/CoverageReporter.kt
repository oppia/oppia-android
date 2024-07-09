package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import java.io.File

/**
 * Class responsible for generating rich text coverage report.
 *
 * @param repoRoot the root directory of the repository
 * @param coverageReport the coverage data proto
 * @param reportFormat the format in which the report will be generated
 */
class CoverageReporter(
  private val repoRoot: String,
  private val coverageReport: CoverageReport,
  private val reportFormat: ReportFormat,
) {
  private val computedCoverageRatio = computeCoverageRatio()
  private val formattedCoveragePercentage = "%.2f".format(computedCoverageRatio * 100)

  private val filePath = coverageReport.filePath

  private val totalLinesFound = coverageReport.linesFound
  private val totalLinesHit = coverageReport.linesHit

  /**
   * Generates a rich text report for the analysed coverage data based on the specified format.
   * It supports Markdown and HTML formats.
   *
   * @return a pair where the first value is the computed coverage ratio represented in [0, 1]
   *     and the second value is the generated report text
   */
  fun generateRichTextReport(): Pair<Float, String> {
    logCoverageReport()

    // Rough
    /* If HTML -> HTML report -> send reportText -> Done
    *  If MD   -> generate report() -> send reportText ("file.kt, 23%, 2/4 lines, :x:")
    *  oh wait no... the last one is decided also based on exemption
    *  so after coverageCheckThreshold -> determine coverage status ->
    *     set coverage status to PASS or FAIL
    *     reportText += " > :tick: ; < :wrong:"
    *
    *  In execute after awaitAll -> check MD -> generateMD report
    * */

    return when (reportFormat) {
      ReportFormat.MARKDOWN -> generateMarkdownReport()
      ReportFormat.HTML -> generateHtmlReport()
    }
  }

  private fun generateMarkdownReport(): Pair<Float, String> {
    // TODO: (remove)
    /*Thinking of alternating or having 2 versions
    * one for just printing to the console
    * two an actual md template with dropdowns as discussed in meeting
    *
    * Adding the meeting template for reference here
    * Total coverage:
    *  Files covered: (# changed / # run with coverage)
    *  Coverage percentage: ##% covered / ##% expected
    *  LOC: # covered / # instrumented
    *
    *  (indent left) Specific coverage:
    *  app/src/.../app
    *  home
    *  HomeActivity.kt - 87% (110/115)
    *  ...
    *  ...
    *
    * ### Coverage Report
    * <details>
    * <summary>MathModel.kt - 100%</summary>
    * <ul>
    * <li><b>Covered File:</b> <a href="https://github.com/oppia/oppia-android/blob/develop/utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt">
    * utility/src/main/java/org/oppia/android/util/parser/math/MathModel.kt</a>
    * </li>
    * <li><b>Coverage Percentage</b>: 100%
    * <li><b>Lines Coverage</b>: 19/19 covered
    * </ul>
    * </details>
    *
    * Sample template for reference:
    * |Covered File|Percentage|Line Coverage|Status|
      |-------------|:----------:|:---------------:|:------:|
      |[HomeActivity.kt](https://www.github.com)|53.00%|4/7 lines|:x:|


      # Coverage Report

      ## Failed Coverage

      | File Path                      | Coverage Percentage | Line Coverage     |
      |-------------------------------|----------------------|-------------------|
      | src/main/file1.kt              | 45.00%              | 90/200 lines      |
      | src/main/file2.kt              | 50.50%              | 101/200 lines     |
      | src/main/really_long_file_name.kt | 60.00%          | 120/200 lines     |

      <details>
      <summary>Success Coverage</summary>

      | File Path                      | Coverage Percentage | Line Coverage     |Status
      |-------------------------------|:----------------------:|-------------------|:------:|
      | src/main/file3.kt              | 85.00%              | 170/200 lines     |:white_check_mark:|
      | src/main/file4.kt              | 90.50%              | 181/200 lines     |:white_check_mark:|
      | src/main/file5.kt              | 95.00%              | 190/200 lines     |:x:|

      </details>

    */
    val markdownContent = "|$filePath" +
      "|$formattedCoveragePercentage%" +
      "|$totalLinesHit / $totalLinesFound"

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
            margin-left: 20px;
            text-align: left;
            white-space: pre-wrap;
            border-bottom: 1px solid #e3e3e3;
        }
        .line-number-col {
            width: 4%;
        }
        .line-number-row {
            border-right: 1px solid #ababab
        }
        .source-code-col {
            width: 96%;
        }
        .covered-line, .not-covered-line, .uncovered-line {
            /*white-space: pre-wrap;*/
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

    val fileContent = File(repoRoot, filePath).readLines()
    val coverageMap = coverageReport.coveredLineList.associateBy { it.lineNumber }

    fileContent.forEachIndexed { index, line ->
      val lineNumber = index + 1
      val lineClass = when (coverageMap.get(lineNumber)?.coverage) {
        Coverage.FULL -> "covered-line"
        Coverage.NONE -> "not-covered-line"
        else -> "uncovered-line"
      }
      htmlContent += """
        <tr>
            <td class="line-number-row">${lineNumber.toString().padStart(4, ' ')}</td>
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
    return if (coverageReport.linesFound != 0) {
      coverageReport.linesHit.toFloat() / coverageReport.linesFound.toFloat()
    } else {
      0f
    }
  }

  private fun logCoverageReport() {
    // TODO: (remove) as this looks un even in the output log
    val logReportText = listOf(
      "Covered File: $filePath",
      "Coverage percentage: $formattedCoveragePercentage% covered",
      "Line coverage: $totalLinesHit / $totalLinesFound lines covered"
    )

    val maxLength = logReportText.maxOf {it.length}
    val horizontalBorder = "+-${"-".repeat(maxLength)}-+"
    val reportText = logReportText.joinToString(separator = "\n") { line ->
      "| ${line.padEnd(maxLength)} |"
    }

    println("$horizontalBorder\n$reportText\n$horizontalBorder")
  }
}

/** Represents the different types of formats available to generate code coverage reports. */
enum class ReportFormat {
  /** Indicates that the report should be formatted in .md format. */
  MARKDOWN,
  /** Indicates that the report should be formatted in .html format. */
  HTML
}
