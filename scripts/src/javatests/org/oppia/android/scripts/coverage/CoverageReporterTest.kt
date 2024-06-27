package org.oppia.android.scripts.coverage

import com.google.common.truth.Truth.assertThat
import org.junit.*
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.CoverageReport
import java.io.File

class CoverageReporterTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var reporter: CoverageReporter
  private lateinit var validCoverageReport: CoverageReport
  private val emptyCoverageReportList = listOf<CoverageReport>()

  @Before
  fun setUp() {
    validCoverageReport = CoverageReport.newBuilder()
      .setFilePath("SampleFile.kt")
      .setLinesFound(10)
      .setLinesHit(8)
      .build()
  }

  @Test
  fun testCoverageReporter_validData_initializesCorrectly() {
    val expectedTotalLinesFound = 10
    val expectedTotalLinesHit = 8
    val expectedFilePath = "SampleFile.kt"
    val expectedFormattedCoveragePercentage = "80.00"
    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      listOf(validCoverageReport),
      ReportFormat.MARKDOWN
    )
    assertThat(expectedFilePath).isEqualTo(reporter.filePath)
    assertThat(expectedTotalLinesFound).isEqualTo(reporter.totalLinesFound)
    assertThat(expectedTotalLinesHit).isEqualTo(reporter.totalLinesHit)
    assertThat(expectedFormattedCoveragePercentage).isEqualTo(reporter.formattedCoveragePercentage)
  }

  @Test
  fun testCoverageReporter_emptyCoverageReportList_initializesWithDefaults() {
    val expectedTotalLinesFound = 0
    val expectedTotalLinesHit = 0
    val expectedFormattedCoveragePercentage = "0.00"
    val unknownFilePath = "Unknown"
    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      emptyCoverageReportList,
      ReportFormat.MARKDOWN
    )
    assertThat(unknownFilePath).isEqualTo(reporter.filePath)
    assertThat(expectedTotalLinesFound).isEqualTo(reporter.totalLinesFound)
    assertThat(expectedTotalLinesHit).isEqualTo(reporter.totalLinesHit)
    assertThat(expectedFormattedCoveragePercentage).isEqualTo(reporter.formattedCoveragePercentage)
  }

  @Test
  fun testCoverageReporter_validData_generatesCorrectCoverageRatio() {
    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      listOf(validCoverageReport),
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
      listOf(report),
      ReportFormat.MARKDOWN
    )
    val (coverageRatio, _) = reporter.generateRichTextReport()
    assertThat(expectedZeroCoverageRatio).isEqualTo(coverageRatio)
  }

  @Test
  fun testCoverageReporter_markdownReportText_check() {
    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      listOf(validCoverageReport),
      ReportFormat.MARKDOWN
    )
    val (_, reportText) = reporter.generateRichTextReport()

    val expectedMarkdown = """
        ## Coverage Report
        
        - **Covered File:** SampleFile.kt
        - **Coverage percentage:** 80.00% covered
        - **Line coverage:** 8 / 10 lines covered
    """.trimIndent()

    assertThat(reportText).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testCoverageReporter_htmlReportText_check() {
    val sourceFile = tempFolder.newFile("SampleFile.kt")
    sourceFile.writeText("""
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
    """.trimIndent())

    reporter = CoverageReporter(
      tempFolder.root.absolutePath,
      listOf(validCoverageReport),
      ReportFormat.HTML
    )
    val (_, reportText) = reporter.generateHtmlReport()

    val expectedHTML = """
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
        <li><strong>Covered File:</strong> SampleFile.kt</li>
        <li><strong>Coverage percentage:</strong> 80.00% covered</li>
        <li><strong>Line coverage:</strong> 8 covered / 10 found</li>
      </ul>
    </div>
    <table>
      <thead>
        <tr>
          <th class="line-number-col">Line No</th>
          <th class="source-code-col">Source Code</th>
        </tr>
      </thead>
      <tbody><tr>
      <td>   1</td>
      <td class="uncovered-line">  fun main() {</td>
  </tr><tr>
      <td>   2</td>
      <td class="uncovered-line">    println("Hello, World!")</td>
  </tr><tr>
      <td>   3</td>
      <td class="uncovered-line">    val x = 10</td>
  </tr><tr>
      <td>   4</td>
      <td class="uncovered-line">    val y = 20</td>
  </tr><tr>
      <td>   5</td>
      <td class="uncovered-line">    val sum = x + y</td>
  </tr><tr>
      <td>   6</td>
      <td class="uncovered-line">    println("Sum: 30")</td>
  </tr><tr>
      <td>   7</td>
      <td class="uncovered-line">    for (i in 1..10) {</td>
  </tr><tr>
      <td>   8</td>
      <td class="uncovered-line">        println(i)</td>
  </tr><tr>
      <td>   9</td>
      <td class="uncovered-line">    }</td>
  </tr><tr>
      <td>  10</td>
      <td class="uncovered-line">}</td>
  </tr>    </tbody>
    </table>
  </body>
  </html>
    """.trimIndent()

    assertThat(reportText).isEqualTo(expectedHTML)
  }
}
