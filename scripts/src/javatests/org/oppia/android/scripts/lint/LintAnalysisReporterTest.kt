package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.lang.IllegalArgumentException

/** Tests for [LintAnalysisReporter]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class LintAnalysisReporterTest {

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var lintAnalysisReporter: LintAnalysisReporter
  private val originalOut = System.out
  private val outputStream = ByteArrayOutputStream()

  companion object {
    private const val XML_HEADER =
      """<issues format="6" by="lint 7.3.1">"""
    private const val XML_FOOTER =
      """</issues>"""

    private val warningIssue = LintIssue(
      id = "UsesMinSdkAttributes",
      severity = LintSeverity.WARNING,
      message = "Manifest should specify a minimum API level",
      category = "Correctness",
      priority = "9",
      summary = "Minimum SDK and target SDK attributes not defined",
      explanation = "The manifest should contain a uses-sdk element",
      errorLine1 = "    <application",
      errorLine2 = "    ~~~~~~~~~~~~",
      locations = listOf(
        LintLocation(
          file = "src/main/AndroidManifest.xml",
          lineNumber = "5"
        )
      )
    )

    private val errorIssue = LintIssue(
      id = "NewApi",
      severity = LintSeverity.ERROR,
      message = "Call requires API level 24 (current min is 19)",
      category = "Correctness",
      priority = "6",
      summary = "Calling new methods on older versions",
      explanation = "This check scans through all the Android API calls",
      errorLine1 = "        stream.forEach { println(it) }",
      errorLine2 = "               ~~~~~~~",
      locations = listOf(
        LintLocation(
          file = "src/main/java/MainActivity.kt",
          lineNumber = "42"
        )
      )
    )

    private val informationIssue = LintIssue(
      id = "IidCompatibilityCheckFailure",
      severity = LintSeverity.INFORMATION,
      message = "Check failed with exception: java.lang.NoSuchMethodException",
      category = "Lint",
      priority = "1",
      summary = "Firebase IID Compatibility Check Unable To Run",
      explanation = "The check failed to run as it encountered unknown failure.",
      errorLine1 = "",
      errorLine2 = "",
      locations = listOf(
        LintLocation(
          file = "test.xml",
          lineNumber = ""
        )
      )
    )

    private val multiLocationIssue = LintIssue(
      id = "UnusedResources",
      severity = LintSeverity.WARNING,
      message =
        "The resource `R.color.color_palette_save_button_border_color` appears to be unused",
      category = "Performance",
      priority = "3",
      summary = "Unused resources",
      explanation = "Unused resources make applications larger and slow down builds.",
      errorLine1 = "    <color name=\"color_palette_save_button_border_color\">#FF0000</color>",
      errorLine2 = "           ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~",
      locations = listOf(
        LintLocation(
          file = "app/src/main/res/values/color_palette.xml",
          lineNumber = "164"
        ),
        LintLocation(
          file = "app/src/main/res/values-night/color_palette.xml",
          lineNumber = "159"
        )
      )
    )
  }

  @Before
  fun setUp() {
    lintAnalysisReporter = LintAnalysisReporter()
    System.setOut(PrintStream(outputStream))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  @Test
  fun testParseLintReport_validXmlWithSingleIssue_parsesCorrectly() {
    val xmlContent = createXmlWithIssues(warningIssue)
    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.id).isEqualTo(warningIssue.id)
    assertThat(issue.severity).isEqualTo(warningIssue.severity)
    assertThat(issue.message).isEqualTo(warningIssue.message)
    assertThat(issue.category).isEqualTo(warningIssue.category)
    assertThat(issue.priority).isEqualTo(warningIssue.priority)
    assertThat(issue.summary).isEqualTo(warningIssue.summary)
    assertThat(issue.explanation).isEqualTo(warningIssue.explanation)
    assertThat(issue.errorLine1).isEqualTo(warningIssue.errorLine1)
    assertThat(issue.errorLine2).isEqualTo(warningIssue.errorLine2)
    assertThat(issue.locations).hasSize(1)
    assertThat(issue.locations[0].file).isEqualTo("src/main/AndroidManifest.xml")
    assertThat(issue.locations[0].lineNumber).isEqualTo("5")
  }

  @Test
  fun testParseLintReport_validXmlWithMultipleIssues_parsesAll() {
    val xmlContent = createXmlWithIssues(warningIssue, errorIssue)
    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(2)
    assertThat(issues[0].id).isEqualTo(warningIssue.id)
    assertThat(issues[0].severity).isEqualTo(LintSeverity.WARNING)
    assertThat(issues[0].errorLine1).isEqualTo(warningIssue.errorLine1)
    assertThat(issues[0].errorLine2).isEqualTo(warningIssue.errorLine2)
    assertThat(issues[1].id).isEqualTo(errorIssue.id)
    assertThat(issues[1].severity).isEqualTo(LintSeverity.ERROR)
    assertThat(issues[1].errorLine1).isEqualTo(errorIssue.errorLine1)
    assertThat(issues[1].errorLine2).isEqualTo(errorIssue.errorLine2)
  }

  @Test
  fun testParseLintReport_issueWithMultipleLocations_parsesAllLocations() {
    val xmlContent = createXmlWithIssues(multiLocationIssue)
    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.id).isEqualTo("UnusedResources")
    assertThat(issue.errorLine1).isEqualTo(multiLocationIssue.errorLine1)
    assertThat(issue.errorLine2).isEqualTo(multiLocationIssue.errorLine2)
    assertThat(issue.locations).hasSize(2)

    assertThat(issue.locations[0].file)
      .isEqualTo("app/src/main/res/values/color_palette.xml")
    assertThat(issue.locations[0].lineNumber).isEqualTo("164")

    assertThat(issue.locations[1].file)
      .isEqualTo("app/src/main/res/values-night/color_palette.xml")
    assertThat(issue.locations[1].lineNumber).isEqualTo("159")
  }

  @Test
  fun testParseLintReport_emptyXml_returnsEmptyList() {
    val xmlContent =
      """
      $XML_HEADER
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).isEmpty()
  }

  @Test
  fun testParseLintReport_issueWithLocationButNoLine_hasEmptyLineNumber() {
    val xmlContent = createXmlWithIssues(informationIssue)
    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    assertThat(issues[0].locations).hasSize(1)
    assertThat(issues[0].locations[0].file).isEqualTo("test.xml")
    assertThat(issues[0].locations[0].lineNumber).isEmpty()
    assertThat(issues[0].errorLine1).isEmpty()
    assertThat(issues[0].errorLine2).isEmpty()
  }

  @Test
  fun testParseLintReport_issueWithEmptyFileAttributes_filtersOutEmptyLocations() {
    val xmlContent =
      """
      $XML_HEADER
        <issue
            id="TestIssueWithEmptyLocations"
            severity="Warning"
            message="Test message">
            <location file="valid_file.xml" line="10"/>
            <location file="" line="20"/>
            <location file="another_valid_file.xml" line="30"/>
            <location file="   " line="40"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.locations).hasSize(2)
    assertThat(issue.locations[0].file).isEqualTo("valid_file.xml")
    assertThat(issue.locations[0].lineNumber).isEqualTo("10")
    assertThat(issue.locations[1].file).isEqualTo("another_valid_file.xml")
    assertThat(issue.locations[1].lineNumber).isEqualTo("30")
  }

  @Test
  fun testParseLintReport_issueWithOnlyEmptyFileAttributes_throwsException() {
    val xmlContent =
      """
      $XML_HEADER
        <issue
            id="TestIssueWithOnlyEmptyLocations"
            severity="Warning"
            message="Test message">
            <location file="" line="10"/>
            <location file="   " line="20"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Issue element must contain at least one location")
  }

  @Test
  fun testParseLintReport_nonExistentFile_throwsException() {
    val nonExistentPath = "/path/that/does/not/exist/lint-report.xml"

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.parseLintReport(nonExistentPath)
    }

    assertThat(exception).hasMessageThat().contains("Lint report file not found: $nonExistentPath")
  }

  @Test
  fun testParseLintReport_malformedXml_throwsError() {
    val malformedXml =
      """
      $XML_HEADER
        <issue id="TestIssue" severity="Warning"
          <!-- Missing closing tag -->
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(malformedXml)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Error processing file")
  }

  @Test
  fun testParseLintReport_issueWithSpecialCharacters_handlesCorrectly() {
    val xmlContent =
      """
      $XML_HEADER
        <issue
            id="SpecialCharsTest"
            severity="Error"
            message="Message with &lt;special&gt; &amp; characters &quot;quoted&quot;"
            category="Test"
            priority="1"
            summary="Summary with special chars"
            explanation="Explanation with &lt;tags&gt; and &amp;amp; symbols"
            errorLine1="val text = &quot;Hello &lt;world&gt; &amp; friends&quot;"
            errorLine2="           ~~~~~~~~~~~~~~~~~~~~~~">
            <location file="path/with spaces/file&amp;name.xml" line="15"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.message).isEqualTo("Message with <special> & characters \"quoted\"")
    assertThat(issue.errorLine1).isEqualTo("val text = \"Hello <world> & friends\"")
    assertThat(issue.errorLine2).isEqualTo("           ~~~~~~~~~~~~~~~~~~~~~~")
    assertThat(issue.locations).hasSize(1)
    assertThat(issue.locations[0].file).isEqualTo("path/with spaces/file&name.xml")
    assertThat(issue.locations[0].lineNumber).isEqualTo("15")
  }

  @Test
  fun testParseLintReport_emptyRequiredAttributes_throwsException() {
    val xmlContent =
      """
      $XML_HEADER
        <issue id="" severity="" message="Test message">
          <location file="test.xml"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Issue element missing required 'id' attribute")
  }

  @Test
  fun testParseLintReport_missingRequiredAttributes_throwsException() {
    val xmlContent =
      """
      $XML_HEADER
        <issue message="Test message">
          <location file="test.xml"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Issue element missing required 'id' attribute")
  }

  @Test
  fun testParseLintReport_issueWithoutLocationElements_throwsException() {
    val xmlContent =
      """
      $XML_HEADER
        <issue id="TestIssue" severity="Warning" message="Test message">
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Issue element must contain at least one location")
  }

  @Test
  fun testParseLintReport_issueWithEmptyErrorLines_parsesCorrectly() {
    val xmlContent =
      """
      $XML_HEADER
        <issue
            id="EmptyErrorLinesTest"
            severity="Warning"
            message="Test issue with empty error lines"
            category="Test"
            priority="5"
            summary="Test summary"
            explanation="Test explanation">
            <location file="test.xml"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.errorLine1).isEmpty()
    assertThat(issue.errorLine2).isEmpty()
    assertThat(issue.id).isEqualTo("EmptyErrorLinesTest")
    assertThat(issue.message).isEqualTo("Test issue with empty error lines")
  }

  @Test
  fun testLintSeverity_fromString_validSeverities_returnsCorrectEnum() {
    assertThat(LintSeverity.fromString("Fatal")).isEqualTo(LintSeverity.FATAL)
    assertThat(LintSeverity.fromString("Error")).isEqualTo(LintSeverity.ERROR)
    assertThat(LintSeverity.fromString("Warning")).isEqualTo(LintSeverity.WARNING)
    assertThat(LintSeverity.fromString("Information"))
      .isEqualTo(LintSeverity.INFORMATION)
  }

  @Test
  fun testLintSeverity_fromString_caseInsensitive_returnsCorrectEnum() {
    assertThat(LintSeverity.fromString("fatal")).isEqualTo(LintSeverity.FATAL)
    assertThat(LintSeverity.fromString("ERROR")).isEqualTo(LintSeverity.ERROR)
    assertThat(LintSeverity.fromString("warning")).isEqualTo(LintSeverity.WARNING)
    assertThat(LintSeverity.fromString("INFORMATION"))
      .isEqualTo(LintSeverity.INFORMATION)
  }

  @Test
  fun testLintSeverity_fromString_invalidSeverity_throwsException() {
    val exception = assertThrows<IllegalArgumentException> {
      LintSeverity.fromString("InvalidSeverity")
    }

    assertThat(exception).hasMessageThat().contains("Unknown severity level: InvalidSeverity")
  }

  @Test
  fun testParseLintReport_mixedSeverityIssues_parsesAllCorrectly() {
    val xmlContent =
      """
      $XML_HEADER
        <issue id="Fatal1" severity="Fatal" message="Fatal issue">
          <location file="file1.xml" line="1"/>
        </issue>
        <issue id="Error1" severity="Error" message="Error issue"> 
          <location file="file2.xml" line="2"/>
        </issue>
        <issue id="Warning1" severity="Warning" message="Warning issue">
          <location file="file3.xml" line="3"/>
        </issue>
        <issue id="Info1" severity="Information" message="Info issue">
          <location file="file4.xml" line="4"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(4)
    assertThat(issues[0].severity).isEqualTo(LintSeverity.FATAL)
    assertThat(issues[1].severity).isEqualTo(LintSeverity.ERROR)
    assertThat(issues[2].severity).isEqualTo(LintSeverity.WARNING)
    assertThat(issues[3].severity).isEqualTo(LintSeverity.INFORMATION)
  }

  @Test
  fun testParseLintReport_issueWithMissingOptionalAttributes_usesEmptyStrings() {
    val xmlContent =
      """
      $XML_HEADER
        <issue id="MinimalIssue" severity="Warning" message="Minimal issue">
          <location file="test.xml" line="1"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.category).isEmpty()
    assertThat(issue.priority).isEmpty()
    assertThat(issue.summary).isEmpty()
    assertThat(issue.explanation).isEmpty()
    assertThat(issue.errorLine1).isEmpty()
    assertThat(issue.errorLine2).isEmpty()
  }

  @Test
  fun testParseLintReport_invalidRootElement_throwsException() {
    val xmlContent =
      """
      <wrongRoot format="6" by="lint 7.3.1">
        <issue id="TestIssue" severity="Warning" message="Test">
          <location file="test.xml"/>
        </issue>
      </wrongRoot>
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Invalid lint report format: expected root element 'issues'")
  }

  @Test
  fun testParseLintReport_cacheHit_returnsCachedResult() {
    val xmlContent = createXmlWithIssues(warningIssue)
    val xmlFile = createXmlFile(xmlContent)

    val issues1 = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    // Should return cached result
    val issues2 = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues1).hasSize(1)
    assertThat(issues2).hasSize(1)
    assertThat(issues1[0].id).isEqualTo(issues2[0].id)
    assertThat(issues1[0].severity).isEqualTo(issues2[0].severity)
  }

  @Test
  fun testParseLintReport_cacheMiss_afterFileModification() {
    val originalContent = createXmlWithIssues(warningIssue)
    val xmlFile = createXmlFile(originalContent)

    val issues1 = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    val modifiedContent = createXmlWithIssues(errorIssue)
    xmlFile.writeText(modifiedContent)

    // Should detect file change and reparse
    val issues2 = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues1).hasSize(1)
    assertThat(issues2).hasSize(1)
    assertThat(issues1[0].id).isEqualTo("UsesMinSdkAttributes")
    assertThat(issues2[0].id).isEqualTo("NewApi")
  }

  @Test
  fun testParseLintReport_invalidSeverity_throwsException() {
    val xmlContent =
      """
      $XML_HEADER
        <issue id="TestIssue" severity="InvalidSeverity" message="Test message">
          <location file="test.xml"/>
        </issue>
      $XML_FOOTER
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Unknown severity level: InvalidSeverity")
  }

  @Test
  fun testPrintLintReport_groupBySeverity_printsCorrectFormat() {
    val issues = listOf(warningIssue, informationIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = true)
    val output = outputStream.toString()

    assertThat(output).contains("${YELLOW}Warning: 1${RESET}")
    assertThat(output).contains("${YELLOW}Information: 1${RESET}")
    assertThat(output).contains("${BOLD}Total Issues: 2${RESET}")

    assertThat(output).contains("=".repeat(60))
    assertThat(output).contains("${BOLD}${YELLOW} SEVERITY: WARNING (1 issues)${RESET}")
    assertThat(output).contains("${BOLD}${YELLOW} SEVERITY: INFORMATION (1 issues)${RESET}")

    assertThat(output).contains("$BOLD Issue ID: UsesMinSdkAttributes${RESET}")
    assertThat(output).contains("${YELLOW}Severity: Warning${RESET}")
    assertThat(output).contains("  File: src/main/AndroidManifest.xml")
    assertThat(output).contains("  Line: 5")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED${RESET}")
  }

  @Test
  fun testPrintLintReport_groupByFile_printsCorrectFormat() {
    val issues = listOf(warningIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    assertThat(output).contains("${YELLOW}Warning: 1${RESET}")
    assertThat(output).contains("${BOLD}Total Issues: 1${RESET}")

    assertThat(output).contains("=".repeat(80))
    assertThat(output).contains("${BOLD}FILE: src/main/AndroidManifest.xml (1 issues)${RESET}")

    assertThat(output).contains("$BOLD Issue #1: UsesMinSdkAttributes${RESET}")
    assertThat(output).contains("${YELLOW}Severity: Warning${RESET}")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED${RESET}")
  }

  @Test
  fun testPrintLintReport_withCriticalIssues_failsLintCheck() {
    val issues = listOf(errorIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    }
    val output = outputStream.toString()

    assertThat(output).contains("${RED}Error: 1${RESET}")
    assertThat(output).contains("${BOLD}Total Issues: 1${RESET}")
    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED${RESET}")
  }

  @Test
  fun testPrintLintReport_onlyWarnings_passesLintCheck() {
    val issues = listOf(warningIssue, informationIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    assertThat(output).contains("${YELLOW}Warning: 1${RESET}")
    assertThat(output).contains("${YELLOW}Information: 1${RESET}")
    assertThat(output).contains("${BOLD}Total Issues: 2${RESET}")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED${RESET}")
    assertThat(output).doesNotContain("FAILED")
  }

  @Test
  fun testPrintLintReport_multipleLocationsIssue_groupBySeverity_printsAllLocations() {
    val issues = listOf(multiLocationIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = true)
    val output = outputStream.toString()

    assertThat(output).contains("  Locations:")
    assertThat(output).contains("    1. File: app/src/main/res/values-night/color_palette.xml")
    assertThat(output).contains("       Line: 159")

    assertThat(output).contains("    2. File: app/src/main/res/values/color_palette.xml")
    assertThat(output).contains("       Line: 164")
    assertThat(output).contains("$BOLD Issue ID: UnusedResources${RESET}")
    assertThat(output).contains("${YELLOW}Severity: Warning${RESET}")
  }

  @Test
  fun testPrintLintReport_multipleLocationsIssue_groupByFile_printsAllFiles() {
    val issues = listOf(multiLocationIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    assertThat(output)
      .contains("${BOLD}FILE: app/src/main/res/values/color_palette.xml (1 issues)${RESET}")
    assertThat(output)
      .contains("${BOLD}FILE: app/src/main/res/values-night/color_palette.xml (1 issues)${RESET}")

    assertThat(output).contains("$BOLD Issue #1: UnusedResources${RESET}")
    assertThat(output).contains("${YELLOW}Severity: Warning${RESET}")
  }

  @Test
  fun testPrintLintReport_issueWithErrorLines_groupBySeverity_printsErrorLines() {
    val issues = listOf(errorIssue)

     assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = true)
    }
    val output = outputStream.toString()

    assertThat(output).contains("  Error Line: ${errorIssue.errorLine1}")
    assertThat(output).contains("              ${errorIssue.errorLine2}")

    assertThat(output).contains("  Category: ${errorIssue.category}")
    assertThat(output).contains("  Priority: ${errorIssue.priority}")
    assertThat(output).contains("  Summary: ${errorIssue.summary}")
    assertThat(output).contains("  Message: ${errorIssue.message}")
    assertThat(output).contains("  Explanation: ${errorIssue.explanation}")
  }

  @Test
  fun testPrintLintReport_issueWithErrorLines_groupByFile_printsErrorLines() {
    val issues = listOf(errorIssue)

    assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    }
    val output = outputStream.toString()

    assertThat(output).contains("  Error Line: ${errorIssue.errorLine1}")
    assertThat(output).contains("              ${errorIssue.errorLine2}")

    assertThat(output).contains("  Category: ${errorIssue.category}")
    assertThat(output).contains("  Priority: ${errorIssue.priority}")
    assertThat(output).contains("  Summary: ${errorIssue.summary}")
    assertThat(output).contains("  Message: ${errorIssue.message}")
    assertThat(output).contains("  Explanation: ${errorIssue.explanation}")
  }

  @Test
  fun testPrintLintReport_issueWithoutErrorLines_groupBySeverity_skipsErrorLines() {
    val issues = listOf(informationIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = true)
    val output = outputStream.toString()

    assertThat(output).doesNotContain("Error Line:")

    assertThat(output).contains("$BOLD Issue ID: IidCompatibilityCheckFailure${RESET}")
    assertThat(output).contains("${YELLOW}Severity: Information${RESET}")
  }

  @Test
  fun testPrintLintReport_issueWithoutErrorLines_groupByFile_skipsErrorLines() {
    val issues = listOf(informationIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    assertThat(output).doesNotContain("Error Line:")

    assertThat(output).contains("${BOLD} Issue #1: IidCompatibilityCheckFailure${RESET}")
    assertThat(output).contains("${YELLOW}Severity: Information${RESET}")
  }

  @Test
  fun testPrintLintReport_emptyIssuesList_groupBySeverity_printsZeroSummary() {
    val issues = emptyList<LintIssue>()

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = true)
    val output = outputStream.toString()

    assertThat(output).contains("${BOLD}Total Issues: 0${RESET}")

    assertThat(output).doesNotContain("Error:")
    assertThat(output).doesNotContain("Warning:")
    assertThat(output).doesNotContain("Information:")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED${RESET}")
  }

  @Test
  fun testPrintLintReport_emptyIssuesList_groupByFile_printsZeroSummary() {
    val issues = emptyList<LintIssue>()

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    assertThat(output).contains("${BOLD}Total Issues: 0${RESET}")

    assertThat(output).doesNotContain("Error:")
    assertThat(output).doesNotContain("Warning:")
    assertThat(output).doesNotContain("Information:")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED${RESET}")
  }

  @Test
  fun testPrintLintReport_severityOrdering_printsInCorrectOrder() {
    val fatalIssue = errorIssue.copy(
      id = "FatalIssue",
      severity = LintSeverity.FATAL
    )
    val issues = listOf(informationIssue, warningIssue, errorIssue, fatalIssue)

     assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = true)
    }
    val output = outputStream.toString()

    val fatalPos = output.indexOf("SEVERITY: FATAL")
    val errorPos = output.indexOf("SEVERITY: ERROR")
    val warningPos = output.indexOf("SEVERITY: WARNING")
    val infoPos = output.indexOf("SEVERITY: INFORMATION")

    assertThat(fatalPos).isLessThan(errorPos)
    assertThat(errorPos).isLessThan(warningPos)
    assertThat(warningPos).isLessThan(infoPos)

    assertThat(output).contains("${BOLD}${RED} SEVERITY: FATAL")
  }

  @Test
  fun testPrintLintReport_fileGroupingSorting_sortsFilesByName() {
    val issue1 = warningIssue.copy(
      locations = listOf(LintLocation("z_file.xml", "10"))
    )
    val issue2 = warningIssue.copy(
      id = "AnotherWarning",
      locations = listOf(LintLocation("a_file.kt", "20"))
    )
    val issues = listOf(issue1, issue2)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    val aFilePos = output.indexOf("FILE: a_file.kt")
    val zFilePos = output.indexOf("FILE: z_file.xml")

    assertThat(aFilePos).isLessThan(zFilePos)
  }

  @Test
  fun testPrintLintReport_multipleIssuesInSameFile_sortsCorrectly() {
    val issue1 = warningIssue.copy(
      id = "Warning50",
      locations = listOf(LintLocation("same_file.kt", "50"))
    )
    val issue2 = warningIssue.copy(
      id = "Warning10",
      locations = listOf(LintLocation("same_file.kt", "10"))
    )
    val issues = listOf(issue1, issue2)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    val line10Pos = output.indexOf("Line: 10")
    val line50Pos = output.indexOf("Line: 50")

    assertThat(line10Pos).isLessThan(line50Pos)
  }

  @Test
  fun testPrintLintReport_singleLocationIssue_groupBySeverity_printsFileAndLine() {
    val issues = listOf(warningIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = true)
    val output = outputStream.toString()

    assertThat(output).contains("  File: src/main/AndroidManifest.xml")
    assertThat(output).contains("  Line: 5")
    assertThat(output).doesNotContain("Locations:")
  }

  @Test
  fun testPrintLintReport_singleLocationIssue_groupByFile_printsLine() {
    val issues = listOf(warningIssue)

    lintAnalysisReporter.printLintReport(issues, groupByIssueSeverity = false)
    val output = outputStream.toString()

    assertThat(output).contains("  Line: 5")
    assertThat(output).contains("${BOLD}FILE: src/main/AndroidManifest.xml (1 issues)${RESET}")
  }

  private fun createXmlFile(content: String, fileName: String = "lint-report.xml"): File {
    val xmlFile = tempFolder.newFile(fileName)
    xmlFile.writeText(content)
    return xmlFile
  }

  private fun createXmlWithIssues(vararg issues: LintIssue): String {
    val issueElements = issues.joinToString("\n") { issue ->
      val locationElements = issue.locations.joinToString("\n") { location ->
        "<location file=\"${escapeXml(location.file)}\" line=\"${location.lineNumber}\"/>"
      }

      val errorLine1Attr = if (issue.errorLine1.isNotEmpty())
        "errorLine1=\"${escapeXml(issue.errorLine1)}\"" else ""
      val errorLine2Attr = if (issue.errorLine2.isNotEmpty())
        "errorLine2=\"${escapeXml(issue.errorLine2)}\"" else ""

      """
        <issue
            id="${issue.id}"
            severity="${issue.severity.displayName}"
            message="${escapeXml(issue.message)}"
            category="${issue.category}"
            priority="${issue.priority}"
            summary="${escapeXml(issue.summary)}"
            explanation="${escapeXml(issue.explanation)}"
            $errorLine1Attr
            $errorLine2Attr>
            $locationElements
        </issue>
      """.trimIndent()
    }

    return """
      $XML_HEADER
        $issueElements
      $XML_FOOTER
    """.trimIndent()
  }

  private fun escapeXml(text: String): String {
    return text
      .replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&apos;")
  }
}
