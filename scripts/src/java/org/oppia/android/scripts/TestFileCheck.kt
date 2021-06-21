package org.oppia.android.scripts

import java.io.File
import org.oppia.android.scripts.ExemptionsList
import org.oppia.android.scripts.ScriptResultConstants

class TestFileCheck {
  companion object {

    @JvmStatic
    fun main(vararg args: String) {

      val repoPath = args[0] + "/"

      val allowedDirectories: MutableList<String> = ArrayList()

      val prodFilesList: MutableList<String> = ArrayList()

      val testFilesList: MutableList<String> = ArrayList()

      for (i in 1 until args.size) {
        allowedDirectories.add(args[i])
      }

      val searchFiles = collectSearchFiles(repoPath,
        allowedDirectories,
        ExemptionsList.TEST_FILE_CHECK_EXEMPTIONS_LIST)

      var scriptFailedFlag = false

      searchFiles.forEach {
        if (it.name.endsWith("Test.kt")) {
          testFilesList.add(it.name)
        } else {
          prodFilesList.add(it.name)
        }
      }

      prodFilesList.forEach {
        val potentialTestFile = it.removeSuffix(".kt") + "Test.kt"
        if (potentialTestFile !in testFilesList) {
          println("No test file found for: $it\n")
          scriptFailedFlag = true
        }
      }

      if (scriptFailedFlag) {
        throw Exception(ScriptResultConstants.TEST_FILE_CHECK_FAILED)
      } else {
        println(ScriptResultConstants.TEST_FILE_CHECK_PASSED)
      }
    }

    /**
     * Collects the paths of all the files which are needed to be checked
     *
     * @param repoPath the path of the repo.
     * @param allowedDirectories a list of all the directories which needs to be checked.
     * @param exemptionList a list of files which needs to be exempted for this check
     * @return [Sequence<File>] all files which needs to be checked.
     */
    fun collectSearchFiles(
      repoPath: String,
      allowedDirectories: MutableList<String>,
      exemptionList: Array<String> = arrayOf<String>()
    ): Sequence<File> {
      val validPaths = File(repoPath).walk().filter { it ->
        checkIfAllowedDirectory(
          it.toString().removePrefix(repoPath),
          allowedDirectories)
          && it.isFile
          && it.name.endsWith(".kt")
          && it.name !in exemptionList
      }
      return validPaths
    }

    /**
     * Checks if a layer is allowed to be analyzed for the check or not.
     * It only allows the layers listed in allowedDirectories
     * (which is specified from the command line arguments) to be analyzed.
     *
     * @param pathString the path of the repo.
     * @param allowedDirectories a list of all the files which needs to be checked.
     * @return [Boolean] check failed or passed
     */
    fun checkIfAllowedDirectory(
      pathString: String,
      allowedDirectories: MutableList<String>
    ): Boolean {
      allowedDirectories.forEach {
        if (pathString.startsWith(it))
          return true
      }
      return false
    }
  }
}
