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
  fun testFilenamePattern_checkShouldPass() {
    val allowedDirectory = arrayOf("app")
    val searchFiles = RegexPatternValidationCheck.
    collectSearchFiles(testDirectoryPath + "filenamepattern/pass/testrepo/", allowedDirectory)
    val activityFilePatternRegexObj = filenameChecks.get(0)
    val scriptResult = RegexPatternValidationCheck.checkProhibitedFileNamePattern(
      repoPath = testDirectoryPath + "filenamepattern/pass/testrepo/",
      searchFiles = searchFiles,
      prohibitedFilenameRegexString = activityFilePatternRegexObj.getProhibitedFilenameRegex(),
      errorToShow = activityFilePatternRegexObj.getFailureMessage()
    )

    assertEquals(expected = false, actual = scriptResult)
  }

  @Test
  fun testFilenamePattern_checkShouldFail() {
    val allowedDirectory = arrayOf("data")
    val searchFiles = RegexPatternValidationCheck.
    collectSearchFiles(testDirectoryPath + "filenamepattern/fail/testrepo/", allowedDirectory)
    val activityFilePatternRegexObj = filenameChecks.get(0)
    val scriptResult = RegexPatternValidationCheck.checkProhibitedFileNamePattern(
      repoPath = testDirectoryPath + "filenamepattern/fail/testrepo/",
      searchFiles = searchFiles,
      prohibitedFilenameRegexString = activityFilePatternRegexObj.getProhibitedFilenameRegex(),
      errorToShow = activityFilePatternRegexObj.getFailureMessage()
    )

    assertEquals(expected = true, actual = scriptResult)
  }

  @Test
  fun testFileContent_noProhibitedContentUsed_checkShouldPass() {
    val allowedDirectory = arrayOf("filecontent/pass")
    val searchFiles = RegexPatternValidationCheck.
    collectSearchFiles(testDirectoryPath, allowedDirectory)
    val supportLibraryRegexObj = fileContentChecks.get(0)
    val scriptResult = RegexPatternValidationCheck.checkProhibitedContent(
      repoPath = testDirectoryPath,
      searchFiles = searchFiles,
      fileNameRegexString = supportLibraryRegexObj.getFilenameRegex(),
      prohibitedContentRegexString = supportLibraryRegexObj.getProhibitedContentRegex(),
      errorToShow = supportLibraryRegexObj.getFailureMessage()
    )

    assertEquals(expected = false, actual = scriptResult)
  }

  @Test
  fun testFileContent_prohibitedContentUsed_checkShouldFail() {
    val allowedDirectory = arrayOf("filecontent/fail")
    val searchFiles = RegexPatternValidationCheck.
    collectSearchFiles(testDirectoryPath, allowedDirectory)
    val supportLibraryRegexObj = fileContentChecks.get(0)
    val scriptResult = RegexPatternValidationCheck.checkProhibitedContent(
      repoPath = testDirectoryPath,
      searchFiles = searchFiles,
      fileNameRegexString = supportLibraryRegexObj.getFilenameRegex(),
      prohibitedContentRegexString = supportLibraryRegexObj.getProhibitedContentRegex(),
      errorToShow = supportLibraryRegexObj.getFailureMessage()
    )

    assertEquals(expected = true, actual = scriptResult)
  }

}

