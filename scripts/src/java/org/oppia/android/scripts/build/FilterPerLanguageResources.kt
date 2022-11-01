package org.oppia.android.scripts.build

import com.android.aapt.Resources.ConfigValue
import com.android.aapt.Resources.Entry
import com.android.aapt.Resources.Package
import com.android.aapt.Resources.ResourceTable
import com.android.aapt.Resources.Type
import org.oppia.android.app.model.LanguageSupportDefinition
import org.oppia.android.app.model.SupportedLanguages
import java.io.File
import java.util.Locale
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
  require(args.size >= 2) {
    "Usage: bazel run //scripts:filter_per_language_resources --" +
      " </absolute/path/to/input_module.zip:Path>" +
      " </absolute/path/to/output_module.zip:Path>"
  }
  FilterPerLanguageResources().filterPerLanguageResources(File(args[0]), File(args[1]))
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

    val allReferencedLanguageCodes =
      pkg.typeList.flatMap { it.entryList }
        .flatMap { it.configValueList }
        .map { it.config }
        .map { it.locale }
        .toSortedSet()
    val supportedLanguageCodes =
      supportedLanguages.languageDefinitionsList.mapNotNull {
        it.toAndroidBcp47Locale()
      }.toSortedSet()
    val removedLanguageCodes = allReferencedLanguageCodes - supportedLanguageCodes
    val updatedResourceTable = resourceTable.recompute(supportedLanguageCodes)
    println(
      "${removedLanguageCodes.size} resources are being removed that are tied to unsupported" +
        " languages: $removedLanguageCodes (size reduction:" +
        " ${resourceTable.serializedSize - updatedResourceTable.serializedSize} bytes)."
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

  private fun ResourceTable.recompute(allowedLanguageCodes: Set<String>): ResourceTable {
    val updatedPackages = packageList.mapNotNull { it.recompute(allowedLanguageCodes) }
    return toBuilder().apply {
      clearPackage()
      addAllPackage(updatedPackages)
    }.build()
  }

  private fun Package.recompute(allowedLanguageCodes: Set<String>): Package? {
    val updatedTypes = typeList.mapNotNull { it.recompute(allowedLanguageCodes) }
    return if (updatedTypes.isNotEmpty()) {
      toBuilder().apply {
        clearType()
        addAllType(updatedTypes)
      }.build()
    } else null
  }

  private fun Type.recompute(allowedLanguageCodes: Set<String>): Type? {
    val updatedEntries = entryList.mapNotNull { it.recompute(allowedLanguageCodes) }
    return if (updatedEntries.isNotEmpty()) {
      toBuilder().apply {
        clearEntry()
        addAllEntry(updatedEntries)
      }.build()
    } else null
  }

  private fun Entry.recompute(allowedLanguageCodes: Set<String>): Entry? {
    val updatedConfigValues = configValueList.filter { it.isKept(allowedLanguageCodes) }
    return if (updatedConfigValues.isNotEmpty()) {
      toBuilder().apply {
        clearConfigValue()
        addAllConfigValue(updatedConfigValues)
      }.build()
    } else null
  }

  private fun ConfigValue.isKept(allowedLanguageCodes: Set<String>) =
    config.locale in allowedLanguageCodes

  private fun LanguageSupportDefinition.toAndroidBcp47Locale(): String? {
    val androidLanguageId = appStringId.androidResourcesLanguageId
    val language = androidLanguageId.languageCode.toLowerCase(Locale.US)
    val region = androidLanguageId.regionCode.toUpperCase(Locale.US)
    return when {
      language.isEmpty() -> null // Unsupported.
      language == "en" -> "" // English is the default language code on Android.
      region.isEmpty() -> language
      else -> "$language-$region"
    }
  }
}
