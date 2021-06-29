package org.oppia.android.scripts.common

import java.io.File

/** Helper class for managing & accessing files within the project repository. */
class RepositoryFile() {
  companion object {
    /** a list of directories and files which should be excluded for every script check. */
    val alwaysExcludeFilesList = listOf<String>(
      ".git",
      ".gitsecret",
      ".idea",
      "bazel",
      "config",
      "gradle",
      ".bazelrc",
      ".editorconfig",
      "gradlew",
      "gradlew.bat",
      ".aswb",
    )

    /**
     * Collects the paths of all the files which are needed to be checked.
     *
     * @param repoPath the path of the repo
     * @param allowedExtension files with only this extension will be included in the search list
     * @param exemptionsList a list of files which should not be included in the search list
     * @return all files which needs to be checked
     */
    fun collectSearchFiles(
      repoPath: String,
      allowedExtension: String = "",
      exemptionsList: List<String> = listOf()
    ): List<File> {
      return File(repoPath).walk().filter { file ->
        val isProhibited = checkIfProhibitedFile(retrieveRelativeFilePath(file, repoPath))
        !isProhibited &&
          file.isFile &&
          file.name.endsWith(allowedExtension) &&
          file.name !in exemptionsList
      }.toList()
    }

    /**
     * Checks if a file/directory is prohibited to be analyzed for the check.
     *
     * @param pathString the path of the repo
     * @return check if path is allowed to be analyzed or not
     */
    private fun checkIfProhibitedFile(pathString: String): Boolean {
      return alwaysExcludeFilesList.any { pathString.startsWith(it) }
    }

    /**
     * Retrieves the file path relative to the root repository.
     *
     * @param file the file whose whose path is to be retrieved
     * @param repoPath the path of the repo to be analyzed
     * @return path relative to root repository
     */
    fun retrieveRelativeFilePath(file: File, repoPath: String): String {
      return file.toString().removePrefix(repoPath)
    }
  }
}
