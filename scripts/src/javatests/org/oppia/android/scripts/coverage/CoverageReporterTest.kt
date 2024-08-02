package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.CoverageDetails
import org.oppia.android.scripts.proto.CoverageExemption
import org.oppia.android.scripts.proto.CoverageFailure
import org.oppia.android.scripts.proto.CoverageReport
import org.oppia.android.scripts.proto.CoverageReportContainer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class CoverageReporterTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private lateinit var filename: String
  private val emptyCoverageReportList = listOf<CoverageReport>()

  @Before
  fun setUp() {
    filename = "SampleFile.kt"
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

/*  @Test
  fun testCoverageReporter_validData_generatesCorrectCoverageRatio() {
    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      validCoverageReport,
      ReportFormat.MARKDOWN
    )
    val expectedCoverageRatio = 0.8F
    val (coverageRatio, _) = reporter.generateRichTextReport()
    assertThat(expectedCoverageRatio).isEqualTo(coverageRatio)
  }

  @Test
  fun testCoverageReporter_noLinesFound_generatesZeroCoverageRatio() {
    val expectedZeroCoverageRatio = 0F
    // to check divided by zero error doesn't occur
    val report = validCoverageReport.toBuilder().setLinesFound(0).build()
    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      report,
      ReportFormat.MARKDOWN
    )
    val (coverageRatio, _) = reporter.generateRichTextReport()
    assertThat(expectedZeroCoverageRatio).isEqualTo(coverageRatio)
  }

  @Test
  fun testCoverageReporter_generateMarkdownReport_hasCorrectContentAndFormatting() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"

    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      validCoverageReport,
      ReportFormat.MARKDOWN
    )
    val (_, reportText) = reporter.generateRichTextReport()

    val expectedMarkdown =
      """
        |[$filename]($oppiaDevelopGitHubLink/$filename)|80.00%|8 / 10
      """.trimIndent()

    assertThat(reportText).isEqualTo(expectedMarkdown)
  }*/

//  fun testGenerateHtmlReport_withCoverageReportExemptions_hasCorrectContentAndFormatting() {}

  @Test
  fun testGenerateHtmlReport_withCoverageReportDetails_hasCorrectContentAndFormatting() {
    val coverageDir = "/coverage_reports"
    val sourceFile = tempFolder.newFile(filename)
    sourceFile.writeText(
      """
      fun main() {
        println("Hello, World!")
        val x = 10
        val y = 20
        val sum = x + y
        println("Sum: 30")
        for (i in 1..10) {
            println(i)
        }
    }
      """.trimIndent()
    )

    val validCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath(filename)
          .setLinesFound(10)
          .setLinesHit(8)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(validCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.HTML
    ).generateRichTextReport()

    val outputReportText = File(
      "${tempFolder.root}" +
        "$coverageDir/${filename.removeSuffix(".kt")}/coverage.html"
    ).readText()

    val expectedHtml =
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
          <strong>Covered File:</strong> $filename <br>
          <div class="legend">
            <div class="legend-item covered"></div>
            <span>Covered</span>
            <div class="legend-item not-covered"></div>
            <span>Uncovered</span>
          </div>
        </div>
        <div class="summary-right">
          <div><strong>Coverage percentage:</strong> 80.00%</div>
          <div><strong>Line coverage:</strong> 8 / 10 covered</div>
        </div>
      </div>
      <table>
        <thead>
          <tr>
            <th class="line-number-col">Line No</th>
            <th class="source-code-col">Source Code</th>
          </tr>
        </thead>
        <tbody><tr>
      <td class="line-number-row">   1</td>
      <td class="uncovered-line">  fun main() {</td>
    </tr><tr>
      <td class="line-number-row">   2</td>
      <td class="uncovered-line">    println("Hello, World!")</td>
    </tr><tr>
      <td class="line-number-row">   3</td>
      <td class="uncovered-line">    val x = 10</td>
    </tr><tr>
      <td class="line-number-row">   4</td>
      <td class="uncovered-line">    val y = 20</td>
    </tr><tr>
      <td class="line-number-row">   5</td>
      <td class="uncovered-line">    val sum = x + y</td>
    </tr><tr>
      <td class="line-number-row">   6</td>
      <td class="uncovered-line">    println("Sum: 30")</td>
    </tr><tr>
      <td class="line-number-row">   7</td>
      <td class="uncovered-line">    for (i in 1..10) {</td>
    </tr><tr>
      <td class="line-number-row">   8</td>
      <td class="uncovered-line">        println(i)</td>
    </tr><tr>
      <td class="line-number-row">   9</td>
      <td class="uncovered-line">    }</td>
    </tr><tr>
      <td class="line-number-row">  10</td>
      <td class="uncovered-line">}</td>
    </tr>    </tbody>
      </table>
    </body>
    </html>
      """.trimIndent()

    assertThat(outputReportText).isEqualTo(expectedHtml)
  }

  @Test
  fun testGenerateHtmlReport_withCoverageReportFailures_hasCorrectContentAndFormatting() {
    System.setOut(PrintStream(outContent))
    val failureCoverageReport = CoverageReport.newBuilder()
      .setFailure(
        CoverageFailure.newBuilder()
          .setBazelTestTarget("//:bazelTestTarget")
          .setFailureMessage("Failure Message")
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(failureCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.HTML
    ).generateRichTextReport()

    assertThat(outContent.toString().trim()).contains(
      "The coverage analysis for //:bazelTestTarget failed - reason: Failure Message"
    )
  }

  @Test
  fun testGenerateHtmlReport_withCoverageReportExemptions_hasCorrectContentAndFormatting() {
    System.setOut(PrintStream(outContent))
    val exemptedFilePath = "app/src/main/java/org/oppia/android/app/activity/ActivityComponent.kt"
    val exemptionCoverageReport = CoverageReport.newBuilder()
      .setExemption(
        CoverageExemption.newBuilder()
          .setFilePath(exemptedFilePath)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(exemptionCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.HTML
    ).generateRichTextReport()

    assertThat(outContent.toString().trim()).contains(
      "The file $exemptedFilePath is exempted from coverage analysis"
    )
  }
}
