package org.oppia.android.scripts

import com.google.protobuf.MessageLite
import org.oppia.android.scripts.proto.FileContentCheck
import org.oppia.android.scripts.proto.FileContentChecks
import org.oppia.android.scripts.proto.FilenameCheck
import org.oppia.android.scripts.proto.FilenameChecks
import java.io.File
import java.io.FileInputStream

/**
 * Script for ensuring that prohibited file contents and
 * file naming patterns are not present in the codebase.
 */
class RegexPatternValidationCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      // path of the repo to be analyzed.
      val repoPath = args[0] + "/"

      // a list of all allowed directories in the repo to be analyzed.
      // args[0] is the repoPath, the allowed directories are specified
      // after it. Hence, we have to start from the 1st index.
      val allowedDirectories = args.drop(1)

      // a list of all files in the repo to be analyzed.
      val searchFiles = RepoFile.collectSearchFiles(
        repoPath,
        allowedDirectories
      )

      // check if the repo has any filename failure.
      val hasFilenameCheckFailure = retrieveFilenameChecks()
        .fold(initial = false) { isFailing, filenameCheck ->
          val checkFailed = checkProhibitedFileNamePattern(
            repoPath,
            searchFiles,
            filenameCheck,
          )
          isFailing || checkFailed
        }

      // check if the repo has any file content failure.
      val hasFileContentCheckFailure = retrieveFileContentChecks()
        .fold(initial = false) { isFailing, fileContentCheck ->
          val checkFailed = checkProhibitedContent(
            repoPath,
            searchFiles,
            fileContentCheck,
          )
          isFailing || checkFailed
        }

      if (hasFilenameCheckFailure || hasFileContentCheckFailure) {
        throw Exception(ScriptResultConstants.REGEX_CHECKS_FAILED)
      } else {
        println(ScriptResultConstants.REGEX_CHECKS_PASSED)
      }
    }
    /**
     * Retrieves all filename checks.
     *
     * @return a list of all the FilenameChecks
     */
    private fun retrieveFilenameChecks(): List<FilenameCheck> {
      return getProto(
        "filename_pattern_validation_checks.pb",
        FilenameChecks.getDefaultInstance()
      ).getFilenameChecksList()
    }

    /**
     * Retrieves all file content checks.
     *
     * @return a list of all the FileContentChecks
     */
    private fun retrieveFileContentChecks(): List<FileContentCheck> {
      return getProto(
        "file_content_validation_checks.pb",
        FileContentChecks.getDefaultInstance()
      ).getFileContentChecksList()
    }

    /**
     * Helper function to parse the textproto file to a proto class.
     *
     * @param textProtoFileName name of the textproto file to be parsed
     * @param proto instance of the proto class
     * @return proto class from the parsed textproto file
     */
    private fun <T : MessageLite> getProto(textProtoFileName: String, proto: T): T {
      val protoBinaryFile = File("scripts/assets/$textProtoFileName")
      val builder = proto.newBuilderForType()

      @Suppress("UNCHECKED_CAST")
      val protoObj: T =
        FileInputStream(protoBinaryFile).use {
          builder.mergeFrom(it)
        }.build() as T
      return protoObj
    }

    /**
     * Checks for a prohibited file naming pattern.
     *
     * @param repoPath the path of the repo
     * @param searchFiles a list of all the files which needs to be checked
     * @param filenameCheck proto object of FilenameCheck
     * @return file name pattern is correct or not
     */
    private fun checkProhibitedFileNamePattern(
      repoPath: String,
      searchFiles: List<File>,
      filenameCheck: FilenameCheck,
    ): Boolean {
      val prohibitedFilenameRegex = filenameCheck.getProhibitedFilenameRegex().toRegex()

      val matchedFiles = searchFiles.filter { file ->
        return@filter file.name !in filenameCheck.getExemptedFileNameList() &&
          prohibitedFilenameRegex.matches(
            RepoFile.retrieveFilePath(
              file,
              repoPath
            )
          )
      }

      logProhibitedFilenameFailure(
        repoPath,
        filenameCheck.getFailureMessage(),
        matchedFiles,
      )

      return matchedFiles.toList().size != 0
    }

    /**
     * Checks for a prohibited file content.
     *
     * @param repoPath the path of the repo
     * @param searchFiles a list of all the files which needs to be checked
     * @param fileContentCheck proto object of FileContentCheck
     * @return file content pattern is correct or not
     */
    private fun checkProhibitedContent(
      repoPath: String,
      searchFiles: List<File>,
      fileContentCheck: FileContentCheck,
    ): Boolean {
      val fileNameRegex = fileContentCheck.getFilenameRegex().toRegex()

      val prohibitedContentRegex =
        fileContentCheck.getProhibitedContentRegex().toRegex()

      val matchedFiles = searchFiles.filter { file ->
        file.name !in fileContentCheck.getExemptedFileNameList() &&
          fileNameRegex.matches(file.name) &&
          File(file.toString()).bufferedReader().lineSequence()
            .foldIndexed(initial = false) { lineIndex, isFailing, lineContent ->
              val matches = prohibitedContentRegex.matches(lineContent)
              if (matches) {
                logProhibitedContentFailure(
                  // Since, the line number starts from 1 and index starts
                  // from 0, therefore we have to increment index by 1, to
                  // denote the line number.
                  lineIndex + 1,
                  fileContentCheck.getFailureMessage(),
                  RepoFile.retrieveFilePath(file, repoPath)
                )
              }
              isFailing || matches
            }
      }

      return matchedFiles.size != 0
    }

    /** Logs the failures for filename pattern violation.
     *
     * @param repoPath the path of the repo to be analyzed
     * @param errorToShow the filename error to be logged
     * @param matchedFiles a list of all the files which had the filenaming violation
     * @return log the filename failures
     */
    private fun logProhibitedFilenameFailure(
      repoPath: String,
      errorToShow: String,
      matchedFiles: List<File>
    ) {
      if (matchedFiles.size != 0) {
        print("Filename pattern violation: $errorToShow\n")
        matchedFiles.forEach {
          print("Prohibited file: [ROOT]/${RepoFile.retrieveFilePath(it, repoPath)}\n")
        }
        println()
      }
    }

    /** Logs the failures for file content violation.
     *
     * @param lineNumberthe line number at which the failure occured
     * @param errorToShow the failure message to be logged
     * @param filePath the path of the file relative to the repository which failed the check
     * @return log the prohibited content failures
     */
    private fun logProhibitedContentFailure(
      lineNumber: Int,
      errorToShow: String,
      filePath: String
    ) {
      val failureMessage =
        """
          Prohibited content usage found on line no. $lineNumber
          File: [ROOT]/$filePath
          Failure message: $errorToShow
        """.trimIndent()
      println(failureMessage)
      println()
    }
  }
}
