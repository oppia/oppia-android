package org.oppia.android.scripts

import java.io.File

/** Helper class which contains all the file collection helper methods */
class RepoFile() {
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
      allowedDirectories: List<String>,
      allowedExtension: String = "",
      exemptionsList: List<String> = listOf()
    ): List<File> {
      return File(repoPath).walk().filter { file ->
        val isAllowed = checkIfAllowedDirectory(
          retrieveFilePath(file, repoPath),
          allowedDirectories
        )
        isAllowed &&
          file.isFile &&
          file.name.endsWith(allowedExtension) &&
          file.name !in exemptionsList
      }.toList()
    }

    /**
     * Checks if a directory is allowed to be analyzed for the check or not.
     * It only allows the directories listed in allowedDirectories (which is
     * specified from the command line arguments) to be analyzed.
     *
     * @param pathString the path of the repo
     * @param allowedDirectories a list of all the files which needs to be checked
     * @return check if path is allowed to be analyzed or not
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
