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
    val computedCoveragePercentage = "%.2f".format(computedCoverageRatio)
    val totalFiles = coverageReportList.size
    val filePath = coverageReportList.firstOrNull()?.getCoveredFile(0)?.filePath ?: "Unknown"

    val (totalLinesFound, totalLinesHit) = computeTotalsFor("lines")
    val (totalFunctionsFound, totalFunctionsHit) = computeTotalsFor("functions")
    val (totalBranchesFound, totalBranchesHit) = computeTotalsFor("branches")

    val markdownReport = """
            # Coverage Report

            **Total coverage:**
            - **Files covered:** $totalFiles
            - **Covered File:** $filePath
            - **Coverage percentage:** $computedCoveragePercentage% covered
            - **Line coverage:** $totalLinesHit covered / $totalLinesFound found
            - **Function coverage:** $totalFunctionsHit covered / $totalFunctionsFound found
            - **Branch coverage:** $totalBranchesFound covered / $totalBranchesHit found

        """.trimIndent()

    val outputFile = File(reportOutputPath)
    outputFile.parentFile?.mkdirs()
    outputFile.writeText(markdownReport)

    println("MARKDOWN: $markdownReport")

    return reportOutputPath
  }

  private fun computeTotalsFor(type: String): Pair<Int, Int> {
    var totalFound = 0
    var totalHit = 0

    coverageReportList.forEach { coverageReport ->
      coverageReport.coveredFileList.forEach { coveredFile ->
        when (type) {
          "lines" -> {
            totalFound += coveredFile.linesFound
            totalHit += coveredFile.linesHit
          }
          "functions" -> {
            totalFound += coveredFile.functionsFound
            totalHit += coveredFile.functionsHit
          }
          "branches" -> {
            totalFound += coveredFile.branchesFound
            totalHit += coveredFile.branchesHit
          }
        }
      }
    }

    return Pair(totalFound, totalHit)
  }

  //just line coverage
  fun generateHtmlReport(): String {
//    val reportOutputPath = "path/to/your/report.html"  // Replace with your desired output path

    println("In HTML report generation")

    val coverageReport = coverageReportList.firstOrNull() ?: return "No coverage report found."

    val computedCoverageRatio = computeCoverageRatio() // Implement this function

    val computedCoveragePercentage = "%.2f".format(computedCoverageRatio)
    val totalFiles = coverageReportList.size
    val coveredFile = coverageReport.getCoveredFile(0) ?: return "No covered file found."
    val filePath = coveredFile.filePath ?: "Unknown"

    val (totalLinesFound, totalLinesHit) = Pair(0,0)
    val (totalFunctionsFound, totalFunctionsHit) = Pair(0, 0)
    val (totalBranchesFound, totalBranchesHit) = Pair(0, 0)

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
                    <li><strong>Function coverage:</strong> $totalFunctionsHit covered / $totalFunctionsFound found</li>
                    <li><strong>Branch coverage:</strong> $totalBranchesHit covered / $totalBranchesFound found</li>
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
//    return ""
  }

  fun getColorBasedOnCoverage(hit: Int, found: Int): String {
    val coveragePercentage = if (found == 0) 0 else (hit.toFloat() / found * 100).toInt()
    return when {
      coveragePercentage == 100 -> "#c8e6c9"
      coveragePercentage >= 50 -> "#fff9c4"
      else -> "#ffcdd2"
    }
  }

  fun getCumulativeCoverageClass(): String {
    val isLineCovered = true
    val isBranchCovered = false
    val isFunctionCovered = true

    return when {
      isLineCovered && isBranchCovered && isFunctionCovered -> "covered-line"
      isLineCovered || isBranchCovered || isFunctionCovered -> "partially-covered-line"
      else -> "not-covered-line"
    }
  }

  fun computeCoverageRatio(): Float {
    var totalFound = 0f
    var totalHit = 0f

    coverageReportList.forEach { coverageReport ->
      coverageReport.coveredFileList.forEach { coveredFile ->
        totalFound += (coveredFile.linesFound + coveredFile.functionsFound + coveredFile.branchesFound).toFloat()
        totalHit += (coveredFile.linesHit + coveredFile.functionsHit + coveredFile.branchesHit).toFloat()
      }
    }

    return if (totalFound > 0) (totalHit / totalFound * 100) else 0.0f
  }
}

enum class ReportFormat {
  MARKDOWN,
  HTML
}
