package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.RegexPatternValidationCheck
import org.junit.Test

class RegexPatternValidationCheckTest {

  private val repoPath = System.getProperty("user.dir") + "/"
  private val testDirectory = arrayOf("scripts/src/test/dummyfiles")
  private val searchFiles = RegexPatternValidationCheck.collectSearchFiles(repoPath, testDirectory)

  @Test
  fun prohibited_Content_Check_Should_Fail() {
    assertEquals(
      true,
      RegexPatternValidationCheck.checkProhibitedContent(
        repoPath = repoPath,
        searchFiles = searchFiles,
        fileNameRegexString = "DummyFile1.kt",
        prohibitedContentRegexString = "^import .+?support.+?$",
        errorToShow = "AndroidX should be used instead of the support library"
      ))
  }

  @Test
  fun no_Prohibited_Content_Check_Should_Pass() {
    assertEquals(
      false,
      RegexPatternValidationCheck.checkProhibitedContent(
        repoPath = repoPath,
        searchFiles = searchFiles,
        fileNameRegexString = "DummyFile2.kt",
        prohibitedContentRegexString = "^import .+?support.+?$",
        errorToShow = "AndroidX should be used instead of the support library"
      ))
  }

}
