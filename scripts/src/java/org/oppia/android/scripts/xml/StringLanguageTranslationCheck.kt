package org.oppia.android.scripts.xml

import java.io.File

/**
 * Script for checking if all strings have been translated across all supported languages.
 *
 * Usage:
 *   bazel run //scripts:string_language_translation_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:string_language_translation_check -- $(pwd)
 */
fun main(vararg args: String) {
  require(args.isNotEmpty()) {
    "Expected: bazel run //scripts:string_language_translation_check -- <repo_path>"
  }

  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  val parser = StringResourceParser(File(repoPath))
  val baseTranslations = parser.retrieveBaseStringNames()
  val missingTranslations = parser.retrieveAllNonEnglishTranslations().mapValues { (_, xlations) ->
    baseTranslations - xlations.strings.keys
  }
  val missingTranslationCount = missingTranslations.values.sumOf { it.size }
  println("$missingTranslationCount translation(s) were found missing.")
  if (missingTranslationCount > 0) {
    println()
    println("Missing translations:")
    missingTranslations.forEach { (language, translations) ->
      if (translations.isNotEmpty()) {
        println("${language.name} (${translations.size}/$missingTranslationCount):")
        translations.forEach { translation ->
          println("- $translation")
        }
        println()
      }
    }
  }
}
