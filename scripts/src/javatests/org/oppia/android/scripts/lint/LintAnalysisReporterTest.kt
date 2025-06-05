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
    severity = "Warning",
    message = "Manifest should specify a minimum API level",
    category = "Correctness",
    priority = "9",
    summary = "Minimum SDK and target SDK attributes not defined",
    explanation = "The manifest should contain a uses-sdk element",
    locations = listOf(
      LintLocation(
        file = "src/main/AndroidManifest.xml",
        lineNumber = "5"
      )
    )
  )
  private val errorIssue = LintIssue(
    id = "NewApi",
    severity = "Error",
    message = "Call requires API level 24 (current min is 19)",
    category = "Correctness",
    priority = "6",
    summary = "Calling new methods on older versions",
    explanation = "This check scans through all the Android API calls",
    locations = listOf(
      LintLocation(
        file = "src/main/java/MainActivity.kt",
        lineNumber = "42"
      )
    )
  )
  private val informationIssue = LintIssue(
    id = "IidCompatibilityCheckFailure",
    severity = "Information",
    message = "Check failed with exception: java.lang.NoSuchMethodException",
    category = "Lint",
    priority = "1",
    summary = "Firebase IID Compatibility Check Unable To Run",
    explanation = "The check failed to run as it encountered unknown failure.",
    locations = listOf(
      LintLocation(
        file = "/home/manas-yu/oppia-android/app",
        lineNumber = ""
      )
    )
  )
  private val multiLocationIssue = LintIssue(
    id = "UnusedResources",
    severity = "Warning",
    message = "The resource `R.color.color_palette_save_button_border_color` appears to be unused",
    category = "Performance",
    priority = "3",
    summary = "Unused resources",
    explanation = "Unused resources make applications larger and slow down builds.",
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
    assertThat(issues[0].severity).isEqualTo("Warning")
    assertThat(issues[1].id).isEqualTo(errorIssue.id)
    assertThat(issues[1].severity).isEqualTo("Error")
  }

  @Test
  fun testParseLintReport_issueWithMultipleLocations_parsesAllLocations() {
    val xmlContent = createXmlWithIssues(multiLocationIssue)
    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.id).isEqualTo("UnusedResources")
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

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Error processing file")
  }

  @Test
  fun testParseLintReport_issueWithSpecialCharacters_handlesCorrectly() {
    val specialCharsIssue = LintIssue(
      id = "SpecialCharsTest",
      severity = "Error",
      message = "Message with <special> & characters \"quoted\"",
      category = "Test",
      priority = "1",
      summary = "Summary with special chars",
      explanation = "Explanation with <tags> and &amp; symbols",
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
            explanation="Explanation with &lt;tags&gt; and &amp;amp; symbols">
            <location file="path/with spaces/file&amp;name.xml" line="15"/>
        </issue>
      </issues>
      """.trimIndent()

    val xmlFile = createXmlFile(xmlContent)
    val issues = lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)

    assertThat(issues).hasSize(1)
    val issue = issues[0]
    assertThat(issue.message).isEqualTo(specialCharsIssue.message)
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

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Issue element is missing required attributes or locations")
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

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Issue element is missing required attributes or locations")
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

    val exception = assertThrows<IllegalStateException> {
      lintAnalysisReporter.parseLintReport(xmlFile.absolutePath)
    }

    assertThat(exception).hasMessageThat()
      .contains("Issue element is missing required attributes or locations")
  }

  private fun createXmlFile(content: String, fileName: String = "lint-report.xml"): File {
    val xmlFile = tempFolder.newFile(fileName)
    xmlFile.writeText(content)
    return xmlFile
  }

  private fun createXmlWithIssues(vararg issues: LintIssue): String {
    val issueElements = issues.joinToString("\n") { issue ->
      val locationElements = issue.locations.joinToString("\n") { location ->
        "<location file=\"${location.file}\" line=\"${location.lineNumber}\"/>"
      }

      """
        <issue
            id="${issue.id}"
            severity="${issue.severity}"
            message="${issue.message}"
            category="${issue.category}"
            priority="${issue.priority}"
            summary="${issue.summary}"
            explanation="${issue.explanation}">
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

  private fun createMinimalXmlWithIssue(
    id: String = "TestIssue",
    severity: String = "Warning",
    file: String = "test.xml"
  ): String {
    return """
      <issues format="6" by="lint 7.3.1">
        <issue id="$id" severity="$severity" message="Test message">
          <location file="$file"/>
        </issue>
      </issues>
    """.trimIndent()
  }
}
