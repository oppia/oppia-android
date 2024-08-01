package org.oppia.android.scripts.coverage

import org.oppia.android.scripts.proto.Coverage
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoverageReportContainer
import java.io.File

/**
 * Class responsible for generating rich text coverage report.
 *
 * @param repoRoot the root directory of the repository
 * @param coverageReport the coverage data proto
 * @param reportFormat the format in which the report will be generated
 */
//class CoverageReporter(
//  private val repoRoot: String,
//  private val coverageReport: CoverageReport,
//  private val coverageReportContainer: CoverageReportContainer,
//  private val reportFormat: ReportFormat,
//) {
//  private val computedCoverageRatio = computeCoverageRatio()
//  private val formattedCoveragePercentage = "%.2f".format(computedCoverageRatio * 100)
//
////  private val filePath = coverageReport.filePath
//  private val filePath = ""
//
//  private val totalLinesFound = coverageReport.details.linesFound
//  private val totalLinesHit = coverageReport.details.linesHit
//
//  /**
//   * Generates a rich text report for the analysed coverage data based on the specified format.
//   * It supports Markdown and HTML formats.
//   *
//   * @return a pair where the first value is the computed coverage ratio represented in [0, 1]
//   *     and the second value is the generated report text
//   */
//  fun generateRichTextReport(): Pair<Float, String> {
//    logCoverageReport()
//
//    return when (reportFormat) {
//      ReportFormat.MARKDOWN -> generateMarkdownReport()
//      ReportFormat.HTML -> generateHtmlReport()
//    }
//  }
//
//  private fun generateMarkdownReport(): Pair<Float, String> {
//    val markdownContent = "|${getFilenameAsLink(filePath)}" +
//      "|$formattedCoveragePercentage%" +
//      "|$totalLinesHit / $totalLinesFound"
//
//    return Pair(computedCoverageRatio, markdownContent)
//  }
//
//  private fun generateHtmlReport(): Pair<Float, String> {
//    println("In generate html report: $coverageReportContainer")
//    // update later
//    // for firstOrNull
//    // have a coveragerport = coveragereportcontainer.coveragereportlist.details
//    // but that will need to be handled differently for md
//    // as that will need to include failure cases
//    // wait no in this way even html wouild have one
//    // so that too needs to be handled
//    println("File path is: ${coverageReportContainer.coverageReportList.firstOrNull()?.details?.filePath}")
//    val filePath = coverageReportContainer.coverageReportList.firstOrNull()?.details?.filePath
//
//    var htmlContent =
//      """
//    <!DOCTYPE html>
//    <html lang="en">
//    <head>
//      <meta charset="UTF-8">
//      <meta name="viewport" content="width=device-width, initial-scale=1.0">
//      <title>Coverage Report</title>
//      <style>
//        body {
//            font-family: Arial, sans-serif;
//            font-size: 12px;
//            line-height: 1.6;
//            padding: 20px;
//        }
//        table {
//            width: 100%;
//            border-collapse: collapse;
//            margin-bottom: 20px;
//        }
//        th, td {
//            padding: 8px;
//            margin-left: 20px;
//            text-align: left;
//            white-space: pre-wrap;
//            border-bottom: 1px solid #e3e3e3;
//        }
//        .line-number-col {
//            width: 4%;
//        }
//        .line-number-row {
//            border-right: 1px solid #ababab
//        }
//        .source-code-col {
//            width: 96%;
//        }
//        .covered-line, .not-covered-line, .uncovered-line {
//            /*white-space: pre-wrap;*/
//        }
//        .covered-line {
//            background-color: #c8e6c9; /* Light green */
//        }
//        .not-covered-line {
//            background-color: #ffcdd2; /* Light red */
//        }
//        .uncovered-line {
//            background-color: #f7f7f7; /* light gray */
//        }
//        .coverage-summary {
//          margin-bottom: 20px;
//        }
//        h2 {
//          text-align: center;
//        }
//        ul {
//          list-style-type: none;
//          padding: 0;
//          text-align: center;
//        }
//        .summary-box {
//          border: 1px solid #ccc;
//          border-radius: 8px;
//          padding: 10px;
//          margin-bottom: 20px;
//          display: flex;
//          justify-content: space-between;
//          align-items: flex-start;
//        }
//        .summary-left {
//          text-align: left;
//        }
//        .summary-right {
//          text-align: right;
//        }
//        .legend {
//          display: flex;
//          align-items: center;
//        }
//        .legend-item {
//          width: 20px;
//          height: 10px;
//          margin-right: 5px;
//          border-radius: 2px;
//          display: inline-block;
//        }
//        .legend .covered {
//          background-color: #c8e6c9; /* Light green */
//        }
//        .legend .not-covered {
//          margin-left: 4px;
//          background-color: #ffcdd2; /* Light red */
//        }
//        @media screen and (max-width: 768px) {
//          body {
//              padding: 10px;
//          }
//          table {
//              width: auto;
//          }
//        }
//      </style>
//    </head>
//    <body>
//      <h2>Coverage Report</h2>
//      <div class="summary-box">
//        <div class="summary-left">
//          <strong>Covered File:</strong> $filePath <br>
//          <div class="legend">
//            <div class="legend-item covered"></div>
//            <span>Covered</span>
//            <div class="legend-item not-covered"></div>
//            <span>Uncovered</span>
//          </div>
//        </div>
//        <div class="summary-right">
//          <div><strong>Coverage percentage:</strong> $formattedCoveragePercentage%</div>
//          <div><strong>Line coverage:</strong> $totalLinesHit / $totalLinesFound covered</div>
//        </div>
//      </div>
//      <table>
//        <thead>
//          <tr>
//            <th class="line-number-col">Line No</th>
//            <th class="source-code-col">Source Code</th>
//          </tr>
//        </thead>
//        <tbody>
//      """.trimIndent()
//
//    val fileContent = File(repoRoot, filePath).readLines()
//    val coverageMap = coverageReport.details.coveredLineList.associateBy { it.lineNumber }
////    val coverageMap = coverageReport.coveredLineList.associateBy { it.lineNumber }
////    val coverageMap = coverageReportContainer.coverageReportList.details.coveredLineList.associateBy {it.lineNumber}
//
//    fileContent.forEachIndexed { index, line ->
//      val lineNumber = index + 1
//      val lineClass = when (coverageMap.get(lineNumber)?.coverage) {
//        Coverage.FULL -> "covered-line"
//        Coverage.NONE -> "not-covered-line"
//        else -> "uncovered-line"
//      }
//      htmlContent += """
//        <tr>
//            <td class="line-number-row">${lineNumber.toString().padStart(4, ' ')}</td>
//            <td class="$lineClass">$line</td>
//        </tr>
//      """.trimIndent()
//    }
//
//    htmlContent += """
//        </tbody>
//      </table>
//    </body>
//    </html>
//    """.trimIndent()
//
//    return Pair(computedCoverageRatio, htmlContent)
//  }
//
//  private fun computeCoverageRatio(): Float {
//    return coverageReport.details.linesFound.takeIf { it != 0 }?.let {
//      coverageReport.details.linesHit.toFloat() / it.toFloat()
//    } ?: 0f
//  }
//
//  private fun logCoverageReport() {
//    val logReportText =
//      """
//      Coverage Report:
//      ---------------
//      Covered File: $filePath
//      Coverage percentage: $formattedCoveragePercentage% covered
//      Line coverage: $totalLinesHit / $totalLinesFound lines covered
//      """
//    println("$logReportText")
//  }
//}
//
//private fun getFilenameAsLink(filePath: String): String {
//  val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
//  val filename = filePath.substringAfterLast("/").trim()
//  val filenameAsLink = "[$filename]($oppiaDevelopGitHubLink/$filePath)"
//  return filenameAsLink
//}
//
///** Represents the different types of formats available to generate code coverage reports. */
//enum class ReportFormat {
//  /** Indicates that the report should be formatted in .md format. */
//  MARKDOWN,
//  /** Indicates that the report should be formatted in .html format. */
//  HTML
//}

// updated
// for local dev save them to a default location
// for ci save them to a specific provided path
// have main call

// ok may what we can do is have a pass to the path to the saved proto container
// and retrieve it here and pass to the reporter

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
