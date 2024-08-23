package org.oppia.android.scripts.coverage.reporter

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
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

class CoverageReporterTest {
  @field:[Rule JvmField]
  val tempFolder = TemporaryFolder()

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private lateinit var coverageDir: String

  @Before
  fun setUp() {
    coverageDir = "/coverage_reports"
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testGenerateMarkDownReport_withPassCoverageReportDetails_generatesMarkdownTable() {
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
      ReportFormat.MARKDOWN
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
        "| ${getFilenameAsDetailsSummary(filename)} " +
          "| 100.00% | 10 / 10 | :white_check_mark: | $MIN_THRESHOLD% |\n\n"
      )
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withFailCoverageReportDetails_generatesMarkdownTable() {
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
      ReportFormat.MARKDOWN
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
        "| ${getFilenameAsDetailsSummary(filename)} | " +
          "0.00% | 0 / 10 | :x: | $MIN_THRESHOLD% |\n"
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
      ReportFormat.MARKDOWN
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **0.00%**\n")
      append("Coverage Analysis: **FAIL** :x:\n")
      append("##\n\n")
      append("### Failure Cases\n\n")
      append("| File | Failure Reason | Status |\n")
      append("|------|----------------|--------|\n")
      append("| ://bazelTestTarget | Failure Message | :x: |")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withTestFileExemptionCoverageReport_generatesMarkdownTable() {
    val testExemptedFilePath = "TestExempted.kt"
    val exemptionReason = "This file is exempted from having a test file; skipping coverage check."
    val exemptionsReferenceNote = ">Refer [test_file_exemptions.textproto]" +
      "(https://github.com/oppia/oppia-android/blob/develop/" +
      "scripts/assets/test_file_exemptions.textproto) for the comprehensive " +
      "list of file exemptions and their required coverage percentages."

    val testFileExemption = TestFileExemptions.TestFileExemption.newBuilder().apply {
      this.exemptedFilePath = testExemptedFilePath
      this.testFileNotRequired = true
    }.build()
    val testFileExemptions = TestFileExemptions.newBuilder().apply {
      addTestFileExemption(testFileExemption)
    }.build()

    val exemptionCoverageReport = CoverageReport.newBuilder()
      .setExemption(
        CoverageExemption.newBuilder()
          .setFilePath(testExemptedFilePath)
          .setExemptionReason(exemptionReason)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(exemptionCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testFileExemptionTextProtoPath = createTestFileExemptionsProtoFile(testFileExemptions)
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **0.00%**\n")
      append("Coverage Analysis: **PASS** :white_check_mark:\n")
      append("##\n\n")
      append("### Exempted coverage\n")
      append("<details><summary>Files exempted from coverage</summary><br>")
      append("\n\n")
      append("| File | Exemption Reason |\n")
      append("|------|------------------|\n")
      append("| ${getFilenameAsDetailsSummary(testExemptedFilePath)} | $exemptionReason |")
      append("\n\n")
      append(exemptionsReferenceNote)
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withSourceIncompatibilityExemption_generatesMarkdownTable() {
    val testExemptedFilePath = "TestExempted.kt"
    val exemptionReason = "This file is incompatible with code coverage tooling; " +
      "skipping coverage check."
    val exemptionsReferenceNote = ">Refer [test_file_exemptions.textproto]" +
      "(https://github.com/oppia/oppia-android/blob/develop/" +
      "scripts/assets/test_file_exemptions.textproto) for the comprehensive " +
      "list of file exemptions and their required coverage percentages."

    val testFileExemption = TestFileExemptions.TestFileExemption.newBuilder().apply {
      this.exemptedFilePath = testExemptedFilePath
      this.testFileNotRequired = true
    }.build()
    val testFileExemptions = TestFileExemptions.newBuilder().apply {
      addTestFileExemption(testFileExemption)
    }.build()

    val exemptionCoverageReport = CoverageReport.newBuilder()
      .setExemption(
        CoverageExemption.newBuilder()
          .setFilePath(testExemptedFilePath)
          .setExemptionReason(exemptionReason)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(exemptionCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.MARKDOWN,
      testFileExemptionTextProtoPath = createTestFileExemptionsProtoFile(testFileExemptions)
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 1\n")
      append("Overall Coverage: **0.00%**\n")
      append("Coverage Analysis: **PASS** :white_check_mark:\n")
      append("##\n\n")
      append("### Exempted coverage\n")
      append("<details><summary>Files exempted from coverage</summary><br>")
      append("\n\n")
      append("| File | Exemption Reason |\n")
      append("|------|------------------|\n")
      append("| ${getFilenameAsDetailsSummary(testExemptedFilePath)} | $exemptionReason |")
      append("\n\n")
      append(exemptionsReferenceNote)
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withOverriddenHighCoverage_generatesFailStatusMarkdownTable() {
    val highCoverageRequiredFilePath = "coverage/main/java/com/example/HighCoverageExempted.kt"

    val testFileExemption = TestFileExemptions.TestFileExemption.newBuilder().apply {
      this.exemptedFilePath = highCoverageRequiredFilePath
      this.overrideMinCoveragePercentRequired = 101
    }.build()
    val testFileExemptions = TestFileExemptions.newBuilder().apply {
      addTestFileExemption(testFileExemption)
    }.build()

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
      testFileExemptionTextProtoPath = createTestFileExemptionsProtoFile(testFileExemptions)
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
        "| ${getFilenameAsDetailsSummary(highCoverageRequiredFilePath)} | " +
          "20.00% | 2 / 10 | :x: | 101% _*_ |\n"
      )
      append("\n>**_*_** represents tests with custom overridden pass/fail coverage thresholds")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withOverriddenLowCoverage_generatesPassStatusMarkdownTable() {
    val lowCoverageRequiredFilePath = "coverage/main/java/com/example/LowCoverageExempted.kt"

    val testFileExemption = TestFileExemptions.TestFileExemption.newBuilder().apply {
      this.exemptedFilePath = lowCoverageRequiredFilePath
      this.overrideMinCoveragePercentRequired = 0
    }.build()
    val testFileExemptions = TestFileExemptions.newBuilder().apply {
      addTestFileExemption(testFileExemption)
    }.build()

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
      testFileExemptionTextProtoPath = createTestFileExemptionsProtoFile(testFileExemptions)
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
        "| ${getFilenameAsDetailsSummary(lowCoverageRequiredFilePath)} | " +
          "40.00% | 4 / 10 | :white_check_mark: | 0% _*_ |\n"
      )
      append("\n>**_*_** represents tests with custom overridden pass/fail coverage thresholds\n")
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateMarkDownReport_withCombinedCoverageReportDetails_generatesMarkdownTable() {
    val successFileName = "SampleSuccessFile.kt"
    val failureFileName = "SampleFailureFile.kt"
    val testExemptedFilePath = "TestExempted.kt"
    val exemptionReason = "This file is exempted from having a test file; skipping coverage check."
    val exemptionsReferenceNote = ">Refer [test_file_exemptions.textproto]" +
      "(https://github.com/oppia/oppia-android/blob/develop/" +
      "scripts/assets/test_file_exemptions.textproto) for the comprehensive " +
      "list of file exemptions and their required coverage percentages."

    val testFileExemption = TestFileExemptions.TestFileExemption.newBuilder().apply {
      this.exemptedFilePath = testExemptedFilePath
      this.testFileNotRequired = true
    }.build()
    val testFileExemptions = TestFileExemptions.newBuilder().apply {
      addTestFileExemption(testFileExemption)
    }.build()

    val exemptionCoverageReport = CoverageReport.newBuilder()
      .setExemption(
        CoverageExemption.newBuilder()
          .setFilePath(testExemptedFilePath)
          .setExemptionReason(exemptionReason)
          .build()
      ).build()

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
      testFileExemptionTextProtoPath = createTestFileExemptionsProtoFile(testFileExemptions)
    ).generateRichTextReport()

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 4\n")
      append("Overall Coverage: **50.00%**\n")
      append("Coverage Analysis: **FAIL** :x:\n")
      append("##\n\n")
      append("### Failure Cases\n\n")
      append("| File | Failure Reason | Status |\n")
      append("|------|----------------|--------|\n")
      append("| ://bazelTestTarget | Failure Message | :x: |\n\n")
      append("### Failing coverage\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| ${getFilenameAsDetailsSummary(failureFileName)} | " +
          "0.00% | 0 / 10 | :x: | $MIN_THRESHOLD% |\n\n"
      )
      append("### Passing coverage\n\n")
      append("<details>\n")
      append("<summary>Files with passing code coverage</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| ${getFilenameAsDetailsSummary(successFileName)} | " +
          "100.00% | 10 / 10 | :white_check_mark: | $MIN_THRESHOLD% |\n\n"
      )
      append("</details>\n\n")
      append("### Exempted coverage\n")
      append("<details><summary>Files exempted from coverage</summary><br>")
      append("\n\n")
      append("| File | Exemption Reason |\n")
      append("|------|------------------|\n")
      append("| ${getFilenameAsDetailsSummary(testExemptedFilePath)} | $exemptionReason |")
      append("\n\n")
      append(exemptionsReferenceNote)
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  @Test
  fun testGenerateHtmlReport_withCoverageReportDetails_generatesCorrectContentAndFormatting() {
    val filename = "SampleFile.kt"
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
      ReportFormat.HTML
    ).generateRichTextReport()

    assertThat(outContent.toString().trim()).contains(
      "The coverage analysis for //:bazelTestTarget failed - reason: Failure Message"
    )
  }

  @Test
  fun testGenerateHtmlReport_withCoverageReportExemptions_logsExemptionDetails() {
    System.setOut(PrintStream(outContent))
    val testExemptedFilePath = "TestExempted.kt"
    val additionalData = "This file is exempted from having a test file; skipping coverage check."
    val testFileExemption = TestFileExemptions.TestFileExemption.newBuilder().apply {
      this.exemptedFilePath = testExemptedFilePath
      this.testFileNotRequired = true
    }.build()
    val testFileExemptions = TestFileExemptions.newBuilder().apply {
      addTestFileExemption(testFileExemption)
    }.build()

    val exemptionCoverageReport = CoverageReport.newBuilder()
      .setExemption(
        CoverageExemption.newBuilder()
          .setFilePath(testExemptedFilePath)
          .setExemptionReason(additionalData)
          .build()
      ).build()

    val coverageReportContainer = CoverageReportContainer.newBuilder()
      .addCoverageReport(exemptionCoverageReport)
      .build()

    CoverageReporter(
      tempFolder.root.absolutePath,
      coverageReportContainer,
      ReportFormat.HTML,
      testFileExemptionTextProtoPath = createTestFileExemptionsProtoFile(testFileExemptions)
    ).generateRichTextReport()

    assertThat(outContent.toString().trim()).isEqualTo("-> $testExemptedFilePath - $additionalData")
  }

  @Test
  fun testCoverageReporter_passingInvalidProtoListTextPath_throwsException() {
    val invalidProtoListTextPath = "invalid.txt"

    val exception = assertThrows<IllegalStateException>() {
      main(
        "${tempFolder.root}",
        invalidProtoListTextPath
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("File not found")
  }

  @Test
  fun testCoverageReporter_passingInvalidProtoPath_throwsException() {
    val protoListTextPath = "protoList.txt"
    val protoListTextFile = tempFolder.newFile(protoListTextPath)
    val invalidProtoPath = "invalid.pb"
    protoListTextFile.writeText(invalidProtoPath)

    val exception = assertThrows<IllegalStateException>() {
      main(
        "${tempFolder.root}",
        protoListTextPath
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("Error processing file $invalidProtoPath")
  }

  @Test
  fun testCoverageReporter_successCoverageProtoPath_checksCoverageStatus() {
    System.setOut(PrintStream(outContent))
    val validProtoPath = "coverageReport.pb"
    val protoFile = tempFolder.newFile(validProtoPath)

    val coverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath("file.kt")
          .setLinesFound(10)
          .setLinesHit(10)
          .build()
      ).build()

    protoFile.outputStream().use { outputStream ->
      coverageReport.writeTo(outputStream)
    }

    val protoListTextPath = "protoList.txt"
    val protoListTextFile = tempFolder.newFile(protoListTextPath)
    protoListTextFile.writeText(validProtoPath)

    main(
      "${tempFolder.root}",
      protoListTextPath
    )

    assertThat(outContent.toString().trim())
      .contains("Coverage Analysis$BOLD$GREEN PASSED$RESET")
  }

  @Test
  fun testCoverageReporter_failureCoverageProtoPath_checksCoverageStatus() {
    val validProtoPath = "coverageReport.pb"
    val protoFile = tempFolder.newFile(validProtoPath)

    val coverageReport = CoverageReport.newBuilder()
      .setFailure(
        CoverageFailure.newBuilder()
          .setBazelTestTarget("//:coverageReport")
          .setFailureMessage(
            "Coverage retrieval failed for the test target: " +
              "//:coverageReport"
          )
          .build()
      )
      .build()

    protoFile.outputStream().use { outputStream ->
      coverageReport.writeTo(outputStream)
    }

    val protoListTextPath = "protoList.txt"
    val protoListTextFile = tempFolder.newFile(protoListTextPath)
    protoListTextFile.writeText(validProtoPath)

    val exception = assertThrows<IllegalStateException>() {
      main(
        "${tempFolder.root}",
        protoListTextPath
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")
  }

  @Test
  fun testCoverageReporter_listOfCoverageProtoPath_checksCoverageStatus() {
    val successProtoPath = "successCoverageReport.pb"
    val successProtoFile = tempFolder.newFile(successProtoPath)

    val successCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath("file.kt")
          .setLinesFound(10)
          .setLinesHit(10)
          .build()
      ).build()

    successProtoFile.outputStream().use { outputStream ->
      successCoverageReport.writeTo(outputStream)
    }

    val failureProtoPath = "failureCoverageReport.pb"
    val failureProtoFile = tempFolder.newFile(failureProtoPath)

    val failureCoverageReport = CoverageReport.newBuilder()
      .setFailure(
        CoverageFailure.newBuilder()
          .setBazelTestTarget("//:coverageReport")
          .setFailureMessage(
            "Coverage retrieval failed for the test target: " +
              "//:coverageReport"
          )
          .build()
      )
      .build()

    failureProtoFile.outputStream().use { outputStream ->
      failureCoverageReport.writeTo(outputStream)
    }

    val protoListTextPath = "protoList.txt"
    val protoListTextFile = tempFolder.newFile(protoListTextPath)
    protoListTextFile.appendText(successProtoPath)
    protoListTextFile.appendText(" ")
    protoListTextFile.appendText(failureProtoPath)

    val exception = assertThrows<IllegalStateException>() {
      main(
        "${tempFolder.root}",
        protoListTextPath
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")
  }

  @Test
  fun testCoverageReporter_listOfCoverageProtoPath_generatesMarkdownReport() {
    val successProtoPath = "successCoverageReport.pb"
    val successProtoFile = tempFolder.newFile(successProtoPath)

    val successCoverageReport = CoverageReport.newBuilder()
      .setDetails(
        CoverageDetails.newBuilder()
          .setFilePath("file.kt")
          .setLinesFound(10)
          .setLinesHit(10)
          .build()
      ).build()

    successProtoFile.outputStream().use { outputStream ->
      successCoverageReport.writeTo(outputStream)
    }

    val failureProtoPath = "failureCoverageReport.pb"
    val failureProtoFile = tempFolder.newFile(failureProtoPath)

    val failureCoverageReport = CoverageReport.newBuilder()
      .setFailure(
        CoverageFailure.newBuilder()
          .setBazelTestTarget("//:coverageReport")
          .setFailureMessage("Failure Message")
          .build()
      )
      .build()

    failureProtoFile.outputStream().use { outputStream ->
      failureCoverageReport.writeTo(outputStream)
    }

    val protoListTextPath = "protoList.txt"
    val protoListTextFile = tempFolder.newFile(protoListTextPath)
    protoListTextFile.appendText(successProtoPath)
    protoListTextFile.appendText(" ")
    protoListTextFile.appendText(failureProtoPath)

    val exception = assertThrows<IllegalStateException>() {
      main(
        "${tempFolder.root}",
        protoListTextPath
      )
    }

    assertThat(exception).hasMessageThat()
      .contains("Coverage Analysis$BOLD$RED FAILED$RESET")

    val expectedMarkdown = buildString {
      append("## Coverage Report\n\n")
      append("### Results\n")
      append("Number of files assessed: 2\n")
      append("Overall Coverage: **100.00%**\n")
      append("Coverage Analysis: **FAIL** :x:\n")
      append("##\n\n")
      append("### Failure Cases\n\n")
      append("| File | Failure Reason | Status |\n")
      append("|------|----------------|--------|\n")
      append("| //:coverageReport | Failure Message | :x: |\n")
      append("### Passing coverage\n\n")
      append("<details>\n")
      append("<summary>Files with passing code coverage</summary><br>\n\n")
      append("| File | Coverage | Lines Hit | Status | Min Required |\n")
      append("|------|:--------:|----------:|:------:|:------------:|\n")
      append(
        "| ${getFilenameAsDetailsSummary("file.kt")} " +
          "| 100.00% | 10 / 10 | :white_check_mark: | $MIN_THRESHOLD% |\n\n"
      )
      append("</details>")
    }

    assertThat(readFinalMdReport()).isEqualTo(expectedMarkdown)
  }

  private fun readFinalMdReport(): String {
    return File(
      "${tempFolder.root}" +
        "$coverageDir/CoverageReport.md"
    ).readText()
  }

  private fun getFilenameAsDetailsSummary(
    filePath: String,
    additionalData: String? = null
  ): String {
    val fileName = filePath.substringAfterLast("/")
    val additionalDataPart = additionalData?.let { " - $it" } ?: ""

    return "<details><summary><b>$fileName</b>$additionalDataPart</summary>$filePath</details>"
  }

  private fun createTestFileExemptionsProtoFile(testFileExemptions: TestFileExemptions): String {
    return tempFolder.newFile("test_file_exemptions.pb").also {
      it.outputStream().use(testFileExemptions::writeTo)
    }.path
  }

  private fun loadCoverageReportProto(
    coverageReportProtoPath: String
  ): CoverageReport {
    return File("$coverageReportProtoPath").inputStream().use { stream ->
      CoverageReport.newBuilder().also { builder ->
        builder.mergeFrom(stream)
      }.build()
    }
  }
}
