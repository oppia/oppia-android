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
import java.lang.IllegalArgumentException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/** Tests for the string_language_translation_check test. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class StringLanguageTranslationCheckTest {
  private companion object {
    private val ARABIC_STRINGS_SHARED = mapOf("shared_string" to "مشغل رحلة الاستكشاف")
    private val ARABIC_STRINGS_EXTRAS = mapOf("arabic_only_string" to "خيارات")

    private val BRAZILIAN_PORTUGUESE_STRINGS_SHARED = mapOf("shared_string" to "Meus Downloads")
    private val BRAZILIAN_PORTUGUESE_STRINGS_EXTRAS = mapOf(
      "brazilian_portuguese_only_string" to "Reprodutor de Exploração"
    )

    private val ENGLISH_STRINGS_SHARED = mapOf("shared_string" to "Exploration Player")
    private val ENGLISH_STRINGS_EXTRAS = mapOf("english_only_string" to "Help")

    private val SWAHILI_STRINGS_SHARED = mapOf("shared_string" to "Kicheza Ugunduzi")
    private val SWAHILI_STRINGS_EXTRAS = mapOf("swahili_only_string" to "Badili Wasifu")
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
      .contains("Expected: bazel run //scripts:string_language_translation_check -- <repo_path>")
  }

  @Test
  fun testScript_validPath_noStringFiles_throwsException() {
    val exception = assertThrows(IllegalStateException::class) {
      runScript(tempFolder.root.absolutePath)
    }

    assertThat(exception).hasMessageThat().contains("Missing translation strings for language(s)")
  }

  @Test
  fun testScript_presentTranslations_allMatch_outputsNoneFoundMissing() {
    populateArabicTranslations(ARABIC_STRINGS_SHARED)
    populateBrazilianPortugueseTranslations(BRAZILIAN_PORTUGUESE_STRINGS_SHARED)
    populateEnglishTranslations(ENGLISH_STRINGS_SHARED)
    populateSwahiliTranslations(SWAHILI_STRINGS_SHARED)

    runScript(tempFolder.root.absolutePath)

    assertThat(outContent.asString()).contains("0 translation(s) were found missing")
  }

  @Test
  fun testScript_presentTranslations_missingSomeArabic_outputsMissingTranslations() {
    populateArabicTranslations(ARABIC_STRINGS_EXTRAS)
    populateBrazilianPortugueseTranslations(BRAZILIAN_PORTUGUESE_STRINGS_SHARED)
    populateEnglishTranslations(ENGLISH_STRINGS_SHARED)
    populateSwahiliTranslations(SWAHILI_STRINGS_SHARED)

    runScript(tempFolder.root.absolutePath)

    assertThat(outContent.asString().trim()).isEqualTo(
      """
      1 translation(s) were found missing.
      
      Missing translations:
      ARABIC (1/1):
      - shared_string
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_presentTranslations_missingSomeBrazilianPortuguese_outputsMissingTranslations() {
    populateArabicTranslations(ARABIC_STRINGS_SHARED)
    populateBrazilianPortugueseTranslations(BRAZILIAN_PORTUGUESE_STRINGS_EXTRAS)
    populateEnglishTranslations(ENGLISH_STRINGS_SHARED)
    populateSwahiliTranslations(SWAHILI_STRINGS_SHARED)

    runScript(tempFolder.root.absolutePath)

    assertThat(outContent.asString().trim()).isEqualTo(
      """
      1 translation(s) were found missing.
      
      Missing translations:
      BRAZILIAN_PORTUGUESE (1/1):
      - shared_string
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_presentTranslations_missingSomeSwahili_outputsMissingTranslations() {
    populateArabicTranslations(ARABIC_STRINGS_SHARED)
    populateBrazilianPortugueseTranslations(BRAZILIAN_PORTUGUESE_STRINGS_SHARED)
    populateEnglishTranslations(ENGLISH_STRINGS_SHARED)
    populateSwahiliTranslations(SWAHILI_STRINGS_EXTRAS)

    runScript(tempFolder.root.absolutePath)

    assertThat(outContent.asString().trim()).isEqualTo(
      """
      1 translation(s) were found missing.
      
      Missing translations:
      SWAHILI (1/1):
      - shared_string
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_presentTranslations_missingMultiple_outputsMissingTranslationsWithTotalCount() {
    populateArabicTranslations(ARABIC_STRINGS_EXTRAS)
    populateBrazilianPortugueseTranslations(BRAZILIAN_PORTUGUESE_STRINGS_EXTRAS)
    populateEnglishTranslations(ENGLISH_STRINGS_SHARED + ENGLISH_STRINGS_EXTRAS)
    populateSwahiliTranslations(SWAHILI_STRINGS_EXTRAS)

    runScript(tempFolder.root.absolutePath)

    assertThat(outContent.asString().trim()).isEqualTo(
      """
      6 translation(s) were found missing.
      
      Missing translations:
      ARABIC (2/6):
      - shared_string
      - english_only_string
      
      BRAZILIAN_PORTUGUESE (2/6):
      - shared_string
      - english_only_string
      
      SWAHILI (2/6):
      - shared_string
      - english_only_string
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_presentTranslations_missingMultiple_someShared_outputsMissingXlationsWithCount() {
    populateArabicTranslations(ARABIC_STRINGS_SHARED + ARABIC_STRINGS_EXTRAS)
    populateBrazilianPortugueseTranslations(
      BRAZILIAN_PORTUGUESE_STRINGS_SHARED + BRAZILIAN_PORTUGUESE_STRINGS_EXTRAS
    )
    populateEnglishTranslations(ENGLISH_STRINGS_SHARED + ENGLISH_STRINGS_EXTRAS)
    populateSwahiliTranslations(SWAHILI_STRINGS_SHARED + SWAHILI_STRINGS_EXTRAS)

    runScript(tempFolder.root.absolutePath)

    assertThat(outContent.asString().trim()).isEqualTo(
      """
      3 translation(s) were found missing.
      
      Missing translations:
      ARABIC (1/3):
      - english_only_string
      
      BRAZILIAN_PORTUGUESE (1/3):
      - english_only_string
      
      SWAHILI (1/3):
      - english_only_string
      """.trimIndent().trim()
    )
  }

  @Test
  fun testScript_missingEnglishTranslations_outputsNoneFoundMissing() {
    populateArabicTranslations(ARABIC_STRINGS_SHARED)
    populateBrazilianPortugueseTranslations(BRAZILIAN_PORTUGUESE_STRINGS_SHARED)
    populateEnglishTranslations(mapOf())
    populateSwahiliTranslations(SWAHILI_STRINGS_SHARED)

    runScript(tempFolder.root.absolutePath)

    // No translations should be found missing if the string is everywhere except the base file.
    assertThat(outContent.asString()).contains("0 translation(s) were found missing")
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
