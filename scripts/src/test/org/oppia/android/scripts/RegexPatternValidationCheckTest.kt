package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.RegexPatternValidationCheck
import org.junit.Test

class RegexPatternValidationCheckTest {

  private val testDirectoryPath = System.getProperty("user.dir") + "/scripts/src/test/testfiles/"
  private val filenameChecks = RegexPatternValidationCheck.getFilenameChecks()
  private val fileContentChecks = RegexPatternValidationCheck.getFileContentChecks()

  @Test
  fun no_Prohibited_Content_Check_Should_Pass() {
    val passDirectory = arrayOf("filecontent/pass")
    val searchFiles = RegexPatternValidationCheck.
    collectSearchFiles(testDirectoryPath, passDirectory)
    val supportLibraryRegexObj = fileContentChecks.get(0)
    val scriptResult = RegexPatternValidationCheck.checkProhibitedContent(
      repoPath = testDirectoryPath,
      searchFiles = searchFiles,
      fileNameRegexString = supportLibraryRegexObj.getFilenameRegex(),
      prohibitedContentRegexString = supportLibraryRegexObj.getProhibitedContentRegex(),
      errorToShow = supportLibraryRegexObj.getFailureMessage()
    )

    assertEquals(false, scriptResult)
  }

  @Test
  fun prohibited_Content_Check_Should_Fail() {
    val failDirectory = arrayOf("filecontent/fail")
    val searchFiles = RegexPatternValidationCheck.
    collectSearchFiles(testDirectoryPath, failDirectory)
    val supportLibraryRegexObj = fileContentChecks.get(0)
    val scriptResult = RegexPatternValidationCheck.checkProhibitedContent(
      repoPath = testDirectoryPath,
      searchFiles = searchFiles,
      fileNameRegexString = supportLibraryRegexObj.getFilenameRegex(),
      prohibitedContentRegexString = supportLibraryRegexObj.getProhibitedContentRegex(),
      errorToShow = supportLibraryRegexObj.getFailureMessage()
    )

    assertEquals(true, scriptResult)
  }

}
