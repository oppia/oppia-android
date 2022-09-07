package org.oppia.android.scripts.common

import java.io.File
import java.nio.file.FileVisitOption
import java.nio.file.Files
import java.nio.file.Path
import kotlin.streams.asSequence

/** Helper class for managing & accessing files within the project repository. */
class RepositoryFile() {
  companion object {
    /** A list of directories which should be excluded for every script check. */
    private val alwaysExcludeDirectoryList = listOf<String>(
      ".git",
      ".gitsecret",
      ".idea",
      ".aswb",
      "gradle",
      "bazel-bin",
      "bazel-oppia-android",
      "bazel-out",
      "bazel-testlogs",
      "app/build/",
      "data/build/",
      "domain/build/",
      "model/build/",
      "testing/build/",
      "utility/build/",
    )

    /**
     * Collects the paths of all the files which are needed to be checked.
     * Files that are generated or can't be altered in style/best practices (such as those managed
     * by Android Studio) are automatically exempted.
     *
     * @param repoPath the path of the repo
     * @param expectedExtension files with only this extension will be included in the search list.
     *     This defaults to the empty string which signifies no extension restriction.
     * @param exemptionsList a list of files that are exempted from the check. This defaults to an
     *     empty list which signifies no file is exempted for the check.
     * @return all files which need to be checked
     */
    fun collectSearchFiles(
      repoPath: String,
      expectedExtension: String = "",
      exemptionsList: List<String> = listOf<String>()
    ): List<File> {
      // Note that Files.walk() is used instead of Kotlin's walk() function since the latter follows
      // symbolic links which is almost 10x slower than not following them (due to very deep Bazel
      // build directories), and it's not necessary to follow the symlinks.
      return Files.walk(File(repoPath).toPath()).asSequence().map(Path::toFile).filter { file ->
        val isProhibited = checkIfProhibitedFile(retrieveRelativeFilePath(file, repoPath))
        !isProhibited &&
          file.isFile &&
          file.name.endsWith(expectedExtension) &&
          retrieveRelativeFilePath(file, repoPath) !in exemptionsList
      }.toList()
    }

    /**
     * Checks if a file/directory is prohibited to be analyzed for the check.
     *
     * @param pathString the path of the repo
     * @return whether the specified path should be analyzed per allow rules
     */
    private fun checkIfProhibitedFile(pathString: String): Boolean {
      return alwaysExcludeDirectoryList.any {
        if (it.endsWith("/")) {
          pathString.startsWith("$it")
        } else {
          pathString.startsWith("$it/")
        }
      }
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
