package org.oppia.android.scripts.xml

import com.google.protobuf.MessageLite
import org.oppia.android.app.model.LanguageDefinition
import org.oppia.android.app.model.SupportedLanguages
import org.w3c.dom.Element
import java.io.File
import java.io.FileInputStream
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

  /**
   * Retrieves all supported languages from the configuration file.
   */
  private fun retrieveAllLanguages(): List<LanguageDefinition> {
    return loadProto(
      "supported_languages.pb",
      SupportedLanguages.getDefaultInstance()
    ).languageDefinitionsList
  }

  /** Returns the [StringFile] corresponding to the base (i.e. untranslated English) strings. */
  fun retrieveBaseStringFile(): StringFile {
    val englishDef = retrieveAllLanguages().find { it.language.name == "ENGLISH" }
      ?: error("English language definition not found.")
    return translations.getValue(englishDef.language.name)
  }

  /** Returns the [Set] of all string keys contained within the base strings file. */
  fun retrieveBaseStringNames(): Set<String> = retrieveBaseStringFile().strings.keys

  /**
   * Returns a map of all [StringFile]s (keyed by their language ID) which represent
   * actual translations (i.e. all non-base files).
   */
  fun retrieveAllNonEnglishTranslations(): Map<String, StringFile> {
    return translations.filter { (langId, _) -> langId != "ENGLISH" }
  }

  private fun parseTranslations(): Map<String, StringFile> {
    val languageDefinitions = retrieveAllLanguages()
    val directoryToLanguageMap = languageDefinitions.associateBy {
      computeAndroidValuesDirectory(it)
    }

    val collectedFiles = collectedSearchFiles(repoRoot)

    val stringFiles = collectedFiles.filter {
      it.toRelativeString(repoRoot).startsWith("app/") && it.name == "strings.xml"
    }.mapNotNull { file ->
      val parentDirName = file.parentFile?.name
      val languageDef = directoryToLanguageMap[parentDirName]

      if (languageDef != null) {
        languageDef.language.name to StringFile(languageDef, file, parseStrings(file))
      } else {
        null
      }
    }.toMap().toSortedMap()

    val foundLanguageIds = stringFiles.keys
    val expectedLanguageIds = languageDefinitions.filter {
      it.appStringId.hasAndroidResourcesLanguageId()
    }.map { it.language.name }.toSet()
    return stringFiles
  }

  private fun parseStrings(file: File): Map<String, String> {
    val documentBuilder = documentBuilderFactory.newDocumentBuilder()
    val manifestDocument = documentBuilder.parse(file)
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

  private fun computeAndroidValuesDirectory(definition: LanguageDefinition): String {
    if (!definition.appStringId.hasAndroidResourcesLanguageId()) {
      return "contents-only-${definition.language.name}"
    }

    val androidId = definition.appStringId.androidResourcesLanguageId
    val code = androidId.languageCode
    val region = androidId.regionCode

    return when {
      code == "en" && region.isEmpty() -> "values"
      region.isNotEmpty() -> "values-$code-r$region"
      else -> "values-$code"
    }
  }

  /**
   * A record of a specific set of translations corresponding to one language.
   */
  data class StringFile(
    val languageDefinition: LanguageDefinition,
    val file: File,
    val strings: Map<String, String>
  )

  private fun collectedSearchFiles(root: File): Sequence<File> {
    return root.walk().filter { it.isFile }
  }

  private companion object {
    private fun <T : MessageLite> loadProto(fileName: String, defaultInstance: T): T {
      val protoPath = "config/src/java/org/oppia/android/config/alllanguages/$fileName"
      val protoFile = File(protoPath)
      val builder = defaultInstance.newBuilderForType()
      FileInputStream(protoFile).use { inputStream ->
        builder.mergeFrom(inputStream)
      }
      @Suppress("UNCHECKED_CAST")
      return builder.build() as T
    }
  }
}
