package org.oppia.android.scripts.xml

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.oppia.android.scripts.common.RepositoryFile
import org.w3c.dom.Node
import org.w3c.dom.NodeList

class StringResourceParser(private val repoRoot: File) {
  private val translations by lazy { parseTranslations() }
  private val documentBuilderFactory by lazy { DocumentBuilderFactory.newInstance() }

  enum class TranslationLanguage(val valuesDirectoryName: String) {
    ARABIC(valuesDirectoryName = "values-ar"),
    BRAZILIAN_PORTUGUESE(valuesDirectoryName = "values-pt-rBR"),
    ENGLISH(valuesDirectoryName = "values"),
    SWAHILI(valuesDirectoryName = "values-sw");
  }

  data class StringFile(
    val language: TranslationLanguage, val file: File, val strings: Map<String, String>
  )

  fun retrieveBaseStringFile(): StringFile = retrieveTranslations(TranslationLanguage.ENGLISH)

  fun retrieveBaseStringNames(): Set<String> = retrieveBaseStringFile().strings.keys

  fun retrieveTranslations(language: TranslationLanguage): StringFile =
    translations.getValue(language)

  fun retrieveAllNonEnglishTranslations(): Map<TranslationLanguage, StringFile> =
    translations.filter { (language, _) -> language != TranslationLanguage.ENGLISH }

  private fun parseTranslations(): Map<TranslationLanguage, StringFile> {
    // A list of all XML files in the repo to be analyzed.
    val stringFiles = RepositoryFile.collectSearchFiles(
      repoPath = repoRoot.absolutePath,
      expectedExtension = ".xml"
    ).filter {
      it.relativeTo(repoRoot).startsWith("app/") && it.nameWithoutExtension == "strings"
    }.associateBy {
      checkNotNull(it.parentFile?.name?.let(::findTranslationLanguage)) {
        "Parent directory of ${it.relativeTo(repoRoot)} does not correspond to a known" +
          " translation: ${it.parentFile?.name}"
      }
    }
    val expectedLanguages = TranslationLanguage.values().toSet()
    check(expectedLanguages == stringFiles.keys) {
      "Missing translation strings for language(s): ${expectedLanguages - stringFiles.keys}"
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

  private companion object {

    private fun DocumentBuilderFactory.parseXmlFile(file: File) = newDocumentBuilder().parse(file)

    private fun Node.getChildSequence() = childNodes.asSequence()

    private fun NodeList.asSequence() = (0 until length).asSequence().map(this::item)

    private fun findTranslationLanguage(valuesDirectoryName: String) =
      TranslationLanguage.values().find { it.valuesDirectoryName == valuesDirectoryName }
  }
}
