package org.oppia.android.scripts

import java.io.File
import org.oppia.android.scripts.ExemptionsList
import org.oppia.android.scripts.ScriptResultConstants

class TestFileCheck {
  companion object {

    @JvmStatic
    fun main(vararg args: String) {
      /** path of the repo to be analyzed. */
      val repoPath = args[0] + "/"

      /** a list of all allowed directories in the repo to be analyzed. */
      val allowedDirectories = args.drop(1)

      /** a list of all files in the repo to be analyzed. */
      val searchFiles = RepoFile.collectSearchFiles(
        repoPath,
        allowedDirectories,
        ExemptionsList.TEST_FILE_CHECK_EXEMPTIONS_LIST
      )

      /** a list of all the prod files present in the repo. */
      val prodFilesList: MutableList<String> = ArrayList()

      /** a list of all the test files present in the repo. */
      val testFilesList: MutableList<String> = ArrayList()

      searchFiles.forEach {
        if (it.name.endsWith("Test.kt")) {
          testFilesList.add(it.name)
        } else {
          prodFilesList.add(it.name)
        }
      }

      /** a list of all the prod files that do not have a corresponding test file. */
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
     * retrieves the potential test file name for a prod file.
     *
     * @param prodFileName name of the prod file
     * @return potential name of the test file
     */
    fun retrievePotentionalTestFileName(prodFileName: String): String {
      return prodFileName.removeSuffix(".kt") + "Test.kt"
    }

    /**
     * logs the file names of all the prod files that do not have a test file.
     *
     * @param matchedFileNames list of all the files missing a test file
     * @return log the failures
     */
    fun logFailures(matchedFileNames: List<String>) {
      if (matchedFileNames.size != 0) {
        print("No test file found for:\n")
        matchedFileNames.forEach {
          print("$it\n")
        }
        print("\n")
      }
    }
  }

  /** helper class which contains all the file related helper methods. */
  private class RepoFile() {
    companion object {
      /**
       * Collects the paths of all the files which are needed to be checked.
       *
       * @param repoPath the path of the repo
       * @param allowedDirectories a list of all the directories which needs to be checked
       * @return all files which needs to be checked
       */
      fun collectSearchFiles(
        repoPath: String,
        allowedDirectories: List<String>, exemptionsList: Array<String>)
        : List<File> {
        return File(repoPath).walk().filter { it ->
          checkIfAllowedDirectory(
            retrieveFilePath(it, repoPath),
            allowedDirectories)
            && it.isFile
            && it.name.endsWith(".kt")
            && it.name !in exemptionsList
        }.toList()
      }

      /**
       * Checks if a layer is allowed to be analyzed for the check or not.
       * It only allows the layers listed in allowedDirectories (which is
       * specified from the command line arguments) to be analyzed.
       *
       * @param pathString the path of the repo
       * @param allowedDirectories a list of all the files which needs to be checked
       * @return check if path is  allowed to be analyzed or not
       */
      fun checkIfAllowedDirectory(
        pathString: String,
        allowedDirectories: List<String>
      ): Boolean {
        return allowedDirectories.any { pathString.startsWith(it) }
      }

      /**
       * Retrieves the file path relative to the root repository.
       *
       * @param file the file whose whose path is to be retrieved
       * @param repoPath the path of the repo to be analyzed
       * @return path relative to root repository
       */
      fun retrieveFilePath(file: File, repoPath: String): String {
        return file.toString().removePrefix(repoPath)
      }
    }
  }
}
