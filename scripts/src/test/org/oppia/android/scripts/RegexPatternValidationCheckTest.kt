package org.oppia.android.scripts

import java.io.File
import org.oppia.android.scripts.RegexPatternValidationCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.After
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.ScriptResultConstants
import com.google.common.truth.Truth.assertThat
import org.oppia.android.testing.assertThrows
import java.io.PrintStream
import java.io.ByteArrayOutputStream

class RegexPatternValidationCheckTest {

  @Rule
  @JvmField
  public var tempFolder: TemporaryFolder = TemporaryFolder()

  @Before
  fun initTestFilesDirectory() {
    tempFolder.newFolder("testfiles")
  }

  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = java.lang.System.out

  @Before
  fun setUpStreams() {
    java.lang.System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    java.lang.System.setOut(originalOut)
  }

  @Test
  fun testFileNamePattern_validFileNamePattern_fileNamePatternIsCorrect() {
    tempFolder.newFolder("testfiles", "app", "src", "main")
    tempFolder.newFile("testfiles/app/src/main/TestActivity.kt")

    runScript(tempFolder.getRoot().toString() + "/testfiles", arrayOf("app"))

    assertThat(outContent.toString().trim()).isEqualTo(
      ScriptResultConstants.REGEX_CHECKS_PASSED
    )
  }

  @Test
  fun testFileNamePattern_prohibitedFileNamePattern_fileNamePatternIsNotCorrect() {
    tempFolder.newFolder("testfiles", "data", "src", "main")
    tempFolder.newFile("testfiles/data/src/main/TestActivity.kt")

    val exception = assertThrows(Exception::class) {
      runScript(
        tempFolder.getRoot().toString() + "/testfiles",
        arrayOf("data")
      )
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.REGEX_CHECKS_FAILED)
    assertThat(outContent.toString().trim()).isEqualTo(
      "Filename pattern violation: Activities cannot be placed in the data module\n" +
        "Prohibited file: [ROOT]/data/src/main/TestActivity.kt"
    )
  }

  @Test
  fun testFileContent_noSupportLibraryImport_fileContentIsCorrect() {
    tempFolder.newFile("testfiles/TestFile.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(
      ScriptResultConstants.REGEX_CHECKS_PASSED
    )
  }

  @Test
  fun testFileContent_supportLibraryImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import android.support.v7.app"
    val fileContainsSuppotLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSuppotLibraryImport.writeText(prohibitedContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.REGEX_CHECKS_FAILED)
    assertThat(
      "Prohibited content usage found on line no. 1\n" +
        "File: [ROOT]/testfiles/TestFile.kt\n" +
        "Failure message: AndroidX should be used instead of the support library"
    ).isEqualTo(
      outContent.toString().trim())
  }

  @Test
  fun testMultipleFailures_useProhibitedFileNameAndFileContent_MultipleFailuresShouldBeLogged() {
    tempFolder.newFolder("testfiles", "data", "src", "main")
    val prohibitedFile = tempFolder.newFile(
      "testfiles/data/src/main/TestActivity.kt"
    )
    val prohibitedContent = "import android.support.v7.app"
    prohibitedFile.writeText(prohibitedContent)


    val exception = assertThrows(Exception::class) {
      runScript(
        tempFolder.getRoot().toString() + "/testfiles",
        arrayOf("data")
      )
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.REGEX_CHECKS_FAILED)
    assertThat(outContent.toString().trim()).isEqualTo(
      "Filename pattern violation: Activities cannot be placed in the data module\n" +
        "Prohibited file: [ROOT]/data/src/main/TestActivity.kt\n\n" +
        "Prohibited content usage found on line no. 1\n" +
        "File: [ROOT]/data/src/main/TestActivity.kt\n" +
        "Failure message: AndroidX should be used instead of the support library"
    )
  }

  private fun runScript(
    testDirectoryPath: String = tempFolder.getRoot().toString(),
    allowedDirectories: Array<String> = arrayOf("testfiles")) {
    RegexPatternValidationCheck.main(
      testDirectoryPath,
      *allowedDirectories
    )
  }
}
