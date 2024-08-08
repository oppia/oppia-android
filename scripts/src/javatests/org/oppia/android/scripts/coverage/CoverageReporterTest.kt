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
import org.oppia.android.scripts.proto.TestFileExemptions
import org.oppia.android.scripts.proto.TestFileExemptions.TestFileExemption
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class CoverageReporterTest {
  @field:[Rule JvmField]
  val tempFolder = TemporaryFolder()

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out

  private lateinit var coverageDir: String
  private lateinit var testExemptions: Map<String, TestFileExemptions.TestFileExemption>

  @Before
  fun setUp() {
    coverageDir = "/coverage_reports"
    testExemptions = createTestFileExemptionTextProto()
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testGenerateMarkDownReport_withPassCoverageReportDetails_generatesMarkdownTable() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filename = "SampleFile.kt"
    val validCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath(filename)
          .setLinesFound(10)
          .setLinesHit(10)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(validCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testExemptions
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **100.00%**\n")
      append("Coverage Analysis: **PASS** :white_check_mark:\n")
      append("##\n")
      append("### Passing coverage\n\n")
      append("<details>\n")
      append("<summary>Files with passing code coverage</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [$filename]($oppiaDevelopGitHubLink/$filename) " +
          "| 100.00% | 10 / 10 | :white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withFailCoverageReportDetails_generatesMarkdownTable() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val filename = "SampleFile.kt"
    val validCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath(filename)
          .setLinesFound(10)
          .setLinesHit(0)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(validCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testExemptions
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **0.00%**\n")
      append("Coverage Analysis: **FAIL** :x:\n")
      append("##\n\n")
      append("### Failing coverage\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [$filename]($oppiaDevelopGitHubLink/$filename) | " +
          "0.00% | 0 / 10 | :x: | $MIN_THRESHOLD% |"
      )
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withFailureCoverageReportDetails_generatesMarkdownTable() {
    val failureCoverageReport = CoverageReport.newBuilder()
      .setFailure(
        CoverageFailure.newBuilder()
          .setBazelTestTarget("://bazelTestTarget")
          .setFailureMessage("Failure Message")
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(failureCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testExemptions
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **0.00%**\n")
      append("Coverage Analysis: **FAIL** :x:\n")
      append("##\n\n")
      append("### Failure Cases\n\n")
      append("| File | Failure Reason |\n")
      append("|------|----------------|\n")
      append("| ://bazelTestTarget | Failure Message |")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withExemptionCoverageReportDetails_generatesMarkdownTable() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val exemptedFilePath = "TestExempted.kt"
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
      ReportFormat.MARKDOWN,
      testExemptions
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **0.00%**\n")
      append("Coverage Analysis: **PASS** :white_check_mark:\n")
      append("##\n\n")
      append("### Files Exempted from Coverage\n")
      append("- [TestExempted.kt]($oppiaDevelopGitHubLink/$exemptedFilePath)")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withOverriddenHighCoverage_generatesFailStatusMarkdownTable() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val highCoverageRequiredFilePath = "coverage/main/java/com/example/HighCoverageExempted.kt"
    val highCoverageRequiredCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath(highCoverageRequiredFilePath)
          .setLinesFound(10)
          .setLinesHit(2)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(highCoverageRequiredCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testExemptions
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **20.00%**\n")
      append("Coverage Analysis: **FAIL** :x:\n")
      append("##\n\n")
      append("### Failing coverage\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [HighCoverageExempted.kt]($oppiaDevelopGitHubLink/$highCoverageRequiredFilePath) | " +
          "20.00% | 2 / 10 | :x: | 101% _*_ |\n"
      )
      append("\n>**_*_** represents tests with custom overridden pass/fail coverage thresholds")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withOverriddenLowCoverage_generatesPassStatusMarkdownTable() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val lowCoverageRequiredFilePath = "coverage/main/java/com/example/LowCoverageExempted.kt"
    val lowCoverageRequiredCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath(lowCoverageRequiredFilePath)
          .setLinesFound(10)
          .setLinesHit(4)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(lowCoverageRequiredCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testExemptions
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **40.00%**\n")
      append("Coverage Analysis: **PASS** :white_check_mark:\n")
      append("##\n")
      append("### Passing coverage\n\n")
      append("<details>\n")
      append("<summary>Files with passing code coverage</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [LowCoverageExempted.kt]($oppiaDevelopGitHubLink/$lowCoverageRequiredFilePath) | " +
          "40.00% | 4 / 10 | :white_check_mark: | 0% _*_ |\n"
      )
      append("\n>**_*_** represents tests with custom overridden pass/fail coverage thresholds\n")
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withCombinedCoverageReportDetails_generatesMarkdownTable() {
    val oppiaDevelopGitHubLink = "https://github.com/oppia/oppia-android/tree/develop"
    val successFileName = "SampleSuccessFile.kt"
    val failureFileName = "SampleFailureFile.kt"
    val exemptedFilePath = "TestExempted.kt"
    val validPassCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath(successFileName)
          .setLinesFound(10)
          .setLinesHit(10)
          .build()
      ).build()

    val validFailCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath(failureFileName)
          .setLinesFound(10)
          .setLinesHit(0)
          .build()
      ).build()

    val failureCoverageReport = CoverageReport.newBuilder()
      .setFailure(
        CoverageFailure.newBuilder()
          .setBazelTestTarget("://bazelTestTarget")
          .setFailureMessage("Failure Message")
          .build()
      ).build()

    val exemptionCoverageReport = CoverageReport.newBuilder()
      .setExemption(
        CoverageExemption.newBuilder()
          .setFilePath(exemptedFilePath)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(validPassCoverageReport)
      .addCoverageReport(validFailCoverageReport)
      .addCoverageReport(failureCoverageReport)
      .addCoverageReport(exemptionCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testExemptions
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 4\n")
      append("Overall Coverage: **50.00%**\n")
      append("Coverage Analysis: **FAIL** :x:\n")
      append("##\n\n")
      append("### Failure Cases\n\n")
      append("| File | Failure Reason |\n")
      append("|------|----------------|\n")
      append("| ://bazelTestTarget | Failure Message |\n\n")
      append("### Failing coverage\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [$failureFileName]($oppiaDevelopGitHubLink/$failureFileName) | " +
          "0.00% | 0 / 10 | :x: | $MIN_THRESHOLD% |\n"
      )
      append("### Passing coverage\n\n")
      append("<details>\n")
      append("<summary>Files with passing code coverage</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| [$successFileName]($oppiaDevelopGitHubLink/$successFileName) | " +
          "100.00% | 10 / 10 | :white_check_mark: | $MIN_THRESHOLD% |\n"
      )
      append("</details>\n\n")
      append("### Files Exempted from Coverage\n")
      append("- [TestExempted.kt]($oppiaDevelopGitHubLink/$exemptedFilePath)")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateHtmlReport_withCoverageReportDetails_generatesCorrectContentAndFormatting() {
    val filename = "SampleFile.kt"
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
      ReportFormat.HTML,
      testExemptions
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
  fun testGenerateHtmlReport_withCoverageReportFailures_logsFailureDetails() {
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
      ReportFormat.HTML,
      testExemptions
    ).generateRichTextReport()

    assertThat(outContent.toString().trim()).contains(
      "The coverage analysis for //:bazelTestTarget failed - reason: Failure Message"
    )
  }

  @Test
  fun testGenerateHtmlReport_withCoverageReportExemptions_logsExemptionDetails() {
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
      ReportFormat.HTML,
      testExemptions
    ).generateRichTextReport()

    assertThat(outContent.toString().trim()).contains(
      "The file $exemptedFilePath is exempted from coverage analysis"
    )
  }

  private fun readFinalMdReport(): String {
    return File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()
  }

  private fun createTestFileExemptionTextProto():
    Map<String, TestFileExemptions.TestFileExemption> {
      val testFileExemptions = TestFileExemptions.newBuilder()
        .addTestFileExemption(
          TestFileExemption.newBuilder()
            .setExemptedFilePath("TestExempted.kt")
            .setTestFileNotRequired(true)
            .build()
        )
        .addTestFileExemption(
          TestFileExemption.newBuilder()
            .setExemptedFilePath("coverage/main/java/com/example/HighCoverageExempted.kt")
            .setOverrideMinCoveragePercentRequired(101)
            .build()
        )
        .addTestFileExemption(
          TestFileExemption.newBuilder()
            .setExemptedFilePath("coverage/main/java/com/example/LowCoverageExempted.kt")
            .setOverrideMinCoveragePercentRequired(0)
            .build()
        )
        .build()

      val testExemptionPb = "test_exemption.pb"
      val coverageTestExemptionTextProto = tempFolder.newFile(testExemptionPb)
      coverageTestExemptionTextProto.outputStream().use { outputStream ->
        testFileExemptions.writeTo(outputStream)
      }

      val testFileExemptionsFromFile =
        TestFileExemptions.parseFrom(coverageTestExemptionTextProto.inputStream())

      return testFileExemptionsFromFile.testFileExemptionList
        .associateBy { it.exemptedFilePath }
    }
}
