package org.oppia.android.scripts

/**
 * Script for ensuring that every prod file has a
 * corresponding Test file present in the repo.
 */
class TestFileCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      // path of the repo to be analyzed.
      val repoPath = args[0] + "/"

      // a list of all allowed directories in the repo to be analyzed.
      // args[0] is the repoPath, the allowed directories are specified
      // after it. Hence, we have to start from the 1st index.
      val allowedDirectories = args.drop(1)

      // a list of all kotlin files in the repo to be analyzed.
      val searchFiles = RepoFile.collectSearchFiles(
        repoPath,
        allowedDirectories,
        ".kt",
        ExemptionsList.TEST_FILE_CHECK_EXEMPTIONS_LIST
      )

      // a list of all the prod files present in the repo.
      val prodFilesList: MutableList<String> = ArrayList()

      // a list of all the test files present in the repo.
      val testFilesList: MutableList<String> = ArrayList()

      searchFiles.forEach {
        if (it.name.endsWith("Test.kt")) {
          testFilesList.add(it.name)
        } else {
          prodFilesList.add(it.name)
        }
      }

      // a list of all the prod files that do not have a corresponding test file.
      val matchedFileNames = prodFilesList.filter {
        retrievePotentionalTestFileName(it) !in testFilesList
      }

      logFailures(matchedFileNames)

      if (matchedFileNames.size != 0) {
        throw Exception(ScriptResultConstants.TEST_FILE_CHECK_FAILED)
      } else {
        println(ScriptResultConstants.TEST_FILE_CHECK_PASSED)
      }
    }

    /**
     * Retrieves the potential test file name for a prod file.
     *
     * @param prodFileName name of the prod file
     * @return potential name of the test file
     */
    private fun retrievePotentionalTestFileName(prodFileName: String): String {
      return prodFileName.removeSuffix(".kt") + "Test.kt"
    }

    /**
     * Logs the file names of all the prod files that do not have a test file.
     *
     * @param matchedFileNames list of all the files missing a test file
     * @return log the failures
     */
    private fun logFailures(matchedFileNames: List<String>) {
      if (matchedFileNames.size != 0) {
        print("No test file found for:\n")
        matchedFileNames.forEach { file ->
          print("$file\n")
        }
        println()
      }
    }
  }
}
