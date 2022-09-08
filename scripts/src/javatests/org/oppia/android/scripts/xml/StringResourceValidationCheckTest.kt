package org.oppia.android.scripts.xml

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import org.w3c.dom.Document
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/** Tests for the string_resource_validation_check test. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class StringResourceValidationCheckTest {
  private companion object {
    private const val AR_STRING_NO_NEWLINES = "مساعدة"
    private const val AR_STRING_ONE_NEWLINE = "مساعدة\\n"
    private const val AR_STRING_TWO_NEWLINES = "\\nمساعدة\\n"

    private const val PT_BR_STRING_NO_NEWLINES = "Ajuda"
    private const val PT_BR_STRING_ONE_NEWLINE = "\\nAjuda"
    private const val PT_BR_STRING_TWO_NEWLINES = "\\nAjuda\\n"

    private const val EN_STRING_ONE_NEWLINE = "\\nHelp"

    private const val SW_STRING_NO_NEWLINES = "Msaada"
    private const val SW_STRING_ONE_NEWLINE = "\\nMsaada"
    private const val SW_STRING_TWO_NEWLINES = "\\nMsaada\\n"
  }

  @field:[Rule JvmField] var tempFolder = TemporaryFolder()

  private val originalOut: PrintStream = System.out
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }
  private val transformerFactory by lazy { TransformerFactory.newInstance() }

  private lateinit var outContent: ByteArrayOutputStream
  private lateinit var appResources: File

  @Before
  fun setUp() {
    outContent = ByteArrayOutputStream()
    appResources = tempFolder.newFolder("app", "src", "main", "res")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testScript_missingPath_throwsException() {
    val exception = assertThrows(IllegalArgumentException::class) { runScript(/* With no path. */) }

    assertThat(exception)
      .hasMessageThat()
      .contains("Expected: bazel run //scripts:string_resource_validation_check -- <repo_path>")
  }

  @Test
  fun testScript_validPath_noStringFiles_fails() {
    val exception = assertThrows(IllegalStateException::class) {
      runScript(tempFolder.root.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Missing translation strings for language(s)")
  }

  @Test
  fun testScript_allMatch_succeeds() {
    populateArabicTranslations(mapOf("str1" to AR_STRING_ONE_NEWLINE))
    populateBrazilianPortugueseTranslations(mapOf("str1" to PT_BR_STRING_ONE_NEWLINE))
    populateEnglishTranslations(mapOf("str1" to EN_STRING_ONE_NEWLINE))
    populateSwahiliTranslations(mapOf("str1" to SW_STRING_ONE_NEWLINE))

    runScript(tempFolder.root.absolutePath)

    assertThat(outContent.asString()).contains("STRING RESOURCE VALIDATION CHECKS PASSED")
  }

  @Test
  fun testScript_inconsistentLines_arabic_failsWithFindings() {
    populateArabicTranslations(
      mapOf("str1" to AR_STRING_NO_NEWLINES, "str2" to AR_STRING_TWO_NEWLINES)
    )
    populateBrazilianPortugueseTranslations(mapOf("str1" to PT_BR_STRING_ONE_NEWLINE))
    populateEnglishTranslations(
      mapOf("str1" to EN_STRING_ONE_NEWLINE, "str2" to EN_STRING_ONE_NEWLINE)
    )
    populateSwahiliTranslations(mapOf("str1" to SW_STRING_ONE_NEWLINE))

    val exception = assertThrows(Exception::class) { runScript(tempFolder.root.absolutePath) }

    // This output check also inadvertently verifies that the script doesn't care about missing
    // strings in translated string files.
    assertThat(exception).hasMessageThat().contains("STRING RESOURCE VALIDATION CHECKS FAILED")
    assertThat(outContent.asString().trim()).isEqualTo(
      """
      1 language(s) were found with string consistency errors.
      
      2 consistency error(s) were found for ARABIC strings (file: app/src/main/res/values-ar/strings.xml):
      - string str1: original translation uses 2 line(s) but translation uses 1 line(s). Please remove any extra lines or add any that are missing.
      - string str2: original translation uses 2 line(s) but translation uses 3 line(s). Please remove any extra lines or add any that are missing.
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_inconsistentLines_brazilianPortuguese_failsWithFindings() {
    populateArabicTranslations(mapOf("str1" to AR_STRING_ONE_NEWLINE))
    populateBrazilianPortugueseTranslations(
      mapOf("str1" to PT_BR_STRING_NO_NEWLINES, "str2" to PT_BR_STRING_TWO_NEWLINES)
    )
    populateEnglishTranslations(
      mapOf("str1" to EN_STRING_ONE_NEWLINE, "str2" to EN_STRING_ONE_NEWLINE)
    )
    populateSwahiliTranslations(mapOf("str1" to SW_STRING_ONE_NEWLINE))

    val exception = assertThrows(Exception::class) { runScript(tempFolder.root.absolutePath) }

    // This output check also inadvertently verifies that the script doesn't care about missing
    // strings in translated string files.
    assertThat(exception).hasMessageThat().contains("STRING RESOURCE VALIDATION CHECKS FAILED")
    assertThat(outContent.asString().trim()).isEqualTo(
      """
      1 language(s) were found with string consistency errors.
      
      2 consistency error(s) were found for BRAZILIAN_PORTUGUESE strings (file: app/src/main/res/values-pt-rBR/strings.xml):
      - string str1: original translation uses 2 line(s) but translation uses 1 line(s). Please remove any extra lines or add any that are missing.
      - string str2: original translation uses 2 line(s) but translation uses 3 line(s). Please remove any extra lines or add any that are missing.
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_inconsistentLines_swahili_failsWithFindings() {
    populateArabicTranslations(mapOf("str1" to AR_STRING_ONE_NEWLINE))
    populateBrazilianPortugueseTranslations(mapOf("str1" to PT_BR_STRING_ONE_NEWLINE))
    populateEnglishTranslations(
      mapOf("str1" to EN_STRING_ONE_NEWLINE, "str2" to EN_STRING_ONE_NEWLINE)
    )
    populateSwahiliTranslations(
      mapOf("str1" to SW_STRING_NO_NEWLINES, "str2" to SW_STRING_TWO_NEWLINES)
    )

    val exception = assertThrows(Exception::class) { runScript(tempFolder.root.absolutePath) }

    // This output check also inadvertently verifies that the script doesn't care about missing
    // strings in translated string files.
    assertThat(exception).hasMessageThat().contains("STRING RESOURCE VALIDATION CHECKS FAILED")
    assertThat(outContent.asString().trim()).isEqualTo(
      """
      1 language(s) were found with string consistency errors.
      
      2 consistency error(s) were found for SWAHILI strings (file: app/src/main/res/values-sw/strings.xml):
      - string str1: original translation uses 2 line(s) but translation uses 1 line(s). Please remove any extra lines or add any that are missing.
      - string str2: original translation uses 2 line(s) but translation uses 3 line(s). Please remove any extra lines or add any that are missing.
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_inconsistentLines_allLanguages_failsWithFindings() {
    populateArabicTranslations(
      mapOf("str1" to AR_STRING_NO_NEWLINES, "str2" to AR_STRING_TWO_NEWLINES)
    )
    populateBrazilianPortugueseTranslations(
      mapOf("str1" to PT_BR_STRING_NO_NEWLINES, "str2" to PT_BR_STRING_TWO_NEWLINES)
    )
    populateEnglishTranslations(
      mapOf("str1" to EN_STRING_ONE_NEWLINE, "str2" to EN_STRING_ONE_NEWLINE)
    )
    populateSwahiliTranslations(
      mapOf("str1" to SW_STRING_NO_NEWLINES, "str2" to SW_STRING_TWO_NEWLINES)
    )

    val exception = assertThrows(Exception::class) { runScript(tempFolder.root.absolutePath) }

    // This output check also inadvertently verifies that the script doesn't care about missing
    // strings in translated string files.
    assertThat(exception).hasMessageThat().contains("STRING RESOURCE VALIDATION CHECKS FAILED")
    assertThat(outContent.asString().trim()).isEqualTo(
      """
      3 language(s) were found with string consistency errors.
      
      2 consistency error(s) were found for ARABIC strings (file: app/src/main/res/values-ar/strings.xml):
      - string str1: original translation uses 2 line(s) but translation uses 1 line(s). Please remove any extra lines or add any that are missing.
      - string str2: original translation uses 2 line(s) but translation uses 3 line(s). Please remove any extra lines or add any that are missing.
      
      2 consistency error(s) were found for BRAZILIAN_PORTUGUESE strings (file: app/src/main/res/values-pt-rBR/strings.xml):
      - string str1: original translation uses 2 line(s) but translation uses 1 line(s). Please remove any extra lines or add any that are missing.
      - string str2: original translation uses 2 line(s) but translation uses 3 line(s). Please remove any extra lines or add any that are missing.
      
      2 consistency error(s) were found for SWAHILI strings (file: app/src/main/res/values-sw/strings.xml):
      - string str1: original translation uses 2 line(s) but translation uses 1 line(s). Please remove any extra lines or add any that are missing.
      - string str2: original translation uses 2 line(s) but translation uses 3 line(s). Please remove any extra lines or add any that are missing.
      """.trimIndent().trim()
    )
  }

  private fun runScript(vararg args: String) = main(*args)

  private fun populateArabicTranslations(strings: Map<String, String>) {
    populateTranslations(appResources, "values-ar", strings)
  }

  private fun populateBrazilianPortugueseTranslations(strings: Map<String, String>) {
    populateTranslations(appResources, "values-pt-rBR", strings)
  }

  private fun populateEnglishTranslations(strings: Map<String, String>) {
    populateTranslations(appResources, "values", strings)
  }

  private fun populateSwahiliTranslations(strings: Map<String, String>) {
    populateTranslations(appResources, "values-sw", strings)
  }

  private fun populateTranslations(
    resourceDir: File,
    valuesDirName: String,
    translations: Map<String, String>
  ) {
    val document = documentBuilderFactory.newDocumentBuilder().newDocument()
    val resourcesRoot = document.createElement("resources").also { document.appendChild(it) }
    translations.map { (name, value) ->
      document.createElement("string").also {
        it.setAttribute("name", name)
        it.textContent = value
      }
    }.forEach(resourcesRoot::appendChild)
    writeTranslationsFile(resourceDir, valuesDirName, document.toSource())
  }

  private fun writeTranslationsFile(resourceDir: File, valuesDirName: String, contents: String) {
    val valuesDir = File(resourceDir, valuesDirName).also { check(it.mkdir()) }
    File(valuesDir, "strings.xml").writeText(contents)
  }

  private fun Document.toSource(): String {
    // Reference: https://stackoverflow.com/a/5456836.
    val transformer = transformerFactory.newTransformer()
    return StringWriter().apply {
      transformer.transform(DOMSource(this@toSource), StreamResult(this@apply))
    }.toString()
  }

  private fun ByteArrayOutputStream.asString() = toString(Charsets.UTF_8.name())
}
