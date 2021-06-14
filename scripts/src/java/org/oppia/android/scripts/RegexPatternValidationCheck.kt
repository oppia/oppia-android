package org.oppia.android.scripts

import java.io.File
import java.io.FileInputStream
import org.oppia.android.app.model.FilenameChecks
import org.oppia.android.app.model.FileContentChecks
import kotlin.system.exitProcess


class RegexPatternValidationCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {

      val fileNamePatternsBinaryFile =
        File("scripts/assets/filename_pattern_validation_checks.pb")
      val fileContentsBinaryFile =
        File("scripts/assets/file_content_validation_checks.pb")
      val filenameCheckBuilder = FilenameChecks.newBuilder()
      val fileContentCheckBuilder = FileContentChecks.newBuilder()
      val namePatternsObj: FilenameChecks =
        FileInputStream(fileNamePatternsBinaryFile).use {
          filenameCheckBuilder.mergeFrom(it)
        }.build() as FilenameChecks
      val fileContentsObj: FileContentChecks =
        FileInputStream(fileContentsBinaryFile).use {
          fileContentCheckBuilder.mergeFrom(it)
        }.build() as FileContentChecks
      val repoPath = args[0] + "/"
      val allowedDirectories = arrayOf("data", "app")
      val searchFiles = collectSearchFiles(repoPath, allowedDirectories)
      var scriptFailedFlag = false

      namePatternsObj.getFilenameChecksList().forEach {
        if (checkProhibitedFileNamePattern(
            repoPath = repoPath,
            searchFiles = searchFiles,
            prohibitedFilenameRegexString = it.getProhibitedFilenameRegex(),
            errorToShow = it.getFailureMessage()
          )) {
          scriptFailedFlag = true
        }
      }

      fileContentsObj.getFileContentChecksList().forEach {
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
        println("REGEX PATTERN CHECKS FAILED")
        exitProcess(1)
      } else {
        println("REGEX PATTERN CHECKS PASSED")
      }
    }

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
          File(it.toString()).forEachLine { l ->
            lineNumber++
            if (prohibitedContentRegex.matches(l)) {
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

    fun collectSearchFiles(
      repoPath: String,
      allowedDirectories: Array<String>,
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
      allowedDirectories: Array<String>
    ): Boolean {
      allowedDirectories.forEach {
        if (pathString.startsWith(it))
          return true
      }
      return false
    }

    fun logProhibitedFilenameFailure(
      errorToShow: String,
      filePath: String
    ) {
      println("Prohibited filename pattern: [ROOT]/$filePath")
      println("Failure message: $errorToShow")
      println()
    }

    fun logProhibitedContentFailure(
      lineNumber: Int,
      errorToShow: String,
      filePath: String) {
      println("Prohibited content usage found on line no. $lineNumber")
      println("File: [ROOT]/$filePath")
      println("Failure message: $errorToShow")
      println()
    }
  }
}