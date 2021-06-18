package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.RegexPatternValidationCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.junit.rules.ExpectedException

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
  fun test_fileNamePattern_validFileNamePattern_scriptShouldPass() {
    val appLayerMimic = tempFolder.newFolder("testfiles", "app", "src", "main")
    val prohibitedFileNamePattern = tempFolder.newFile(
      "testfiles/app/src/main/TestActivity.kt"
    )

    runScript(tempFolder.getRoot().toString() + "/testfiles", arrayOf("app"))
  }

  @Test
  fun test_fileNamePattern_useProhibitedFileNamePattern_scriptShouldFail() {
    val dataLayerMimic = tempFolder.newFolder("testfiles", "data", "src", "main")
    val prohibitedFileNamePattern = tempFolder.newFile(
      "testfiles/data/src/main/TestActivity.kt"
    )

    expectScriptFailure()

    runScript(tempFolder.getRoot().toString() + "/testfiles", arrayOf("data"))
  }

  @Test
  fun test_fileContent_noProhibitedSupportLibraryImport_scriptShouldPass() {
    val fileContainsNoSuppotLibraryImport = tempFolder.newFile("testfiles/TestFile.kt")

    runScript()
  }

  @Test
  fun test_fileContent_useProhibitedSupportLibraryImport_scriptShouldFail() {
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
    thrown.expectMessage("REGEX PATTERN CHECKS FAILED")
  }
}
