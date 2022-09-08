package org.oppia.android.scripts.xml

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.xml.StringResourceParser.TranslationLanguage.ARABIC
import org.oppia.android.scripts.xml.StringResourceParser.TranslationLanguage.BRAZILIAN_PORTUGUESE
import org.oppia.android.scripts.xml.StringResourceParser.TranslationLanguage.ENGLISH
import org.oppia.android.scripts.xml.StringResourceParser.TranslationLanguage.SWAHILI
import org.oppia.android.testing.assertThrows
import org.w3c.dom.Document
import org.xml.sax.SAXParseException
import java.io.File
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

/** Tests for [StringResourceParser]. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
class StringResourceParserTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private companion object {
    private val ARABIC_STRINGS = mapOf(
      "shared_string" to "مشغل رحلة الاستكشاف",
      "arabic_only_string" to "خيارات"
    )

    private val BRAZILIAN_PORTUGUESE_STRINGS = mapOf(
      "shared_string" to "Meus Downloads",
      "brazilian_portuguese_only_string" to "Reprodutor de Exploração"
    )

    private val ENGLISH_STRINGS = mapOf(
      "shared_string" to "Exploration Player",
      "english_only_string" to "Help"
    )

    private val SWAHILI_STRINGS = mapOf(
      "shared_string" to "Kicheza Ugunduzi",
      "swahili_only_string" to "Badili Wasifu"
    )
  }

  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }
  private val transformerFactory by lazy { TransformerFactory.newInstance() }

  private lateinit var appResources: File
  private lateinit var utilityResources: File

  @Before
  fun setUp() {
    // Ensure there are directories for string resources.
    appResources = tempFolder.newFolder("app", "src", "main", "res")
    utilityResources = tempFolder.newFolder("utility", "src", "main", "res")
  }

  @Test
  fun testRetrieveBaseStringFile_noStrings_throwsException() {
    val parser = StringResourceParser(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) { parser.retrieveBaseStringFile() }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Missing translation strings for language(s): ARABIC, BRAZILIAN_PORTUGUESE, ENGLISH," +
          " SWAHILI"
      )
  }

  @Test
  fun testRetrieveBaseStringFile_noBaseEnglishStrings_throwsException() {
    populateArabicTranslations()
    populateBrazilianPortugueseTranslations()
    populateSwahiliTranslations()
    val parser = StringResourceParser(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) { parser.retrieveBaseStringFile() }

    assertThat(exception)
      .hasMessageThat()
      .contains("Missing translation strings for language(s): ENGLISH")
  }

  @Test
  fun testRetrieveBaseStringFile_noArabicStrings_throwsException() {
    populateBrazilianPortugueseTranslations()
    populateEnglishTranslations()
    populateSwahiliTranslations()
    val parser = StringResourceParser(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) { parser.retrieveBaseStringFile() }

    assertThat(exception)
      .hasMessageThat()
      .contains("Missing translation strings for language(s): ARABIC")
  }

  @Test
  fun testRetrieveBaseStringFile_noBrazilianPortugueseStrings_throwsException() {
    populateArabicTranslations()
    populateEnglishTranslations()
    populateSwahiliTranslations()
    val parser = StringResourceParser(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) { parser.retrieveBaseStringFile() }

    assertThat(exception)
      .hasMessageThat()
      .contains("Missing translation strings for language(s): BRAZILIAN_PORTUGUESE")
  }

  @Test
  fun testRetrieveBaseStringFile_noSwahiliStrings_throwsException() {
    populateArabicTranslations()
    populateBrazilianPortugueseTranslations()
    populateEnglishTranslations()
    val parser = StringResourceParser(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) { parser.retrieveBaseStringFile() }

    assertThat(exception)
      .hasMessageThat()
      .contains("Missing translation strings for language(s): SWAHILI")
  }

  @Test
  fun testRetrieveBaseStringFile_extraStringsDirectory_throwsException() {
    populateAllAppTranslations()
    populateTranslations(appResources, "values-fake", mapOf())
    val parser = StringResourceParser(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) { parser.retrieveBaseStringFile() }

    assertThat(exception)
      .hasMessageThat()
      .contains(
        "Strings file 'app/src/main/res/values-fake/strings.xml' does not correspond to a known" +
          " language: values-fake"
      )
  }

  @Test
  fun testRetrieveBaseStringFile_stringsOutsideAppDirectory_areIgnored() {
    populateArabicTranslations()
    populateBrazilianPortugueseTranslations()
    populateSwahiliTranslations()
    populateTranslations(utilityResources, "values", mapOf())
    val parser = StringResourceParser(tempFolder.root)

    val exception = assertThrows(IllegalStateException::class) { parser.retrieveBaseStringFile() }

    // An exception is still thrown since resources outside the app directory are ignored.
    assertThat(exception)
      .hasMessageThat()
      .contains("Missing translation strings for language(s): ENGLISH")
  }

  @Test
  fun testRetrieveBaseStringFile_allStringsPresent_baseStringsInvalidXml_throwsException() {
    populateArabicTranslations()
    populateBrazilianPortugueseTranslations()
    populateSwahiliTranslations()
    writeTranslationsFile(appResources, "values", "<bad XML>")
    val parser = StringResourceParser(tempFolder.root)

    assertThrows(SAXParseException::class) { parser.retrieveBaseStringFile() }
  }

  @Test
  fun testRetrieveBaseStringFile_allStringsPresentAndValid_returnsBaseStringFile() {
    populateAllAppTranslations()
    val parser = StringResourceParser(tempFolder.root)

    val stringFile = parser.retrieveBaseStringFile()

    assertThat(stringFile.language).isEqualTo(ENGLISH)
    assertThat(stringFile.file.toRelativeString(tempFolder.root))
      .isEqualTo("app/src/main/res/values/strings.xml")
    assertThat(stringFile.strings).containsExactlyEntriesIn(ENGLISH_STRINGS)
  }

  @Test
  fun testRetrieveBaseStringNames_allStringsPresentAndValid_returnsBaseStringNames() {
    populateAllAppTranslations()
    val parser = StringResourceParser(tempFolder.root)

    val stringNames = parser.retrieveBaseStringNames()

    assertThat(stringNames).containsExactly("shared_string", "english_only_string")
  }

  @Test
  fun retrieveAllNonEnglishTranslations_allStringsPresentAndValid_returnsNonEnglishStringFiles() {
    populateAllAppTranslations()
    val parser = StringResourceParser(tempFolder.root)

    val nonEnglishTranslations = parser.retrieveAllNonEnglishTranslations()

    assertThat(nonEnglishTranslations).hasSize(3)
    assertThat(nonEnglishTranslations).containsKey(ARABIC)
    assertThat(nonEnglishTranslations).containsKey(BRAZILIAN_PORTUGUESE)
    assertThat(nonEnglishTranslations).containsKey(SWAHILI)
    assertThat(nonEnglishTranslations).doesNotContainKey(ENGLISH) // Only non-English are included.
    val arFile = nonEnglishTranslations[ARABIC]
    assertThat(arFile?.language).isEqualTo(ARABIC)
    assertThat(arFile?.file?.toRelativeString(tempFolder.root))
      .isEqualTo("app/src/main/res/values-ar/strings.xml")
    assertThat(arFile?.strings).containsExactlyEntriesIn(ARABIC_STRINGS)
    val ptBrFile = nonEnglishTranslations[BRAZILIAN_PORTUGUESE]
    assertThat(ptBrFile?.language).isEqualTo(BRAZILIAN_PORTUGUESE)
    assertThat(ptBrFile?.file?.toRelativeString(tempFolder.root))
      .isEqualTo("app/src/main/res/values-pt-rBR/strings.xml")
    assertThat(ptBrFile?.strings).containsExactlyEntriesIn(BRAZILIAN_PORTUGUESE_STRINGS)
    val swFile = nonEnglishTranslations[SWAHILI]
    assertThat(swFile?.language).isEqualTo(SWAHILI)
    assertThat(swFile?.file?.toRelativeString(tempFolder.root))
      .isEqualTo("app/src/main/res/values-sw/strings.xml")
    assertThat(swFile?.strings).containsExactlyEntriesIn(SWAHILI_STRINGS)
  }

  private fun populateAllAppTranslations() {
    populateArabicTranslations()
    populateBrazilianPortugueseTranslations()
    populateEnglishTranslations()
    populateSwahiliTranslations()
  }

  private fun populateArabicTranslations() {
    populateTranslations(appResources, "values-ar", ARABIC_STRINGS)
  }

  private fun populateBrazilianPortugueseTranslations() {
    populateTranslations(appResources, "values-pt-rBR", BRAZILIAN_PORTUGUESE_STRINGS)
  }

  private fun populateEnglishTranslations() {
    populateTranslations(appResources, "values", ENGLISH_STRINGS)
  }

  private fun populateSwahiliTranslations() {
    populateTranslations(appResources, "values-sw", SWAHILI_STRINGS)
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
}
