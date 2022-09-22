package org.oppia.android.scripts.xml

import org.oppia.android.scripts.xml.StringResourceParser.StringFile
import org.oppia.android.scripts.xml.StringResourceParser.TranslationLanguage
import java.io.File

/**
 * Script for validating consistency between translated and base string resources.
 *
 * Usage:
 *   bazel run //scripts:string_resource_validation_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:string_resource_validation_check -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "Expected: bazel run //scripts:string_resource_validation_check -- <repo_path>"
  }

  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"
  val repoRoot = File(repoPath)

  data class Finding(val language: TranslationLanguage, val file: File, val errorLine: String)
  val parser = StringResourceParser(repoRoot)
  val baseFile = parser.retrieveBaseStringFile()
  val otherTranslations = parser.retrieveAllNonEnglishTranslations()
  val inconsistencies = otherTranslations.entries.fold(listOf<Finding>()) { errors, entry ->
    val (_, translatedFile) = entry
    errors + computeInconsistenciesBetween(baseFile, translatedFile).map { line ->
      Finding(translatedFile.language, translatedFile.file, line)
    }
  }.groupBy(keySelector = { it.language to it.file }, valueTransform = { it.errorLine })

  if (inconsistencies.isNotEmpty()) {
    println("${inconsistencies.size} language(s) were found with string consistency errors.")
    println()

    inconsistencies.forEach { (context, errorLines) ->
      val (language, file) = context
      println(
        "${errorLines.size} consistency error(s) were found for ${language.name} strings (file:" +
          " ${file.toRelativeString(repoRoot)}):"
      )
      errorLines.forEach { println("- $it") }
      println()
    }
    throw Exception("STRING RESOURCE VALIDATION CHECKS FAILED")
  } else println("STRING RESOURCE VALIDATION CHECKS PASSED")
}

private fun computeInconsistenciesBetween(
  baseFile: StringFile,
  translatedFile: StringFile
): List<String> {
  val commonTranslations = baseFile.strings.intersectWith(translatedFile.strings)

  // Check for inconsistent newlines post-translation.
  return commonTranslations.mapNotNull { (stringName, stringPair) ->
    val (baseString, translatedString) = stringPair
    val baseLines = baseString.split("\\n")
    val translatedLines = translatedString.split("\\n")
    return@mapNotNull if (baseLines.size != translatedLines.size) {
      "string $stringName: original translation uses ${baseLines.size} line(s) but translation" +
        " uses ${translatedLines.size} line(s). Please remove any extra lines or add any that are" +
        " missing."
    } else null // The number of lines match.
  }
}

private fun Map<String, String>.intersectWith(other: Map<String, String>) =
  keys.intersect(other.keys).associateWith { getValue(it) to other.getValue(it) }
