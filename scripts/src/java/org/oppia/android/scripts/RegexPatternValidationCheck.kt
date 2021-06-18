package org.oppia.android.scripts

import java.io.File
import java.io.FileInputStream
import org.oppia.android.app.model.FilenameChecks
import org.oppia.android.app.model.FilenameCheck
import org.oppia.android.app.model.FileContentChecks
import org.oppia.android.app.model.FileContentCheck
import kotlin.system.exitProcess

class RegexPatternValidationCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {

      val repoPath = args[0] + "/"

      val allowedDirectories: MutableList<String> = ArrayList()

      for (i in 1 until args.size) {
        allowedDirectories.add(args[i])
      }

      val searchFiles = collectSearchFiles(repoPath, allowedDirectories)

      var scriptFailedFlag = false

      getFilenameChecks().forEach {
        if (checkProhibitedFileNamePattern(
            repoPath = repoPath,
            searchFiles = searchFiles,
            prohibitedFilenameRegexString = it.getProhibitedFilenameRegex(),
            errorToShow = it.getFailureMessage()
          )) {
          scriptFailedFlag = true
        }
      }

      getFileContentChecks().forEach {
        if (checkProhibitedContent(
            repoPath = repoPath,
            searchFiles = searchFiles,
            fileNameRegexString = it.getFilenameRegex(),
            prohibitedContentRegexString = it.getProhibitedContentRegex(),
            errorToShow = it.getFailureMessage()
          )) {
          scriptFailedFlag = true
        }
      }

      if (scriptFailedFlag) {
        throw Exception("REGEX PATTERN CHECKS FAILED")
      } else {
        println("REGEX PATTERN CHECKS PASSED")
      }
    }

    fun getFilenameChecks(): List<FilenameCheck> {
      val fileNamePatternsBinaryFile =
        File("scripts/assets/filename_pattern_validation_checks.pb")
      val filenameCheckBuilder = FilenameChecks.newBuilder()
      val namePatternsObj: FilenameChecks =
        FileInputStream(fileNamePatternsBinaryFile).use {
          filenameCheckBuilder.mergeFrom(it)
        }.build() as FilenameChecks

      return namePatternsObj.getFilenameChecksList()
    }

    fun getFileContentChecks(): List<FileContentCheck> {
      val fileContentsBinaryFile =
        File("scripts/assets/file_content_validation_checks.pb")
      val fileContentCheckBuilder = FileContentChecks.newBuilder()
      val fileContentsObj: FileContentChecks =
        FileInputStream(fileContentsBinaryFile).use {
          fileContentCheckBuilder.mergeFrom(it)
        }.build() as FileContentChecks

      return fileContentsObj.getFileContentChecksList()
    }

    /**
     * Checks for a prohibited file naming pattern
     *
     * @param repoPath the path of the repo.
     * @param searchFiles a list of all the files which needs to be checked.
     * @param fileNameRegexString filename pattern regex which should not be present
     * @param errorToShow error to show incase of failure
     * @return [Boolean] check failed or passed
     */
    fun checkProhibitedFileNamePattern(
      repoPath: String,
      searchFiles: Sequence<File>,
      prohibitedFilenameRegexString: String,
      errorToShow: String
    ): Boolean {
      var filenamePatternCheckFailedFlag = false
      val prohibitedFilenameRegex = prohibitedFilenameRegexString.toRegex()
      searchFiles.forEach {
        val filePath = it.toString().removePrefix(repoPath)
        if (prohibitedFilenameRegex.matches(filePath)) {
          logProhibitedFilenameFailure(
            filePath = filePath,
            errorToShow = errorToShow
          )
          filenamePatternCheckFailedFlag = true
        }
      }
      return filenamePatternCheckFailedFlag
    }

    /**
     * Checks for a prohibited file content
     *
     * @param repoPath the path of the repo.
     * @param searchFiles a list of all the files which needs to be checked.
     * @param fileNameRegexString filename regex string in which to do the content check
     * @param prohibitedContentRegexString regex string which should not be contained in the file
     * @param errorToShow error to show incase of failure
     * @return [Boolean] check failed or passed
     */
    fun checkProhibitedContent(
      repoPath: String,
      searchFiles: Sequence<File>,
      fileNameRegexString: String,
      prohibitedContentRegexString: String,
      errorToShow: String
    ): Boolean {
      var contentCheckFailedFlag = false
      val fileNameRegex = fileNameRegexString.toRegex()
      val prohibitedContentRegex = prohibitedContentRegexString.toRegex()
      searchFiles.forEach {
        if (fileNameRegex.matches(it.name)) {
          var lineNumber = 0
          File(it.toString()).forEachLine { lineString ->
            lineNumber++
            if (prohibitedContentRegex.matches(lineString)) {
              logProhibitedContentFailure(
                lineNumber = lineNumber,
                errorToShow = errorToShow,
                filePath = it.toString().removePrefix(repoPath)
              )
              contentCheckFailedFlag = true
            }
          }
        }
      }
      return contentCheckFailedFlag
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
          && it.name !in exemptionList
      }
      return validPaths
    }

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

    /** Logs the failures for filename pattern violation */
    fun logProhibitedFilenameFailure(
      errorToShow: String,
      filePath: String
    ) {
      println("Prohibited filename pattern: [ROOT]/$filePath\n" +
        "Failure message: $errorToShow\n")
    }

    /** Logs the failures for file content violation */
    fun logProhibitedContentFailure(
      lineNumber: Int,
      errorToShow: String,
      filePath: String) {
      println("Prohibited content usage found on line no. $lineNumber\n" +
        "File: [ROOT]/$filePath\n" +
        "Failure message: $errorToShow\n")
    }
  }
}
