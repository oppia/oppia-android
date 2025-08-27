package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.AndroidLintExemption
import org.oppia.android.scripts.proto.AndroidLintExemptions
import org.oppia.android.scripts.proto.LintIssueId
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
  private val pathToProtoBinary = "scripts/assets/android_lint_exemptions.pb"
  private lateinit var repoRoot: File
  private lateinit var warningIssue: LintIssue
  private lateinit var errorIssue: LintIssue
  private lateinit var informationIssue: LintIssue
  private lateinit var multiLocationIssue: LintIssue

  @Before
  fun setUp() {
    lintAnalysisReporter = LintAnalysisReporter()
    tempFolder.newFolder("scripts", "assets")
    tempFolder.newFile(pathToProtoBinary)
    repoRoot = tempFolder.root

    warningIssue = LintIssue(
      id = "UnusedResources",
      severity = LintSeverity.WARNING,
      message = "The resource appears to be unused",
      category = "Performance",
      explanation = "Unused resources make applications larger",
      errorLine1 = "<color name=\"unused_color\">#FF0000</color>",
      errorLine2 = "",
      locations = listOf(
        LintLocation(
          "${repoRoot.absolutePath}/app/src/main/res/values/colors.xml", "5"
        )
      )
    )

    errorIssue = LintIssue(
      id = "NewApi",
      severity = LintSeverity.ERROR,
      message = "Call requires API level 23",
      category = "Correctness",
      explanation = "This API is not available in older versions",
      errorLine1 = "context.getSystemService(JobScheduler::class.java)",
      errorLine2 = "",
      locations = listOf(
        LintLocation("${repoRoot.absolutePath}/app/src/main/java/MainActivity.kt", "42")
      )
    )

    informationIssue = LintIssue(
      id = "IidCompatibilityCheckFailure",
      severity = LintSeverity.INFORMATION,
      message = "Check failed with exception: java.lang.NoSuchMethodException",
      category = "Lint",
      explanation = "The check failed to run as it encountered unknown failure.",
      errorLine1 = "",
      errorLine2 = "",
      locations = listOf(
        LintLocation(
          file = "${repoRoot.absolutePath}/app/src/main/test.xml",
          lineNumber = ""
        )
      )
    )

    multiLocationIssue = LintIssue(
      id = "DuplicateStrings",
      severity = LintSeverity.WARNING,
      message = "Duplicate string value",
      category = "Correctness",
      explanation = "String literals should not be duplicated",
      errorLine1 = "<string name=\"hello\">Hello World</string>",
      errorLine2 = "",
      locations = listOf(
        LintLocation(
          "${repoRoot.absolutePath}/app/src/main/res/values/strings.xml", "10"
        ),
        LintLocation(
          "${repoRoot.absolutePath}/src/main/res/values-es/strings.xml", "8"
        )
      )
    )
    System.setOut(PrintStream(outputStream))
  }

  @After
  fun tearDown() {
    System.setOut(originalOut)
  }

  companion object {
    private const val XML_HEADER =
      """<issues format="6" by="lint 7.3.1">"""
    private const val XML_FOOTER =
      """</issues>"""
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
    assertThat(issue.explanation).isEqualTo(warningIssue.explanation)
    assertThat(issue.errorLine1).isEqualTo(warningIssue.errorLine1)
    assertThat(issue.errorLine2).isEqualTo(warningIssue.errorLine2)
    assertThat(issue.locations).hasSize(1)
    assertThat(issue.locations[0].file).isEqualTo(warningIssue.locations[0].file)
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
    assertThat(issue.id).isEqualTo(multiLocationIssue.id)
    assertThat(issue.errorLine1).isEqualTo(multiLocationIssue.errorLine1)
    assertThat(issue.errorLine2).isEqualTo(multiLocationIssue.errorLine2)
    assertThat(issue.locations).hasSize(2)

    assertThat(issue.locations[0].file)
      .isEqualTo(multiLocationIssue.locations[0].file)
    assertThat(issue.locations[0].lineNumber).isEqualTo(multiLocationIssue.locations[0].lineNumber)

    assertThat(issue.locations[1].file)
      .isEqualTo(multiLocationIssue.locations[1].file)
    assertThat(issue.locations[1].lineNumber).isEqualTo(multiLocationIssue.locations[1].lineNumber)
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
    assertThat(issues[0].locations[0].file).isEqualTo(informationIssue.locations.first().file)
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
    assertThat(issues1[0].id).isEqualTo(warningIssue.id)
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

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("${YELLOW}Warning: 1$RESET")
    assertThat(output).contains("${YELLOW}Information: 1$RESET")
    assertThat(output).contains("${BOLD}Total Issues: 2$RESET")

    assertThat(output).contains("=".repeat(60))
    assertThat(output).contains("${BOLD}$YELLOW SEVERITY: WARNING (1 issues)$RESET")
    assertThat(output).contains("${BOLD}$YELLOW SEVERITY: INFORMATION (1 issues)$RESET")

    assertThat(output).contains(
      "$BOLD Issue 1 of 1: ${toUpperSnakeCase(warningIssue.id)} " +
        "(Category: Performance)$RESET"
    )
    assertThat(output).contains("SEVERITY: WARNING (1 issues)")
    assertThat(output).contains("  File: ${warningIssue.locations[0].file}")
    assertThat(output).contains("  Line: 5")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_groupByFile_printsCorrectFormat() {
    val issues = listOf(warningIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("${YELLOW}Warning: 1$RESET")
    assertThat(output).contains("${BOLD}Total Issues: 1$RESET")

    assertThat(output).contains("=".repeat(80))
    assertThat(output).contains("${BOLD}FILE: ${warningIssue.locations[0].file} (1 issue)$RESET")

    assertThat(output).contains(
      "$BOLD Issue 1 of 1: ${toUpperSnakeCase(warningIssue.id)}" +
        " (Category: Performance)$RESET"
    )
    assertThat(output).contains("${YELLOW}Severity: Warning$RESET")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_withCriticalIssues_failsLintCheck() {
    val issues = listOf(errorIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("${RED}Error: 1$RESET")
    assertThat(output).contains("${BOLD}Total Issues: 1$RESET")
    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_onlyWarnings_failsLintCheck() {
    val issues = listOf(warningIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("${YELLOW}Warning: 1$RESET")
    assertThat(output).contains("${BOLD}Total Issues: 1$RESET")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
    assertThat(output).doesNotContain("PASSED")
  }

  @Test
  fun testPrintLintReport_onlyInformation_passesLintCheck() {
    val issues = listOf(informationIssue)

    lintAnalysisReporter.printLintReport(
      issues, reportUnusedEnum = false, groupByIssueSeverity = false
    )

    val output = outputStream.toString()

    assertThat(output).contains("${YELLOW}Information: 1$RESET")
    assertThat(output).contains("${BOLD}Total Issues: 1$RESET")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
    assertThat(output).doesNotContain("FAILED")
  }

  @Test
  fun testPrintLintReport_multipleLocationsIssue_groupBySeverity_printsAllLocations() {
    val issues = listOf(multiLocationIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("  Locations:")
    assertThat(output).contains("    1. File: ${multiLocationIssue.locations[0].file}")
    assertThat(output).contains("       Line: 10")

    assertThat(output).contains("    2. File: ${multiLocationIssue.locations[1].file}")
    assertThat(output).contains("       Line: 8")
    assertThat(output).contains(
      "$BOLD Issue 1 of 1: ${toUpperSnakeCase(multiLocationIssue.id)}" +
        " (Category: Correctness)$RESET"
    )
    assertThat(output).contains("SEVERITY: WARNING (1 issues)")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_multipleLocationsIssue_groupByFile_printsAllFiles() {
    val issues = listOf(multiLocationIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    assertThat(output)
      .contains("${BOLD}FILE: ${multiLocationIssue.locations[0].file} (1 issue)$RESET")
    assertThat(output)
      .contains("${BOLD}FILE: ${multiLocationIssue.locations[1].file} (1 issue)$RESET")

    assertThat(output).contains(
      "$BOLD Issue 1 of 1: ${toUpperSnakeCase(multiLocationIssue.id)}" +
        " (Category: Correctness)$RESET"
    )
    assertThat(output).contains("${YELLOW}Severity: Warning$RESET")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_issueWithErrorLines_groupBySeverity_printsErrorLines() {
    val issues = listOf(errorIssue)

    assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("  Error Line: ${errorIssue.errorLine1}")

    assertThat(output).contains("Category: ${errorIssue.category}")
    assertThat(output).contains("  Message: ${errorIssue.message}")
    assertThat(output).contains("Explanation:\n")
    assertThat(output).contains("This API is not available in older versions")
  }

  @Test
  fun testPrintLintReport_issueWithErrorLines_groupByFile_printsErrorLines() {
    val issues = listOf(errorIssue)

    assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("  Error Line: ${errorIssue.errorLine1}")

    assertThat(output).contains("(Category: ${errorIssue.category})")
    assertThat(output).contains("  Message: ${errorIssue.message}")
    assertThat(output).contains("  Explanation:\n")
    assertThat(output).contains(errorIssue.explanation)
  }

  @Test
  fun testPrintLintReport_issueWithoutErrorLines_groupBySeverity_skipsErrorLines() {
    val issues = listOf(informationIssue)

    lintAnalysisReporter.printLintReport(
      issues, reportUnusedEnum = false, groupByIssueSeverity = true
    )
    val output = outputStream.toString()

    assertThat(output).doesNotContain("Error Line:")

    assertThat(output).contains(
      "$BOLD Issue 1 of 1: " +
        "IID_COMPATIBILITY_CHECK_FAILURE (Category: Lint)$RESET"
    )
    assertThat(output).contains("SEVERITY: INFORMATION (1 issues)")
  }

  @Test
  fun testPrintLintReport_issueWithoutErrorLines_groupByFile_skipsErrorLines() {
    val issues = listOf(informationIssue)

    lintAnalysisReporter.printLintReport(
      issues, reportUnusedEnum = false, groupByIssueSeverity = false
    )
    val output = outputStream.toString()

    assertThat(output).doesNotContain("Error Line:")

    assertThat(output).contains(
      "$BOLD Issue 1 of 1: IID_COMPATIBILITY_CHECK_FAILURE" +
        " (Category: Lint)$RESET"
    )
    assertThat(output).contains("${YELLOW}Severity: Information$RESET")
  }

  @Test
  fun testPrintLintReport_falsePositiveIssue_printsWorkaround() {
    val issues = listOf(
      LintIssue(
        id = "NewApi",
        severity = LintSeverity.ERROR,
        message = "Call requires API level 24 (current min is 21): `java.lang.Iterable#forEach`",
        category = "Correctness",
        explanation = "This check scans through all the Android API " +
          "calls in the application and warns about any calls that are not available",
        errorLine1 = "          component.getAnalyticsStartupListenerStartupListeners().forEach {",
        errorLine2 = "                                                                  ~~~~~~~",
        locations = listOf(
          LintLocation(
            file = "${repoRoot.absolutePath}/app/src/main/java/MainActivity.kt",
            lineNumber = "42"
          )
        )
      )
    )
    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("$BOLD Issue 1 of 1: NEW_API (Category: Correctness)$RESET")
    assertThat(output).contains("${RED}Severity: Error$RESET (FALSE POSITIVE)")
    assertThat(output).contains("Line: 42")
    assertThat(output).contains(
      "Error Line:           " +
        "component.getAnalyticsStartupListenerStartupListeners().forEach {"
    )
    assertThat(output).contains(
      "Message: Call requires API level 24 " +
        "(current min is 21): `java.lang.Iterable#forEach`"
    )
    assertThat(output).contains(
      "Workaround: Use safeForEach from IterableExtensions.kt" +
        " instead of directly calling forEach to avoid known lint false positives on API < 24."
    )
    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_emptyIssuesList_groupBySeverity_printsZeroSummary() {
    val issues = emptyList<LintIssue>()

    lintAnalysisReporter.printLintReport(
      issues, reportUnusedEnum = false, groupByIssueSeverity = true
    )
    val output = outputStream.toString()

    assertThat(output).contains("${BOLD}Total Issues: 0$RESET")

    assertThat(output).doesNotContain("Error:")
    assertThat(output).doesNotContain("Warning:")
    assertThat(output).doesNotContain("Information:")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
  }

  @Test
  fun testPrintLintReport_emptyIssuesList_groupByFile_printsZeroSummary() {
    val issues = emptyList<LintIssue>()

    lintAnalysisReporter.printLintReport(
      issues, reportUnusedEnum = false, groupByIssueSeverity = false
    )
    val output = outputStream.toString()

    assertThat(output).contains("${BOLD}Total Issues: 0$RESET")

    assertThat(output).doesNotContain("Error:")
    assertThat(output).doesNotContain("Warning:")
    assertThat(output).doesNotContain("Information:")

    assertThat(output).contains("${GREEN}ANDROID LINT CHECK ${BOLD}PASSED$RESET")
  }

  @Test
  fun testPrintLintReport_severityOrdering_printsInCorrectOrder() {
    val fatalIssue = errorIssue.copy(
      id = "FatalIssue",
      severity = LintSeverity.FATAL
    )
    val issues = listOf(informationIssue, warningIssue, errorIssue, fatalIssue)

    assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()

    val fatalPos = output.indexOf("SEVERITY: FATAL")
    val errorPos = output.indexOf("SEVERITY: ERROR")
    val warningPos = output.indexOf("SEVERITY: WARNING")
    val infoPos = output.indexOf("SEVERITY: INFORMATION")

    assertThat(fatalPos).isLessThan(errorPos)
    assertThat(errorPos).isLessThan(warningPos)
    assertThat(warningPos).isLessThan(infoPos)

    assertThat(output).contains("${BOLD}$RED SEVERITY: FATAL")
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

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    val aFilePos = output.indexOf("FILE: a_file.kt")
    val zFilePos = output.indexOf("FILE: z_file.xml")

    assertThat(aFilePos).isLessThan(zFilePos)

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
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

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    val line10Pos = output.indexOf("Line: 10")
    val line50Pos = output.indexOf("Line: 50")

    assertThat(line10Pos).isLessThan(line50Pos)

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_severityGrouping_printsAllIssuesSeparately() {
    val issue1 = warningIssue.copy(
      id = "SameIssueId",
      message = "First issue message",
      locations = listOf(LintLocation("test_file.kt", "10"))
    )
    val issue2 = warningIssue.copy(
      id = "SameIssueId",
      message = "Second issue message",
      locations = listOf(LintLocation("test_file.kt", "10"))
    )
    val issue3 = warningIssue.copy(
      id = "SameIssueId",
      message = "Third issue message",
      locations = listOf(LintLocation("another_file.kt", "5"))
    )
    val issues = listOf(issue1, issue2, issue3)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("First issue message")
    assertThat(output).contains("Second issue message")
    assertThat(output).contains("Third issue message")

    val anotherFilePos = output.indexOf("File: another_file.kt")
    val testFilePos = output.indexOf("File: test_file.kt")
    assertThat(anotherFilePos).isLessThan(testFilePos)

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_unknownIssueId_printsIssueInfo() {
    val issues = listOf(warningIssue.copy(
      id = "UnknownIssueId",
      message = "First issue message",
      category = "Unknown",
      explanation= "This is an explanation for the unknown issue.",
      locations = listOf(LintLocation("test_file.kt", "10"))
    ))

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()
    assertThat(output).contains("First issue message")
    assertThat(output).contains("This is an explanation for the unknown issue.")
    assertThat(output).contains("Issue 1 of 1: UNKNOWN_ISSUE_ID (Category: Unknown)")
    assertThat(output).contains("test_file.kt")
    assertThat(output).contains("Line: 10")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_withUnusedEnum_listsUnusedEnums() {
    val issues = listOf(warningIssue.copy(
      id = "UnknownIssueId",
      message = "First issue message",
      category = "Unknown",
      explanation= "This is an explanation for the unknown issue.",
      locations = listOf(LintLocation("test_file.kt", "10"))
    ))

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = true, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()
    assertThat(output).contains("${YELLOW}UNUSED ENUM MAPPINGS DETECTED:$RESET")
    assertThat(output).contains("The following issue IDs are defined in issueIdMapping " +
      "but no corresponding lint issues were found.")
    assertThat(output).doesNotContain("LintError -> ${toUpperSnakeCase("LintError")}")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_singleLocationIssue_groupBySeverity_printsFileAndLine() {
    val issues = listOf(warningIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = true
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("  File: ${warningIssue.locations[0].file}")
    assertThat(output).contains("  Line: 5")
    assertThat(output).doesNotContain("Locations:")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testPrintLintReport_singleLocationIssue_groupByFile_printsLine() {
    val issues = listOf(warningIssue)

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.printLintReport(
        issues, reportUnusedEnum = false, groupByIssueSeverity = false
      )
    }
    val output = outputStream.toString()

    assertThat(output).contains("  Line: 5")
    assertThat(output).contains("${BOLD}FILE: ${warningIssue.locations[0].file} (1 issue)$RESET")

    assertThat(exception.message)
      .isEqualTo("${RED}ANDROID LINT CHECK ${BOLD}FAILED$RESET")
  }

  @Test
  fun testFindRedundantExemptions_mixedValidAndRedundantExemptions_returnsCorrectRedundancies() {
    val issues = listOf(warningIssue, errorIssue)
    val exemptions = listOf(
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "app/src/main/res/values/colors.xml"
        addLintIssueId(LintIssueId.UNUSED_RESOURCES) // Valid exemption
        addLintIssueId(LintIssueId.DUPLICATE_STRINGS) // Redundant
      }.build(),
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "app/src/main/java/MainActivity.kt"
        addLintIssueId(LintIssueId.NEW_API) // Valid exemption
        addLintIssueId(LintIssueId.TYPOS) // Redundant
      }.build(),
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "nonexistent/file.xml"
        addLintIssueId(LintIssueId.UNUSED_RESOURCES) // Redundant (file doesn't exist)
      }.build()
    )

    val redundantExemptions =
      lintAnalysisReporter.findRedundantExemptions(issues, exemptions, repoRoot)

    assertThat(redundantExemptions).hasSize(3)
    assertThat(redundantExemptions["app/src/main/res/values/colors.xml"])
      .containsExactly("DuplicateStrings")
    assertThat(redundantExemptions["app/src/main/java/MainActivity.kt"])
      .containsExactly("Typos")
    assertThat(redundantExemptions["nonexistent/file.xml"]).containsExactly("UnusedResources")
  }

  @Test
  fun testLoadExemptionsProto_validProtoBinaryFile_loadsCorrectly() {
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")
    val exemptions = AndroidLintExemptions.newBuilder().apply {
      addAndroidLintExemption(
        AndroidLintExemption.newBuilder().apply {
          exemptedFilePath = "test/file.xml"
          addLintIssueId(LintIssueId.UNUSED_RESOURCES)
        }.build()
      )
    }.build()
    exemptions.writeTo(exemptionFile.outputStream())

    val loadedExemptions = lintAnalysisReporter
      .loadExemptionsProto("${tempFolder.root}/$pathToProtoBinary")

    assertThat(loadedExemptions.androidLintExemptionList).hasSize(1)
    assertThat(loadedExemptions.androidLintExemptionList[0].exemptedFilePath)
      .isEqualTo("test/file.xml")
    assertThat(loadedExemptions.androidLintExemptionList[0].lintIssueIdList)
      .containsExactly(LintIssueId.UNUSED_RESOURCES)
  }

  @Test
  fun testFilterExemptedIssues_multipleIssueIdsInSingleExemption_filtersAllMatching() {
    val issues = listOf(warningIssue, errorIssue)
    val exemptions = listOf(
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "app/src/main/res/values/colors.xml"
        addLintIssueId(LintIssueId.UNUSED_RESOURCES)
        addLintIssueId(LintIssueId.NEW_API)
      }.build()
    )

    val filteredIssues = lintAnalysisReporter.filterExemptedIssues(issues, exemptions, repoRoot)

    assertThat(filteredIssues).hasSize(1)
    assertThat(filteredIssues[0]).isEqualTo(errorIssue)
  }

  @Test
  fun testFilterExemptedIssues_issueWithMultipleLocations_notExemptedIfNoLocationMatches() {
    val issues = listOf(multiLocationIssue)
    val exemptions = listOf(
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "app/src/main/res/values/other.xml"
        addLintIssueId(LintIssueId.DUPLICATE_STRINGS)
      }.build()
    )

    val filteredIssues = lintAnalysisReporter.filterExemptedIssues(issues, exemptions, repoRoot)

    assertThat(filteredIssues).hasSize(1)
    assertThat(filteredIssues[0]).isEqualTo(multiLocationIssue)
  }

  @Test
  fun testFilterExemptedIssues_noExemptions_returnsAllIssues() {
    val issues = listOf(warningIssue, errorIssue)
    val exemptions = emptyList<AndroidLintExemption>()

    val filteredIssues = lintAnalysisReporter.filterExemptedIssues(issues, exemptions, repoRoot)

    assertThat(filteredIssues).hasSize(2)
    assertThat(filteredIssues).containsExactly(warningIssue, errorIssue)
  }

  @Test
  fun testFindRedundantExemptions_multipleRedundantExemptionsForSameFile_returnsSortedList() {
    val issues = listOf(warningIssue)
    val exemptions = listOf(
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "app/src/main/res/values/colors.xml"
        addLintIssueId(LintIssueId.UNUSED_RESOURCES)
        addLintIssueId(LintIssueId.NEW_API)
        addLintIssueId(LintIssueId.DUPLICATE_STRINGS)
      }.build()
    )

    val redundantExemptions = lintAnalysisReporter
      .findRedundantExemptions(issues, exemptions, repoRoot)

    assertThat(redundantExemptions).hasSize(1)
    assertThat(redundantExemptions["app/src/main/res/values/colors.xml"])
      .containsExactly("DuplicateStrings", "NewApi")
  }

  @Test
  fun testLogRedundantExemptions_withRedundantExemptions_printsFormattedOutput() {
    val redundantExemptions = mapOf(
      "file1.xml" to listOf("IssueA", "IssueB"),
      "file2.kt" to listOf("IssueC")
    )
    val outputStream = ByteArrayOutputStream()
    System.setOut(PrintStream(outputStream))

    lintAnalysisReporter.logRedundantExemptions(redundantExemptions)
    val output = outputStream.toString()

    assertThat(output).contains("Redundant exemptions")
    assertThat(output).contains(
      "Please remove them from scripts/assets/android_lint_exemptions.textproto"
    )
    assertThat(output).contains("File: file1.xml")
    assertThat(output).contains("  - ISSUE_A")
    assertThat(output).contains("  - ISSUE_B")
    assertThat(output).contains("File: file2.kt")
    assertThat(output).contains("  - ISSUE_C")
  }

  @Test
  fun testFilterExemptedIssues_withExemptions_returnsNonExemptedIssues() {
    val issue1 = warningIssue.copy(
      locations = listOf(
        LintLocation(
          "${repoRoot.absolutePath}/app/src/main/res/values/colors.xml", "5"
        )
      )
    )
    val issue2 = errorIssue.copy(
      locations = listOf(
        LintLocation(
          "${repoRoot.absolutePath}/app/src/main/java/MainActivity.kt", "42"
        )
      )
    )
    val issue3 = multiLocationIssue.copy(
      locations = listOf(
        LintLocation(
          "${repoRoot.absolutePath}/app/src/main/res/values/strings.xml", "10"
        ),
        LintLocation(
          "${repoRoot.absolutePath}/app/src/main/res/values-es/strings.xml", "15"
        )
      )
    )
    val issues = listOf(issue1, issue2, issue3)

    val exemptions = listOf(
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "app/src/main/res/values/colors.xml"
        addLintIssueId(LintIssueId.UNUSED_RESOURCES)
      }.build(),
      AndroidLintExemption.newBuilder().apply {
        exemptedFilePath = "app/src/main/res/values-es/strings.xml"
        addLintIssueId(LintIssueId.DUPLICATE_STRINGS)
      }.build()
    )

    val filteredIssues = lintAnalysisReporter.filterExemptedIssues(issues, exemptions, repoRoot)

    assertThat(filteredIssues).hasSize(1)
    assertThat(filteredIssues[0]).isEqualTo(issue2)
  }

  @Test
  fun testLogRedundantExemptions_emptyRedundantExemptions_printsNothing() {
    val redundantExemptions = emptyMap<String, List<String>>()

    lintAnalysisReporter.logRedundantExemptions(redundantExemptions)
    val output = outputStream.toString()

    assertThat(output).isEmpty()
  }

  @Test
  fun testLogRedundantExemptions_singleFileMultipleIssues_printsCorrectFormat() {
    val redundantExemptions = mapOf(
      "app/src/main/java/TestFile.kt" to listOf("UnusedResources", "NewApi", "Typos")
    )

    lintAnalysisReporter.logRedundantExemptions(redundantExemptions)
    val output = outputStream.toString()

    assertThat(output).contains(
      "${YELLOW}Redundant exemptions (no corresponding lint issues found):$RESET"
    )
    assertThat(output).contains(
      "Please remove them from scripts/assets/android_lint_exemptions.textproto"
    )
    assertThat(output).contains("${BOLD}File: app/src/main/java/TestFile.kt$RESET")
    assertThat(output).contains("  - UNUSED_RESOURCES")
    assertThat(output).contains("  - NEW_API")
    assertThat(output).contains("  - TYPOS")
  }

  @Test
  fun testLogRedundantExemptions_multipleFilesSortedAlphabetically_printsSortedOrder() {
    val redundantExemptions = mapOf(
      "z_file.xml" to listOf("IssueZ"),
      "a_file.kt" to listOf("IssueA"),
      "m_file.java" to listOf("IssueM")
    )

    lintAnalysisReporter.logRedundantExemptions(redundantExemptions)
    val output = outputStream.toString()

    val aFilePos = output.indexOf("File: a_file.kt")
    val mFilePos = output.indexOf("File: m_file.java")
    val zFilePos = output.indexOf("File: z_file.xml")

    assertThat(aFilePos).isLessThan(mFilePos)
    assertThat(mFilePos).isLessThan(zFilePos)
  }

  @Test
  fun testLogRedundantExemptions_customExemptionFilePath_printsCustomPath() {
    val redundantExemptions = mapOf(
      "test_file.kt" to listOf("TestIssue")
    )
    val customPath = "custom/path/to/exemptions.textproto"

    lintAnalysisReporter.logRedundantExemptions(redundantExemptions, customPath)
    val output = outputStream.toString()

    assertThat(output).contains("Please remove them from $customPath")
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

  private fun toUpperSnakeCase(input: String): String {
    return input
      .replace(Regex("([a-z])([A-Z])"), "$1_$2")
      .uppercase()
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
