package org.oppia.android.scripts.testfile

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.TestFileExemptions
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
  val repoPath = "${args[0]}/"

  val testFileExemptiontextProto = "scripts/assets/test_file_exemptions"

  // A list of all the files to be exempted for this check.
  // TODO(#3436): Develop a mechanism for permanently exempting files which do not ever need tests.
  val testFileExemptionList = loadTestFileExemptionsProto(testFileExemptiontextProto)
    .getExemptedFilePathList()

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

  logFailures(matchedFiles, testFileExemptiontextProto)

  if (matchedFiles.isNotEmpty()) {
    println(
      "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
        "#test-file-presence-check for more details on how to fix this.\n"
    )
  }

  if (matchedFiles.isNotEmpty()) {
    throw Exception("TEST FILE CHECK FAILED")
  } else {
    println("TEST FILE CHECK PASSED")
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
 * @param testFileExemptiontextProto the location of the test file exemption textproto file
 */
private fun logFailures(matchedFiles: List<File>, testFileExemptiontextProto: String) {
  if (matchedFiles.isNotEmpty()) {
    matchedFiles.sorted().forEach { file ->
      println("File $file does not have a corresponding test file.")
    }
    println()
  }
}

/**
 * Loads the test file exemptions list to proto.
 *
 * @param testFileExemptiontextProto the location of the test file exemption textproto file
 * @return proto class from the parsed textproto file
 */
private fun loadTestFileExemptionsProto(testFileExemptiontextProto: String): TestFileExemptions {
  val protoBinaryFile = File("$testFileExemptiontextProto.pb")
  val builder = TestFileExemptions.getDefaultInstance().newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  val protoObj: TestFileExemptions =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as TestFileExemptions
  return protoObj
}
