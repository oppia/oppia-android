package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.proto.CoverageReport
import java.io.File

class CoverageReporter(
  private val coverageReportList: List<CoverageReport>,
  private val reportFormat: ReportFormat,
  private val reportOutputPath: String
) {

  fun generateRichTextReport(computedCoverageRatio: Float): String {
    println("output: $reportOutputPath")
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

  private fun generateHtmlReport(): String {
    // Placeholder for HTML report generation
    return ""
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
