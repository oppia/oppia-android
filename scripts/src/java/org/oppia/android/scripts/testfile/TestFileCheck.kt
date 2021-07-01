package org.oppia.android.scripts.testfile

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.common.ScriptExemptions
import org.oppia.android.scripts.common.TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR
import org.oppia.android.scripts.common.TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR
import java.io.File

/**
 * Script for ensuring that all production files have test files present.
 *
 * Usage:
 *   bazel run //scripts:test_file_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:test_file_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = args[0] + "/"

  // A list of all kotlin files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".kt",
    exemptionsList = ScriptExemptions.TEST_FILE_CHECK_EXEMPTIONS_LIST
  )

  // A list of all the prod files present in the repo.
  val prodFilesList = mutableListOf<File>()

  // A list of all the test files present in the repo.
  val testFilesList = mutableListOf<File>()

  searchFiles.forEach { file ->
    if (file.name.endsWith("Test.kt")) {
      testFilesList.add(file)
    } else {
      prodFilesList.add(file)
    }
  }

  // A list of all the prod files that do not have a corresponding test file.
  val matchedFiles = prodFilesList.filter { prodFile ->
    !testFilesList.any { testFile ->
      testFile.name == retrievePotentionalTestFileName(prodFile.name)
    }
  }

  logFailures(matchedFiles)

  if (matchedFiles.isNotEmpty()) {
    throw Exception(TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR)
  } else {
    println(TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR)
  }
}

/**
 * Retrieves the potential test file name for a prod file.
 *
 * @param prodFileName name of the prod file
 * @return potential name of the test file
 */
private fun retrievePotentionalTestFileName(prodFileName: String): String {
  return "${prodFileName.removeSuffix(".kt")}Test.kt"
}

/**
 * Logs the file names of all the prod files that do not have a test file.
 *
 * @param matchedFiles list of all the files missing a test file
 */
private fun logFailures(matchedFiles: List<File>) {
  if (matchedFiles.isNotEmpty()) {
    println("No test file found for:")
    matchedFiles.forEach { file ->
      println("- $file")
    }
    println()
  }
}
