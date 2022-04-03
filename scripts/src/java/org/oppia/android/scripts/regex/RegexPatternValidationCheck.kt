package org.oppia.android.scripts.regex

import com.google.protobuf.MessageLite
import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.FileContentCheck
import org.oppia.android.scripts.proto.FileContentChecks
import org.oppia.android.scripts.proto.FilenameCheck
import org.oppia.android.scripts.proto.FilenameChecks
import java.io.File
import java.io.FileInputStream

/**
 * Script for ensuring that prohibited file contents and file naming patterns are not present in the
 * codebase.
 *
 * Usage:
 *   bazel run //scripts:pattern_validation_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:pattern_validation_check -- $(pwd)
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"
  val repoRoot = File(repoPath)

  // A list of all files in the repo to be analyzed.
  val searchFiles = RepositoryFile.collectSearchFiles(repoPath)

  // Check if the repo has any filename failure.
  val hasFilenameCheckFailure = retrieveFilenameChecks()
    .fold(initial = false) { hasFailingFile, filenameCheck ->
      val fileFails = checkProhibitedFileNamePattern(
        repoRoot,
        searchFiles,
        filenameCheck,
      )
      return@fold hasFailingFile || fileFails
    }

  // Check if the repo has any file content failure.
  val contentChecks = retrieveFileContentChecks().map { MatchableFileContentCheck.createFrom(it) }
  val hasFileContentCheckFailure =
    searchFiles.fold(initial = false) { hasFailingFile, searchFile ->
      val fileFails = checkProhibitedContent(
        repoRoot,
        searchFile,
        contentChecks
      )
      return@fold hasFailingFile || fileFails
    }

  if (hasFilenameCheckFailure || hasFileContentCheckFailure) {
    println(
      "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
        "#regexpatternvalidation-check for more details on how to fix this.\n"
    )
  }

  if (hasFilenameCheckFailure || hasFileContentCheckFailure) {
    throw Exception("REGEX PATTERN CHECKS FAILED")
  } else {
    println("REGEX PATTERN CHECKS PASSED")
  }
}

/**
 * Retrieves all filename checks.
 *
 * @return a list of all the FilenameChecks
 */
private fun retrieveFilenameChecks(): List<FilenameCheck> {
  return loadProto(
    "filename_pattern_validation_checks.pb",
    FilenameChecks.getDefaultInstance()
  ).filenameChecksList
}

/**
 * Retrieves all file content checks.
 *
 * @return a list of all the FileContentChecks
 */
private fun retrieveFileContentChecks(): List<FileContentCheck> {
  return loadProto(
    "file_content_validation_checks.pb",
    FileContentChecks.getDefaultInstance()
  ).fileContentChecksList
}

/**
 * Helper function to parse the textproto file to a proto class.
 *
 * @param textProtoFileName name of the textproto file to be parsed
 * @param proto instance of the proto class
 * @return proto class from the parsed textproto file
 */
private fun <T : MessageLite> loadProto(textProtoFileName: String, proto: T): T {
  val protoBinaryFile = File("scripts/assets/$textProtoFileName")
  val builder = proto.newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  return FileInputStream(protoBinaryFile).use {
    builder.mergeFrom(it)
  }.build() as T
}

/**
 * Checks for a prohibited file naming pattern.
 *
 * @param repoRoot the root directory of the repo
 * @param searchFiles a list of all the files which needs to be checked
 * @param filenameCheck proto object of FilenameCheck
 * @return whether the file name pattern is correct or not
 */
private fun checkProhibitedFileNamePattern(
  repoRoot: File,
  searchFiles: List<File>,
  filenameCheck: FilenameCheck,
): Boolean {
  val prohibitedFilenameRegex = filenameCheck.prohibitedFilenameRegex.toRegex()

  val matchedFiles = searchFiles.filter { file ->
    val fileRelativePath = file.toRelativeString(repoRoot)
    return@filter fileRelativePath !in filenameCheck.exemptedFileNameList &&
      prohibitedFilenameRegex.matches(fileRelativePath)
  }

  logProhibitedFilenameFailure(repoRoot, filenameCheck.failureMessage, matchedFiles)
  return matchedFiles.isNotEmpty()
}

/**
 * Checks for a prohibited file content.
 *
 * @param repoRoot the root directory of the repo
 * @param searchFile the file to check for prohibited content
 * @param fileContentChecks contents to check for validity
 * @return whether the file content pattern is correct or not
 */
private fun checkProhibitedContent(
  repoRoot: File,
  searchFile: File,
  fileContentChecks: Iterable<MatchableFileContentCheck>
): Boolean {
  val lines = searchFile.readLines()
  return fileContentChecks.fold(initial = false) { hasFailingFile, fileContentCheck ->
    val fileRelativePath = searchFile.toRelativeString(repoRoot)
    val fileFails = if (fileContentCheck.isFileAffectedByCheck(fileRelativePath)) {
      val affectedLines = fileContentCheck.computeAffectedLines(lines)
      if (affectedLines.isNotEmpty()) {
        affectedLines.forEach { lineIndex ->
          logProhibitedContentFailure(
            lineIndex + 1, // Increment by 1 since line numbers begin at 1 rather than 0.
            fileContentCheck.failureMessage,
            fileRelativePath
          )
        }
      }
      affectedLines.isNotEmpty()
    } else false
    return@fold hasFailingFile || fileFails
  }
}

/**
 * Logs the failures for filename pattern violation.
 *
 * @param repoRoot the root directory of the repo
 * @param errorToShow the filename error to be logged
 * @param matchedFiles a list of all the files which had the filenaming violation
 */
private fun logProhibitedFilenameFailure(
  repoRoot: File,
  errorToShow: String,
  matchedFiles: List<File>
) {
  if (matchedFiles.isNotEmpty()) {
    println("File name/path violation: $errorToShow")
    matchedFiles.forEach {
      println("- ${it.toRelativeString(repoRoot)}")
    }
    println()
  }
}

/**
 * Logs the failures for file content violation.
 *
 * @param lineNumber the line number at which the failure occured
 * @param errorToShow the failure message to be logged
 * @param filePath the path of the file relative to the repository which failed the check
 */
private fun logProhibitedContentFailure(
  lineNumber: Int,
  errorToShow: String,
  filePath: String
) {
  val failureMessage = "$filePath:$lineNumber: $errorToShow"
  println(failureMessage)
}

/** A matchable version of [FileContentCheck]. */
private data class MatchableFileContentCheck(
  val filePathRegex: Regex,
  val prohibitedContentRegex: Regex,
  val failureMessage: String,
  val exemptedFileNames: List<String>,
  val exemptedFilePatterns: List<Regex>
) {
  /**
   * Returns whether the relative file given by the specified path should be affected by this check
   * (i.e. that it matches the inclusion pattern and is not explicitly or implicitly excluded).
   */
  fun isFileAffectedByCheck(relativePath: String): Boolean =
    filePathRegex.containsMatchIn(relativePath) && !isFileExempted(relativePath)

  /**
   * Returns the list of line indexes which contain prohibited content per this check (given an
   * iterable of lines). Note that the returned indexes are based on the iteration order of the
   * provided iterable.
   */
  fun computeAffectedLines(lines: Iterable<String>): List<Int> {
    return lines.withIndex().filter { (_, line) ->
      prohibitedContentRegex.containsMatchIn(line)
    }.map { (index, _) -> index }
  }

  private fun isFileExempted(relativePath: String): Boolean {
    return relativePath in exemptedFileNames ||
      exemptedFilePatterns.any { it.containsMatchIn(relativePath) }
  }

  companion object {
    /** Returns a new [MatchableFileContentCheck] based on the specified [FileContentCheck]. */
    fun createFrom(fileContentCheck: FileContentCheck): MatchableFileContentCheck {
      return MatchableFileContentCheck(
        filePathRegex = fileContentCheck.filePathRegex.toRegex(),
        prohibitedContentRegex = fileContentCheck.prohibitedContentRegex.toRegex(),
        failureMessage = fileContentCheck.failureMessage,
        exemptedFileNames = fileContentCheck.exemptedFileNameList,
        exemptedFilePatterns = fileContentCheck.exemptedFilePatternsList.map { it.toRegex() }
      )
    }
  }
}
