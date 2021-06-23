package org.oppia.android.scripts

import java.io.File
import java.io.FileInputStream
import org.oppia.android.scripts.proto.FilenameChecks
import org.oppia.android.scripts.proto.FilenameCheck
import org.oppia.android.scripts.proto.FileContentChecks
import org.oppia.android.scripts.proto.FileContentCheck
import org.oppia.android.scripts.ScriptResultConstants
import com.google.protobuf.MessageLite

/** Script for ensuring that prohibited file contents and
 *  file naming patterns are not present in the codebase.
 */
class RegexPatternValidationCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      /** path of the repo to be analyzed. */
      val repoPath = args[0] + "/"

      /** a list of all allowed directories in the repo to be analyzed. */
      val allowedDirectories = args.drop(1)

      /** a list of all files in the repo to be analyzed. */
      val searchFiles = RepoFile.collectSearchFiles(
        repoPath = repoPath,
        allowedDirectories = allowedDirectories
      )

      /** check if the repo has any filename failure. */
      val hasFilenameCheckFailure = retrieveFilenameChecks()
        .fold(initial = false) { isFailing, check ->
          val checkFailed = checkProhibitedFileNamePattern(
            repoPath = repoPath,
            searchFiles = searchFiles,
            prohibitedFilenameObj = check,
          )
          return@fold isFailing || checkFailed
        }

      /** check if the repo has any file content failure. */
      val hasFileContentCheckFailure = retrieveFileContentChecks()
        .fold(initial = false) { isFailing, check ->
          val checkFailed = checkProhibitedContent(
            repoPath = repoPath,
            searchFiles = searchFiles,
            prohibitedContentObj = check,
          )
          return@fold isFailing || checkFailed
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
        textProtoFileName = "filename_pattern_validation_checks.pb",
        proto = FilenameChecks.getDefaultInstance()
      ).getFilenameChecksList()
    }

    /**
     * Retrieves all file content checks.
     *
     * @return a list of all the FileContentChecks
     */
    private fun retrieveFileContentChecks(): List<FileContentCheck> {
      return getProto(
        textProtoFileName = "file_content_validation_checks.pb",
        proto = FileContentChecks.getDefaultInstance()
      ).getFileContentChecksList()
    }

    /**
     * helper function to parse the textproto file to a proto class.
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
     * @param prohibitedFilenameObj proto object of FilenameCheck
     * @return check failed or passed
     */
    private fun checkProhibitedFileNamePattern(
      repoPath: String,
      searchFiles: List<File>,
      prohibitedFilenameObj: FilenameCheck,
    ): Boolean {
      val prohibitedFilenameRegex = prohibitedFilenameObj.getProhibitedFilenameRegex().toRegex()
      val matchedFiles = searchFiles.filter { file ->
        return@filter file.name !in prohibitedFilenameObj.getExemptedFileNameList()
          && prohibitedFilenameRegex.matches(
          RepoFile.retrieveFilePath(
            file,
            repoPath)
        )
      }

      logProhibitedFilenameFailure(
        repoPath = repoPath,
        matchedFiles = matchedFiles,
        errorToShow = prohibitedFilenameObj.getFailureMessage()
      )

      return matchedFiles.toList().size != 0
    }

    /**
     * Checks for a prohibited file content.
     *
     * @param repoPath the path of the repo
     * @param searchFiles a list of all the files which needs to be checked
     * @param prohibitedContentObj proto object of FileContentCheck
     * @return check failed or passed
     */
    private fun checkProhibitedContent(
      repoPath: String,
      searchFiles: List<File>,
      prohibitedContentObj: FileContentCheck,
    ): Boolean {
      var contentCheckFailedFlag = false
      val fileNameRegex = prohibitedContentObj.getFilenameRegex().toRegex()
      val prohibitedContentRegex = prohibitedContentObj.getProhibitedContentRegex().toRegex()
      val matchedFiles = searchFiles.filter {
        it.name !in prohibitedContentObj.getExemptedFileNameList()
          && fileNameRegex.matches(it.name)
          && File(it.toString()).bufferedReader().lineSequence()
          .foldIndexed(initial = false) { lineNumber, acc, lineStr ->
            val matches = prohibitedContentRegex.matches(lineStr)
            if (matches) {
              logProhibitedContentFailure(
                lineNumber = lineNumber + 1,
                errorToShow = prohibitedContentObj.getFailureMessage(),
                filePath = RepoFile.retrieveFilePath(it, repoPath)
              )
            }
            return@foldIndexed acc || matches
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
        print("\n")
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
      filePath: String) {
      println(
        "Prohibited content usage found on line no. $lineNumber\n" +
          "File: [ROOT]/$filePath\n" +
          "Failure message: $errorToShow\n"
      )
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
        allowedDirectories: List<String>)
        : List<File> {
        return File(repoPath).walk().filter { it ->
          checkIfAllowedDirectory(
            retrieveFilePath(it, repoPath),
            allowedDirectories)
            && it.isFile
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
