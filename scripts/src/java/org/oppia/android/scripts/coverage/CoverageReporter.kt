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
            - **Line coverage:** $totalLinesFound covered / $totalLinesHit found
            - **Function coverage:** $totalFunctionsHit covered / $totalFunctionsFound found
            - **Branch coverage:** $totalBranchesFound covered / $totalBranchesHit found

        """.trimIndent()

    val outputFile = File(reportOutputPath)
    outputFile.parentFile?.mkdirs()
    outputFile.writeText(markdownReport)

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

  fun generateHtmlReport(): String {
    println("In HTML report generation")

    val coverageReport = coverageReportList.firstOrNull() ?: return "No coverage report found."

    val computedCoverageRatio = computeCoverageRatio()

    val computedCoveragePercentage = "%.2f".format(computedCoverageRatio)
    val totalFiles = coverageReportList.size
    val coveredFile = coverageReport.getCoveredFile(0) ?: return "No covered file found."
    val filePath = coveredFile.filePath ?: "Unknown"

    val (totalLinesFound, totalLinesHit) = Pair(0,0)
    val (totalFunctionsFound, totalFunctionsHit) = Pair(0,0)
    val (totalBranchesFound, totalBranchesHit) = Pair(0,0)

    val lineCoverageColor = getColorBasedOnCoverage(totalLinesHit, totalLinesFound)
    val functionCoverageColor = getColorBasedOnCoverage(totalFunctionsHit, totalFunctionsFound)
    val branchCoverageColor = getColorBasedOnCoverage(totalBranchesHit, totalBranchesFound)

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
                .covered-line {
                    background-color: #c8e6c9; /* Light green */
                }
                .not-covered-line {
                    background-color: #ffcdd2; /* Light red */
                }
                .uncovered-line {
                    background-color: #ffffff; /* White */
                }
                .partially-covered-line {
                    background-color: #fff9c4; /* Light yellow */
                }
                .coverage-summary {
                    margin-bottom: 20px;
                    padding: 10px;
                    border: 1px solid #ddd;
                    background-color: #f9f9f9;
                    border-radius: 5px;
                }
                .coverage-summary h2 {
                    margin-top: 0;
                }
                .coverage-summary ul {
                    list-style-type: none;
                    padding-left: 0;
                }
                .coverage-summary li {
                    margin-bottom: 5px;
                }
                .coverage-summary .legend {
                    display: flex;
                    gap: 10px;
                    margin-top: 10px;
                }
                .legend-item {
                    display: flex;
                    align-items: center;
                    gap: 5px;
                }
                .legend-color {
                    width: 20px;
                    height: 20px;
                    border: 1px solid #ddd;
                }
                .legend-label {
                    font-size: 14px;
                }
                pre {
                    white-space: pre-wrap;
                    word-wrap: break-word;
                }
                .line {
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid #ddd;
                }
                .line-number, .branch-coverage, .function-coverage {
                    width: 50px;
                    text-align: right;
                    padding-right: 10px;
                    margin-right: 10px;
                    border-right: 1px solid #ddd;
                }
                .code-block {
                    flex-grow: 1;
                    padding-left: 10px;
                }
                .header {
                    display: flex;
                    font-weight: bold;
                    background-color: #f1f1f1;
                    padding: 5px 0;
                }
                .header .line-number, .header .branch-coverage, .header .function-coverage, .header .code-block {
                    border-right: none;
                    text-align: center;
                }
                .coverage-summary li.line-coverage {
                    background-color: $lineCoverageColor;
                }
                .coverage-summary li.branch-coverage {
                    background-color: $branchCoverageColor;
                }
                .coverage-summary li.function-coverage {
                    background-color: $functionCoverageColor;
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
                    <li class="line-coverage"><strong>Line coverage:</strong> $totalLinesHit covered / $totalLinesFound found</li>
                    <li class="function-coverage"><strong>Function coverage:</strong> $totalFunctionsHit covered / $totalFunctionsFound found</li>
                    <li class="branch-coverage"><strong>Branch coverage:</strong> $totalBranchesHit covered / $totalBranchesFound found</li>
                </ul>
                <div class="legend">
                    <div class="legend-item">
                        <div class="legend-color" style="background-color: #c8e6c9;"></div>
                        <div class="legend-label">Fully covered</div>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color" style="background-color: #fff9c4;"></div>
                        <div class="legend-label">Partially covered</div>
                    </div>
                    <div class="legend-item">
                        <div class="legend-color" style="background-color: #ffcdd2;"></div>
                        <div class="legend-label">Low coverage</div>
                    </div>
                </div>
            </div>
            <pre>
                <div class="header">
                    <span class="line-number">Line</span>
                    <span class="branch-coverage">Branch</span>
                    <span class="function-coverage">Function</span>
                    <span class="code-block">Code</span>
                </div>
    """.trimIndent()

    val fileContent = File(repoRoot, filePath).readLines()

    fileContent.forEachIndexed { index, line ->
      val lineNumber = index + 1

      val lineClass = getCumulativeCoverageClass()

      val branchCoverage = coveredFile.branchCoverageList.find { it.lineNumber == lineNumber }
      val branchCoverageText = branchCoverage?.let { "${it.hitCount}" } ?: ""

      val functionCoverage = coveredFile.functionCoverageList.find { it.lineNumber == lineNumber }
      val functionCoverageText = functionCoverage?.let { "${it.executionCount}" } ?: ""


      htmlContent += """
            <div class="line $lineClass">
                <span class="line-number">${lineNumber.toString().padStart(4, ' ')}</span>
                <span class="branch-coverage">${branchCoverageText}</span>
                <span class="function-coverage">${functionCoverageText}</span>
                <span class="code-block">${line}</span>
            </div>
        """.trimIndent()
    }

    htmlContent += """
            </pre>
        </body>
        </html>
    """.trimIndent()

    println("HtMl content: $htmlContent")

    return ""
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
