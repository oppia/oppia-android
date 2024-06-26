package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.Coverage
import java.io.File

class CoverageReporter(
  private val repoRoot: String,
  private val coverageReportList: List<CoverageReport>,
  private val reportFormat: ReportFormat,
  private val reportOutputPath: String
) {

  fun generateRichTextReport(computedCoverageRatio: Float): String {
    println("output: $reportOutputPath")
    println("report format: $reportFormat")
    return when (reportFormat) {
      ReportFormat.MARKDOWN -> generateMarkdownReport(computedCoverageRatio)
      ReportFormat.HTML -> generateHtmlReport()
    }
  }

  private fun generateMarkdownReport(computedCoverageRatio: Float): String {
    val computedCoveragePercentage = computedCoverageRatio * 100
    val formattedCoveragePercentage = "%.2f".format(computedCoveragePercentage)
    val filePath = coverageReportList.firstOrNull()?.filePath ?: "Unknown"

    val totalLinesFound = coverageReportList.getOrNull(0)?.linesFound ?: 0
    val totalLinesHit = coverageReportList.getOrNull(0)?.linesHit ?: 0

    val markdownReport = """
        ## Coverage Report

        - **Covered File:** $filePath
        - **Coverage percentage:** $formattedCoveragePercentage% covered
        - **Line coverage:** $totalLinesHit / $totalLinesFound lines covered
    """.trimIndent()

    File(reportOutputPath).apply {
      parentFile?.mkdirs()
      writeText(markdownReport)
    }

    println("\n$markdownReport")

    return reportOutputPath
  }


  fun generateHtmlReport(): String {
  /*
//    val reportOutputPath = "path/to/your/report.html"  // Replace with your desired output path

    println("In HTML report generation")

    val coverageReport = coverageReportList.firstOrNull() ?: return "No coverage report found."

    val computedCoverageRatio = computeCoverageRatio() // Implement this function

    val computedCoveragePercentage = "%.2f".format(computedCoverageRatio)
    val totalFiles = coverageReportList.size
    val coveredFile = coverageReport.getCoveredFile(0) ?: return "No covered file found."
    val filePath = coveredFile.filePath ?: "Unknown"

    val (totalLinesFound, totalLinesHit) = Pair(0,0)

    var htmlContent = """
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
                .covered-line, .not-covered-line, .uncovered-line {
                    display: inline-block;
                    width: auto;
                    padding: 2px 4px;
                    margin: 0;
                }
                .covered-line {
                    background-color: #c8e6c9; /* Light green */
                }
                .not-covered-line {
                    background-color: #ffcdd2; /* Light red */
                }
                .uncovered-line {
                    background-color: #ffffff; /* White */
                }
                .coverage-summary {
                    margin-bottom: 20px;
                }
                pre {
                    white-space: pre-wrap;
                    word-wrap: break-word;
                }
            </style>
        </head>
        <body>
            <h1>Coverage Report</h1>
            <div class="coverage-summary">
                <h2>Total coverage:</h2>
                <ul>
                    <li><strong>Files covered:</strong> $totalFiles</li>
                    <li><strong>Covered File:</strong> $filePath</li>
                    <li><strong>Coverage percentage:</strong> $computedCoveragePercentage% covered</li>
                    <li><strong>Line coverage:</strong> $totalLinesHit covered / $totalLinesFound found</li>
                </ul>
            </div>
            <pre>
    """.trimIndent()

    val fileContent = File("/mnt/c/Users/Baskaran/AndroidStudioProjects/oppia-android", filePath).readLines()
    val coverageMap = coveredFile.coveredLineList.associateBy { it.lineNumber }

    fileContent.forEachIndexed { index, line ->
      val lineNumber = index + 1
      val lineClass = when (coverageMap[lineNumber]?.coverage) {
        Coverage.FULL -> "covered-line"
        Coverage.NONE -> "not-covered-line"
        else -> "uncovered-line"
      }
      htmlContent += "<div class=\"$lineClass\">${lineNumber.toString().padStart(4, ' ')}: $line</div>\n"
    }

    htmlContent += """
            </pre>
        </body>
        </html>
    """.trimIndent()

    println("HTML content: $htmlContent")

    val outputFile = File(reportOutputPath)
    outputFile.parentFile.mkdirs()
    outputFile.writeText(htmlContent)

    return reportOutputPath
  */
    return ""
  }

  fun computeCoverageRatio(): Float {
    val report = coverageReportList.getOrNull(0)
    return if (report != null && report.linesFound != 0) {
      report.linesHit.toFloat() / report.linesFound.toFloat()
    } else {
      0f
    }
  }
}

enum class ReportFormat {
  MARKDOWN,
  HTML
}
