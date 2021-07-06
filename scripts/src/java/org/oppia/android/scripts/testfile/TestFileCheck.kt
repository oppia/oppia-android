package org.oppia.android.scripts.testfile

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.common.TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR
import org.oppia.android.scripts.common.TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR
import org.oppia.android.scripts.proto.ScriptExemptions
import java.io.File
import java.io.FileInputStream

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

  // A list of all the files to be exempted for this check.
  val testFileExemptionList = loadTestFileExemptionsProto().getExemptList()

  // A list of all kotlin files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(
    repoPath = repoPath,
    expectedExtension = ".kt",
    exemptionsList = testFileExemptionList
  )

  // A list of all the prod files present in the repo.
  val prodFilesList = searchFiles.filter { file -> !file.name.endsWith("Test.kt") }

  // A list of all the test files present in the repo.
  val testFilesList = searchFiles.filter { file -> file.name.endsWith("Test.kt") }

  // A list of all the prod files that do not have a corresponding test file.
  val matchedFiles = prodFilesList.filter { prodFile ->
    !testFilesList.any { testFile ->
      testFile.name == computeExpectedTestFileName(prodFile)
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
 * Computes the expected test file name for a prod file.
 *
 * @param prodFile the prod file for which expected test file name has to be computed
 * @return expected name of the test file
 */
private fun computeExpectedTestFileName(prodFile: File): String {
  return "${prodFile.nameWithoutExtension}Test.kt"
}

/**
 * Logs the file names of all the prod files that do not have a test file.
 *
 * @param matchedFiles list of all the files missing a test file
 */
private fun logFailures(matchedFiles: List<File>) {
  if (matchedFiles.isNotEmpty()) {
    matchedFiles.forEach { file ->
      println("File $file does not have a corresponding test file.")
    }
    println("If this is correct, please update scripts/assets/testfile_exemptions.textproto")
    println(
      "Note that, in general, all new files should have tests. If you choose to add an" +
        " exemption, please specifically call this out in your PR description."
    )
    println()
  }
}

/**
 * Loads the test file exemptions list to proto.
 *
 * @return proto class from the parsed textproto file
 */
private fun loadTestFileExemptionsProto(): ScriptExemptions {
  val protoBinaryFile = File("scripts/assets/test_file_exemptions.pb")
  val builder = ScriptExemptions.getDefaultInstance().newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  val protoObj: ScriptExemptions =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as ScriptExemptions
  return protoObj
}
