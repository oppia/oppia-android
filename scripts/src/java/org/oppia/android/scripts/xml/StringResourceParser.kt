package org.oppia.android.scripts.xml

import org.oppia.android.scripts.common.RepositoryFile
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Parser and processor for all UI-facing string resources, for use in validation and analysis
 * scripts.
 *
 * @property repoRoot the root of the Oppia Android repository being processed
 */
class StringResourceParser(private val repoRoot: File) {
  private val supportedLanguages by lazy { parseSupportedLanguages() }
  private val translations by lazy { parseTranslations() }
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }

  /** Returns the [StringFile] corresponding to the base (i.e. untranslated English) strings. */
  fun retrieveBaseStringFile(): StringFile {
    return translations.entries.single { it.key.name == "ENGLISH" }.value
  }

  /** Returns the [Set] of all string keys contained within the base strings file. */
  fun retrieveBaseStringNames(): Set<String> = retrieveBaseStringFile().strings.keys

  /**
   * Returns a map of all [StringFile]s (keyed by their [StringFile.language]) which represent
   * actual translations (i.e. all non-base files--see [retrieveBaseStringFile] for the base
   * strings).
   */
  fun retrieveAllNonEnglishTranslations(): Map<TranslationLanguage, StringFile> =
    translations.filter { (language, _) -> language.name != "ENGLISH" }

  private fun parseTranslations(): Map<TranslationLanguage, StringFile> {
    // A list of all XML files in the repo to be analyzed.
    val stringFiles = RepositoryFile.collectSearchFiles(
      repoPath = repoRoot.absolutePath,
      expectedExtension = ".xml"
    ).filter {
      it.toRelativeString(repoRoot).startsWith("app/") && it.nameWithoutExtension == "strings"
    }.associateBy { file ->
      val dirName = file.parentFile?.name
      checkNotNull(
        supportedLanguages.find { it.valuesDirectoryName == dirName }
      ) {
        "Strings file '${file.toRelativeString(repoRoot)}'" +
          "does not correspond to a known language:" +
          " $dirName"
      }
    }.toSortedMap(compareBy { it.name }) // Sorted by language name for consistent output.

    val expectedLanguages = supportedLanguages.toSet()
    val foundLanguages = stringFiles.keys
    check(foundLanguages.containsAll(expectedLanguages)) {
      "Missing translation strings for language(s):" +
        " ${(expectedLanguages - foundLanguages).joinToString { it.name } }"
    }
    return stringFiles.map { (language, file) ->
      language to StringFile(language, file, file.parseStrings())
    }.toMap()
  }

  private fun File.parseStrings(): Map<String, String> {
    val manifestDocument = documentBuilderFactory.parseXmlFile(this)
    val stringsElem = manifestDocument.getChildSequence().single { it.nodeName == "resources" }
    val stringElems = stringsElem.getChildSequence().filter { it.nodeName == "string" }
    return stringElems.associate {
      checkNotNull(it.attributes.getNamedItem("name")?.nodeValue) to checkNotNull(it.textContent)
    }
  }

  /** Uses the supported_languages.textproto file to parse the list of supported languages. */
  private fun parseSupportedLanguages(): List<TranslationLanguage> {
    val configFile = File(
      repoRoot,
      "config/src/java/org/oppia/android/config/alllanguages/supported_languages.textproto"
    )
    val content = configFile.readText()
    val definitions = content.split("language_definitions {")
    return definitions.drop(1).mapNotNull { block ->
      val name = Regex("language:\\s*(\\w+)").find(block)?.groupValues?.get(1)
        ?: return@mapNotNull null
      if (!block.contains("app_string_id")) return@mapNotNull null

      val langCode = Regex("language_code:\\s*\"(\\w+)\"").find(block)?.groupValues?.get(1)
        ?: return@mapNotNull null

      val regionCode = Regex("region_code:\\s*\"(\\w+)\"").find(block)?.groupValues?.get(1)

      val directoryName = if (name == "ENGLISH") {
        "values"
      } else {
        val suffix = if (regionCode != null) "-r$regionCode" else ""
        "values-$langCode$suffix"
      }
      TranslationLanguage(name, directoryName)
    }
  }

  /**
   * The language given strings have been translated to/are being represented in.
   *
   * @property name the name of the language (e.g. ARABIC, ENGLISH)
   * @property valuesDirectoryName the name of the resource values directory that is expected to
   *     contain a strings.xml file for strings related to this language
   */
  data class TranslationLanguage(val name: String, val valuesDirectoryName: String)

  /**
   * A record of a specific set of translations corresponding to one language.
   *
   * @property language the language of this string file
   * @property file the direct [File] to the strings.xml containing the translations
   * @property strings a map with keys of string names and values of the actual strings retrieved
   *     from the strings.xml file
   */
  data class StringFile(
    val language: TranslationLanguage,
    val file: File,
    val strings: Map<String, String>
  )

  private companion object {
    private fun DocumentBuilderFactory.parseXmlFile(file: File) = newDocumentBuilder().parse(file)

    private fun Node.getChildSequence() = childNodes.asSequence()

    private fun NodeList.asSequence() = (0 until length).asSequence().map(this::item)
  }
}
