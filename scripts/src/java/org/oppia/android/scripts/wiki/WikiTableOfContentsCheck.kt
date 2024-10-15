package org.oppia.android.scripts.wiki

import java.io.File

/**
 * Script for ensuring that the table of contents in each wiki page matches with its respective headers.
 *
 * Usage:
 *   bazel run //scripts:wiki_table_of_contents_check -- <path_to_default_working_directory>
 *
 * Arguments:
 * - path_to_default_working_directory: The default working directory on the runner for steps, and the default location of repository.
 *
 * Example:
 *   bazel run //scripts:wiki_table_of_contents_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path to the repo's wiki.
  val wikiDirPath = "${args[0]}/wiki/"
  val wikiDir = File(wikiDirPath)

  // Check if the wiki directory exists.
  if (wikiDir.exists() && wikiDir.isDirectory) {
    processWikiDirectory(wikiDir)
    println("WIKI TABLE OF CONTENTS CHECK PASSED")
  } else {
    println("No contents found in the Wiki directory.")
  }
}

private fun processWikiDirectory(wikiDir: File) {
  wikiDir.listFiles()?.forEach { file ->
    checkTableOfContents(file)
  }
}

private fun checkTableOfContents(file: File) {
  val fileContents = file.readLines()
  val tocStartIdx = fileContents.indexOfFirst {
    it.contains(Regex("""##\s+Table\s+of\s+Contents""", RegexOption.IGNORE_CASE))
  }
  if (tocStartIdx == -1) {
    return
  }

  // Skipping the blank line after the ## Table of Contents
  val tocEndIdx = fileContents.subList(tocStartIdx + 2, fileContents.size).indexOfFirst {
    it.startsWith("#")
  }.takeIf { it != -1 }
    ?: error("Wiki doesn't contain headers referenced in Table of Contents.")

  val tocSpecificLines = fileContents.subList(tocStartIdx, tocStartIdx + tocEndIdx + 1)

  for (line in tocSpecificLines) {
    if (line.trimStart().startsWith("- [") && !line.contains("https://")) {
      validateTableOfContents(file, line)
    }
  }
}

private fun validateTableOfContents(file: File, line: String) {
  val titleRegex = "\\[(.*?)\\]".toRegex()
  val title = titleRegex.find(line)?.groupValues?.get(1)?.replace('-', ' ')
    ?.replace(Regex("[?&./:’'*!,(){}\\[\\]+]"), "")
    ?.trim()

  val linkRegex = "\\(#(.*?)\\)".toRegex()
  val link = linkRegex.find(line)?.groupValues?.get(1)?.removePrefix("#")?.replace('-', ' ')
    ?.replace(Regex("[?&./:’'*!,(){}\\[\\]+]"), "")
    ?.trim()

  // Checks if the table of content title matches with the header link text.
  val matches = title.equals(link, ignoreCase = true)
  if (!matches) {
    error(
      "\nWIKI TABLE OF CONTENTS CHECK FAILED" +
        "\nMismatch of Table of Content with headers in the File: ${file.name}. " +
        "\nThe Title: '${titleRegex.find(line)?.groupValues?.get(1)}' " +
        "doesn't match with its corresponding Link: '${linkRegex.find(line)?.groupValues?.get(1)}'."
    )
  }
}
