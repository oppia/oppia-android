package org.oppia.android.scripts

import java.io.File
import java.io.FileInputStream
import org.oppia.android.app.model.FilenameChecks
import org.oppia.android.app.model.FileContentChecks

class RegexPatternValidationCheck {
  companion object {
    @JvmStatic
    fun main(vararg args: String) {
      val path = System.getProperty("user.dir")
      val fileNamePatternsBinaryFile =
        File("$path/scripts/assets/filename_pattern_validation_checks.pb")
      val fileContentsBinaryFile =
        File("$path/scripts/assets/file_content_validation_checks.pb")
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

      namePatternsObj.getFilenameChecksList().forEach {
        println(it.getProhibitedFilenameRegex())
        println(it.getFailureMessage())
        println("------------------------------------")
      }

      fileContentsObj.getFileContentChecksList().forEach {
        println(it.getFilenameRegex())
        println(it.getFailureMessage())
        println(it.getProhibitedContentRegex())
        println("------------------------------------")
      }

    }
  }
}