package org.oppia.android.scripts.build

import com.android.aapt.Resources.ConfigValue
import com.android.aapt.Resources.Entry
import com.android.aapt.Resources.Package
import com.android.aapt.Resources.ResourceTable
import com.android.aapt.Resources.Type
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.SupportedLanguages
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * Script for filtering unsupported languages out of Android app bundle resource tables.
 *
 * Usage:
 *   bazel run //scripts:filter_per_language_resources -- \\
 *     <path_to_input_module> <path_to_output_module>
 *
 * The input module is expected to be a well-formed AAB module zip archive, and include a
 * support_languages.pb asset to provide guidance on which languages should be kept.
 *
 * Arguments:
 * - path_to_input_module: path to the input AAB module zip to be processed.
 * - path_to_output_module: path to the output AAB module zip that will be written.
 *
 * Example:
 *   bazel run //scripts:filter_per_language_resources -- \\
 *     $(pwd)/bazel-bin/oppia_dev_raw_module.zip $(pwd)/oppia_dev_updated_module.zip
 */
fun main(vararg args: String) {
  require(args.size == 2) {
    "Usage: bazel run //scripts:filter_per_language_resources --" +
      " </absolute/path/to/input_module.zip:Path>" +
      " </absolute/path/to/output_module.zip:Path>"
  }
  FilterPerLanguageResources().filterPerLanguageResources(
    inputModuleZip = File(args[0]), outputModuleZip = File(args[1])
  )
}

private class FilterPerLanguageResources {
  /**
   * Filters the resources for the given input module & writes an updated copy of it to the
   * specified output module file.
   */
  fun filterPerLanguageResources(inputModuleZip: File, outputModuleZip: File) {
    val (resourceTable, supportedLanguages) = ZipFile(inputModuleZip).use { zipFile ->
      val resourceTableEntry = checkNotNull(zipFile.getEntry("resources.pb")) {
        "Expected resources.pb in input zip file: $inputModuleZip."
      }
      val supportedLangsEntry = checkNotNull(zipFile.getEntry("assets/supported_languages.pb")) {
        "Expected assets/supported_languages.pb in input zip file: $inputModuleZip."
      }
      val resourceTableData = zipFile.getInputStream(resourceTableEntry).readBytes()
      val supportedLanguages = zipFile.getInputStream(supportedLangsEntry).readBytes()
      ResourceTable.parseFrom(resourceTableData) to SupportedLanguages.parseFrom(supportedLanguages)
    }
    val pkg = resourceTable.packageList.single().also {
      check(it.packageName == "org.oppia.android") {
        "Expected Oppia package, not: ${it.packageName}."
      }
    }

    val allReferencedLanguageLocales =
      pkg.typeList.flatMap { it.entryList }
        .flatMap { it.configValueList }
        .map { it.config }
        .map { it.locale }
        .map { LanguageLocale.createFrom(it) }
        .toSet()
    val supportedLanguageLocales =
      supportedLanguages.languageDefinitionsList.mapNotNull {
        LanguageLocale.createFrom(it)
      }.toSet()
    val removedLanguageCodes =
      (allReferencedLanguageLocales - supportedLanguageLocales).sortedBy {
        it.androidBcp47QualifiedCode
      }
    val updatedResourceTable = resourceTable.recompute(supportedLanguageLocales)
    println(
      "${resourceTable.countResources() - updatedResourceTable.countResources()} resources are" +
        " being removed that are tied to unsupported languages: ${removedLanguageCodes.map {
          it.androidBcp47QualifiedCode
        } } (size reduction: ${
        resourceTable.serializedSize - updatedResourceTable.serializedSize
        } bytes)."
    )

    ZipOutputStream(outputModuleZip.outputStream()).use { outputStream ->
      ZipFile(inputModuleZip).use { zipFile ->
        zipFile.entries().asSequence().forEach { entry ->
          outputStream.putNextEntry(ZipEntry(entry.name))
          if (entry.name == "resources.pb") {
            updatedResourceTable.writeTo(outputStream)
          } else zipFile.getInputStream(entry).use { it.copyTo(outputStream) }
        }
      }
    }
  }

  private fun ResourceTable.recompute(allowedLanguageLocales: Set<LanguageLocale>): ResourceTable {
    val updatedPackages = packageList.mapNotNull { it.recompute(allowedLanguageLocales) }
    return toBuilder().apply {
      clearPackage()
      addAllPackage(updatedPackages)
    }.build()
  }

  private fun ResourceTable.countResources(): Int = packageList.sumOf { it.countResources() }

  private fun Package.recompute(allowedLanguageLocales: Set<LanguageLocale>): Package? {
    val updatedTypes = typeList.mapNotNull { it.recompute(allowedLanguageLocales) }
    return if (updatedTypes.isNotEmpty()) {
      toBuilder().apply {
        clearType()
        addAllType(updatedTypes)
      }.build()
    } else null
  }

  private fun Package.countResources(): Int = typeList.sumOf { it.countResources() }

  private fun Type.recompute(allowedLanguageLocales: Set<LanguageLocale>): Type? {
    val updatedEntries = entryList.mapNotNull { it.recompute(allowedLanguageLocales) }
    return if (updatedEntries.isNotEmpty()) {
      toBuilder().apply {
        clearEntry()
        addAllEntry(updatedEntries)
      }.build()
    } else null
  }

  private fun Type.countResources(): Int = entryList.sumOf { it.configValueCount }

  private fun Entry.recompute(allowedLanguageLocales: Set<LanguageLocale>): Entry? {
    val updatedConfigValues = configValueList.filter { it.isKept(allowedLanguageLocales) }
    return if (updatedConfigValues.isNotEmpty()) {
      toBuilder().apply {
        clearConfigValue()
        addAllConfigValue(updatedConfigValues)
      }.build()
    } else null
  }

  private fun ConfigValue.isKept(allowedLanguageLocales: Set<LanguageLocale>) =
    LanguageLocale.createFrom(config.locale) in allowedLanguageLocales

  /** Represents a locale in which text may be translated to a specific language. */
  private sealed class LanguageLocale {
    /** The IETF BCP 47 language code representation for this locale. */
    abstract val bcp47QualifiedCode: String

    /**
     * The Android-specific IETF BCP 47 language code representation for this locale (which can vary
     * from [bcp47QualifiedCode] since Android doesn't exactly conform to IETF BCP 47).
     */
    abstract val androidBcp47QualifiedCode: String

    /**
     * Locale corresponding to a language that has no regional-specific ties.
     *
     * @property languageCode the 2-character identifier code corresponding to the language
     */
    private data class GlobalLanguage(val languageCode: String) : LanguageLocale() {
      override val bcp47QualifiedCode = languageCode
      override val androidBcp47QualifiedCode: String
        get() = if (languageCode == "en") "" else languageCode
    }

    /**
     * Locale corresponding to a language with regionally-affected translations.
     *
     * @property globalLanguage the language's representation globally
     * @property regionCode the 2-character region code corresponding to the [globalLanguage]
     */
    private data class RegionalLanguage(
      val globalLanguage: GlobalLanguage,
      val regionCode: String
    ) : LanguageLocale() {
      override val bcp47QualifiedCode =
        "${globalLanguage.bcp47QualifiedCode}-${regionCode.uppercase()}"
      override val androidBcp47QualifiedCode =
        "${globalLanguage.androidBcp47QualifiedCode}-${regionCode.uppercase()}"
    }

    companion object {
      /**
       * Returns a new [LanguageLocale] from the provided [qualifiedLanguageCode] (which may be
       * either IETF BCP-47 or the Android version of it).
       */
      fun createFrom(qualifiedLanguageCode: String): LanguageLocale {
        return if ("-" in qualifiedLanguageCode) {
          val (languageCode, regionCode) = qualifiedLanguageCode.split('-', limit = 2)
          RegionalLanguage(createGlobalLanguageLocale(languageCode), regionCode.lowercase())
        } else createGlobalLanguageLocale(qualifiedLanguageCode)
      }

      /** Returns a new [LanguageLocale] to represent the provided [definition]. */
      fun createFrom(definition: LanguageSupportDefinition): LanguageLocale? {
        val androidLanguageId = definition.appStringId.androidResourcesLanguageId
        val language = androidLanguageId.languageCode.lowercase()
        val region = androidLanguageId.regionCode.lowercase()
        return when {
          language.isEmpty() -> null // Unsupported.
          region.isEmpty() -> GlobalLanguage(language)
          else -> RegionalLanguage(GlobalLanguage(language), region)
        }
      }

      private fun createGlobalLanguageLocale(languageCode: String): GlobalLanguage {
        return languageCode.lowercase().takeIf(String::isNotEmpty)?.let(::GlobalLanguage)
          ?: GlobalLanguage(languageCode = "en")
      }
    }
  }
}
