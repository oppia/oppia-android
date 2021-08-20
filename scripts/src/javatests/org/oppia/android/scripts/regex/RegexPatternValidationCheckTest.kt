package org.oppia.android.scripts.regex

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertion.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [RegexPatternValidationCheck]. */
class RegexPatternValidationCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val REGEX_CHECK_PASSED_OUTPUT_INDICATOR: String = "REGEX PATTERN CHECKS PASSED"
  private val REGEX_CHECK_FAILED_OUTPUT_INDICATOR: String = "REGEX PATTERN CHECKS FAILED"
  private val supportLibraryUsageErrorMessage = "AndroidX should be used instead of the support " +
    "library"
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#regexpatternvalidation-check for more details on how to fix this."

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  @Test
  fun testFileNamePattern_activityInAppModule_fileNamePatternIsCorrect() {
    tempFolder.newFolder("testfiles", "app", "src", "main")
    tempFolder.newFile("testfiles/app/src/main/TestActivity.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileNamePattern_activityInTestingModule_fileNamePatternIsCorrect() {
    tempFolder.newFolder("testfiles", "testing", "src", "main")
    tempFolder.newFile("testfiles/testing/src/main/TestActivity.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileNamePattern_activityInDataModule_fileNamePatternIsNotCorrect() {
    tempFolder.newFolder("testfiles", "data", "src", "main")
    tempFolder.newFile("testfiles/data/src/main/TestActivity.kt")

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim()).isEqualTo(
      """
      File name/path violation: Activities cannot be placed outside the app or testing module
      - ${retrieveTestFilesDirectoryPath()}/data/src/main/TestActivity.kt
      
      $wikiReferenceNote
      """.trimIndent()
    )
  }

  @Test
  fun testFileContent_noSupportLibraryImport_fileContentIsCorrect() {
    tempFolder.newFile("testfiles/TestFile.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(REGEX_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testFileContent_supportLibraryImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import android.support.v7.app"
    val fileContainsSuppotLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSuppotLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim())
      .isEqualTo(
        """
        ${retrieveTestFilesDirectoryPath()}/TestFile.kt:1: $supportLibraryUsageErrorMessage
        $wikiReferenceNote
        """.trimIndent()
      )
  }

  @Test
  fun testFilenameAndContent_useProhibitedFileName_useProhibitedFileContent_multipleFailures() {
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val prohibitedFile = tempFolder.newFile("testfiles/data/src/main/TestActivity.kt")
    val prohibitedContent = "import android.support.v7.app"
    prohibitedFile.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(REGEX_CHECK_FAILED_OUTPUT_INDICATOR)
    assertThat(outContent.toString().trim()).isEqualTo(
      """
      File name/path violation: Activities cannot be placed outside the app or testing module
      - ${retrieveTestFilesDirectoryPath()}/data/src/main/TestActivity.kt
      
      ${retrieveTestFilesDirectoryPath()}/data/src/main/TestActivity.kt:1: AndroidX should be used instead of the support library
      $wikiReferenceNote
      """.trimIndent()
    )
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /** Runs the regex_pattern_validation_check. */
  private fun runScript() {
    main(retrieveTestFilesDirectoryPath())
  }
}
