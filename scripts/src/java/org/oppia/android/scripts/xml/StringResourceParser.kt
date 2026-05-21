package org.oppia.android.scripts.xml

import com.google.protobuf.MessageLite
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.SupportedLanguages
import org.w3c.dom.Element
import java.io.File
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parser and processor for all UI-facing string resources, for use in validation and analysis
 * scripts.
 *
 * @property repoRoot the root of the Oppia Android repository being processed
 */
class StringResourceParser(private val repoRoot: File) {
  private val translations by lazy { parseTranslations() }
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }

  private val languageDefinitions by lazy {
    loadProto(
      "supported_languages.pb",
      SupportedLanguages.getDefaultInstance()
    ).languageDefinitionsList
  }

  /** Returns the [StringFile] corresponding to the base (i.e. untranslated English) strings. */
  fun retrieveBaseStringFile(): StringFile = translations.getValue(OppiaLanguage.ENGLISH)

  /** Returns the [Set] of all string keys contained within the base strings file. */
  fun retrieveBaseStringNames(): Set<String> = retrieveBaseStringFile().strings.keys

  /**
   * Returns a map of all [StringFile]s (keyed by their [OppiaLanguage]) which represent
   * actual translations (i.e. all non-base files--see [retrieveBaseStringFile] for the base
   * strings).
   */
  fun retrieveAllNonEnglishTranslations(): Map<OppiaLanguage, StringFile> =
    translations.filter { (language, _) -> language != OppiaLanguage.ENGLISH }

  private fun parseTranslations(): Map<OppiaLanguage, StringFile> {
    // Filter languages that actually have app string translations.
    val appLanguages = languageDefinitions.filter { it.appStringId.hasAndroidResourcesLanguageId() }

    val directoryToLanguageMap = appLanguages.associateBy {
      computeAndroidValuesDirectory(it)
    }

    // A list of all XML files in the repo to be analyzed.
    val stringFiles = collectedSearchFiles(repoRoot).filter {
      it.toRelativeString(repoRoot).startsWith("app/") && it.name == "strings.xml"
    }.mapNotNull { file ->
      val parentDirName = file.parentFile?.name
      val languageDef = directoryToLanguageMap[parentDirName]
      if (languageDef != null) {
        languageDef.language to StringFile(languageDef.language, file, file.parseStrings())
      } else {
        if (parentDirName?.startsWith("values") == true) {
          error("Strings file '${file.toRelativeString(repoRoot)}' does not correspond to a known language: $parentDirName")
        }
        null
      }
    }.toMap().toSortedMap() // Sorted for consistent output.

    val expectedLanguages = appLanguages.map { it.language }.toSet()
    check(expectedLanguages == stringFiles.keys) {
      "Missing translation strings for language(s):" +
        " ${(expectedLanguages - stringFiles.keys).joinToString() }"
    }
    return stringFiles
  }

  private fun File.parseStrings(): Map<String, String> {
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
    val manifestDocument = documentBuilder.parse(this)
    val root = manifestDocument.documentElement
    val stringElems = root.getElementsByTagName("string")

    val results = mutableMapOf<String, String>()
    for (i in 0 until stringElems.length) {
      val node = stringElems.item(i) as Element
      val name = node.getAttribute("name")
      val value = node.textContent
      if (name.isNotEmpty()) {
        results[name] = value
      }
    }
    return results
  }

  private fun computeAndroidValuesDirectory(definition: LanguageSupportDefinition): String {
    val androidId = definition.appStringId.androidResourcesLanguageId
    val code = androidId.languageCode
    val region = androidId.regionCode

    return when {
      code == "en" && region.isEmpty() -> "values"
      region.isEmpty() -> "values-$code"
      region.all { it.isDigit() } -> "values-b+$code+$region"
      else -> "values-$code-r$region"
    }
  }

  private fun collectedSearchFiles(root: File): Sequence<File> {
    return root.walk().filter { it.isFile }
  }

  /**
   * A record of a specific set of translations corresponding to one language.
   *
   * @property language the language of this string file
   * @property file the direct [File] to the strings.xml containing the translations
   * @property strings a map with keys of string names and values of the actual strings retrieved
   *     from the strings.xml file
   */
  data class StringFile(
    val language: OppiaLanguage,
    val file: File,
    val strings: Map<String, String>
  )

  private companion object {
    private fun <T : MessageLite> loadProto(fileName: String, defaultInstance: T): T {
      val resourcePath = "/config/src/java/org/oppia/android/config/alllanguages/$fileName"
      val inputStream: InputStream = StringResourceParser::class.java.getResourceAsStream(resourcePath)
        ?: error("Resource $resourcePath not found in classpath")
      val builder = defaultInstance.newBuilderForType()
      inputStream.use {
        builder.mergeFrom(it)
      }
      @Suppress("UNCHECKED_CAST")
      return builder.build() as T
    }
  }
}
