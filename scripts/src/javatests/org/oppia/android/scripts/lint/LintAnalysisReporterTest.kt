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
    message = "The resource `R.color.color_palette_save_button_border_color` appears to be unused",
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
      <issues format="6" by="lint 7.3.1">
      </issues>
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
  fun testParseLintReport_nonExistentFile_throwsException() {
    val nonExistentPath = "/path/that/does/not/exist/lint-report.xml"

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(nonExistentPath)
    }

    assertThat(exception).hasMessageThat().contains("Lint report file not found: $nonExistentPath")
  }

  @Test
  fun testParseLintReport_malformedXml_throwsError() {
    val malformedXml =
      """
      <issues format="6" by="lint 7.3.1">
        <issue id="TestIssue" severity="Warning"
          <!-- Missing closing tag -->
      </issues>
      """.trimIndent()

    val xmlFile = createXmlFile(malformedXml)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Error processing file")
  }

  @Test
  fun testParseLintReport_issueWithSpecialCharacters_handlesCorrectly() {
    val specialCharsIssue = LintIssue(
      id = "SpecialCharsTest",
      severity = LintSeverity.ERROR,
      message = "Message with <special> & characters \"quoted\"",
      category = "Test",
      priority = "1",
      summary = "Summary with special chars",
      explanation = "Explanation with <tags> and &amp; symbols",
      errorLine1 = "val text = \"Hello <world> & friends\"",
      errorLine2 = "           ~~~~~~~~~~~~~~~~~~~~~~",
      locations = listOf(
        LintLocation(
          file = "path/with spaces/file&name.xml",
          lineNumber = "15"
        )
      )
    )

    val xmlContent =
      """
      <issues format="6" by="lint 7.3.1">
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
      </issues>
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.message).isEqualTo(specialCharsIssue.message)
    assertThat(issue.errorLine1).isEqualTo(specialCharsIssue.errorLine1)
    assertThat(issue.errorLine2).isEqualTo(specialCharsIssue.errorLine2)
    assertThat(issue.locations).hasSize(1)
    assertThat(issue.locations[0].file).isEqualTo(specialCharsIssue.locations[0].file)
    assertThat(issue.locations[0].lineNumber).isEqualTo(specialCharsIssue.locations[0].lineNumber)
  }

  @Test
  fun testParseLintReport_emptyRequiredAttributes_throwsException() {
    val xmlContent =
      """
      <issues format="6" by="lint 7.3.1">
        <issue id="" severity="" message="Test message">
          <location file=""/>
        </issue>
      </issues>
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
      <issues format="6" by="lint 7.3.1">
        <issue message="Test message">
          <location file="test.xml"/>
        </issue>
      </issues>
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
      <issues format="6" by="lint 7.3.1">
        <issue id="TestIssue" severity="Warning" message="Test message">
        </issue>
      </issues>
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
    val issueWithEmptyErrorLines = LintIssue(
      id = "EmptyErrorLinesTest",
      severity = LintSeverity.WARNING,
      message = "Test issue with empty error lines",
      category = "Test",
      priority = "5",
      summary = "Test summary",
      explanation = "Test explanation",
      errorLine1 = "",
      errorLine2 = "",
      locations = listOf(
        LintLocation(
          file = "test.xml",
          lineNumber = "10"
        )
      )
    )

    val xmlContent = createXmlWithIssues(issueWithEmptyErrorLines)
    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.errorLine1).isEmpty()
    assertThat(issue.errorLine2).isEmpty()
    assertThat(issue.id).isEqualTo("EmptyErrorLinesTest")
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
  fun testParseLintReport_invalidSeverity_throwsException() {
    val xmlContent =
      """
      <issues format="6" by="lint 7.3.1">
        <issue id="TestIssue" severity="InvalidSeverity" message="Test message">
          <location file="test.xml"/>
        </issue>
      </issues>
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)

    val exception = assertThrows<IllegalArgumentException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Unknown severity level: InvalidSeverity")
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
      <issues format="6" by="lint 7.3.1">
        $issueElements
      </issues>
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
