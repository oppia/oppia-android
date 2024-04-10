package org.oppia.android.scripts.wiki

import java.io.File

/**
 * Script for ensuring that the table of contents in each wiki page matches with its respective headers.
 *
 * Usage:
 *   bazel run //scripts:wiki_sample -- <path_to_default_working_directory>
 *
 * Arguments:
 * - path_to_default_working_directory: The default working directory on the runner for steps, and the default location of repository.
 *
 * Example:
 *   bazel run //scripts:wiki_sample -- ${GITHUB_WORKSPACE}
 */
fun main(vararg args: String) {
  // Path to the repo's wiki.
  val githubWorkspace = "${args[0]}/wiki/"
  val wikiDirectory = File(githubWorkspace)

  // Check if the wiki directory exists
  if (wikiDirectory.exists() && wikiDirectory.isDirectory) {
    processWikiDirectory(wikiDirectory)
  } else {
    println("No contents found in the Wiki directory.")
  }
}

/**
 * Checks every file in the wiki repo
 *
 * @param wikiDirectory the default working directory
 */
fun processWikiDirectory(wikiDirectory: File) {
  wikiDirectory.listFiles()?.forEach { file ->
    processWikiFile(file)
  }
}

/**
 * Processes the contents of a single wiki file to ensure the accuracy of the Table of Contents.
 *
 * @param file The wiki file to process.
 */
fun processWikiFile(file: File) {
  var inTableOfContents = false
  var skipBlankLine = false

  file.forEachLine { line ->
    when {
      // Checking for Table of Contents section
      line.trim() == "## Table of Contents" -> {
        inTableOfContents = true
        skipBlankLine = true
      }
      // Checking to skip the blank line immediately after the ## Table of Contents
      skipBlankLine && line.isBlank() -> skipBlankLine = false
      // Validating the contents in the Table of Content
      inTableOfContents && line.trimStart().startsWith("- [") && !line.contains("https://") -> {
        validateTableOfContents(file, line)
      }
      // Checking for end of Table of Contents section
      inTableOfContents && line.isBlank() -> inTableOfContents = false
    }
  }
}

/**
 * Validates the accuracy of a Table of Contents entry in a wiki file.
 *
 * @param file The wiki file being validated.
 * @param line The line containing the Table of Contents entry.
 */
fun validateTableOfContents(file: File, line: String) {
  val titleRegex = "\\[(.*?)\\]".toRegex()
  val title = titleRegex.find(line)?.groupValues?.get(1)?.replace('-', ' ')
    ?.replace(Regex("[?&./:’'*!,(){}\\[\\]+]"), "")
    ?.trim()

  val linkRegex = "\\(#(.*?)\\)".toRegex()
  val link = linkRegex.find(line)?.groupValues?.get(1)?.removePrefix("#")?.replace('-', ' ')
    ?.replace(Regex("[?&./:’'*!,(){}\\[\\]+]"), "")
    ?.replace("confetti_ball", "")?.trim()

  // Checks if the table of content title matches with the header link text
  val matches = title.equals(link, ignoreCase = true)
  if (!matches) {
    throw Exception(
      "\nMismatch of Table of Content with headers in the File: ${file.name}. " +
        "\nThe Title: '${titleRegex.find(line)?.groupValues?.get(1)}' " +
        "doesn't match with its corresponding Link: '${linkRegex.find(line)?.groupValues?.get(1)}'."
    )
  }
}
