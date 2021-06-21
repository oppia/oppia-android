package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.RegexPatternValidationCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.junit.rules.ExpectedException
import org.oppia.android.scripts.ScriptResultConstants

class RegexPatternValidationCheckTest {

  @Rule
  @JvmField
  public var tempFolder: TemporaryFolder = TemporaryFolder()

  @Rule
  @JvmField
  var thrown: ExpectedException = ExpectedException.none()

  @Before
  fun initTestFilesDirectory() {
    val testFilesDirectory = tempFolder.newFolder("testfiles")
  }

  @Test
  fun testFileNamePattern_validFileNamePattern_fileNamePatternIsCorrect() {
    val appLayerMimic = tempFolder.newFolder("testfiles", "app", "src", "main")
    val prohibitedFileNamePattern = tempFolder.newFile(
      "testfiles/app/src/main/TestActivity.kt"
    )

    runScript(tempFolder.getRoot().toString() + "/testfiles", arrayOf("app"))
  }

  @Test
  fun testFileNamePattern_prohibitedFileNamePattern_fileNamePatternIsNotCorrect() {
    val dataLayerMimic = tempFolder.newFolder("testfiles", "data", "src", "main")
    val prohibitedFileNamePattern = tempFolder.newFile(
      "testfiles/data/src/main/TestActivity.kt"
    )

    expectScriptFailure()

    runScript(tempFolder.getRoot().toString() + "/testfiles", arrayOf("data"))
  }

  @Test
  fun testFileContent_noSupportLibraryImport_fileContentIsCorrect() {
    val fileContainsNoSuppotLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")

    runScript()
  }

  @Test
  fun testFileContent_supportLibraryImport_fileContentIsNotCorrect() {
    val prohibitedContent = "import android.support.v7.app"
    val fileContainsSuppotLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")
    fileContainsSuppotLibraryImport.writeText(prohibitedContent)

    expectScriptFailure()

    runScript()
  }

  fun runScript(
    testDirectoryPath: String = tempFolder.getRoot().toString(),
    allowedDirectories: Array<String> = arrayOf("testfiles")) {
    RegexPatternValidationCheck.main(
      testDirectoryPath,
      *allowedDirectories
    )
  }

  fun expectScriptFailure() {
    thrown.expect(java.lang.Exception::class.java)
    thrown.expectMessage(ScriptResultConstants.REGEX_CHECKS_FAILED)
  }
}
