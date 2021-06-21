package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.TestFileCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.junit.rules.ExpectedException
import org.oppia.android.scripts.ScriptResultConstants

class TestFileCheckTest {
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
  fun testTestFileCheck_addTestFile_testFileIsPresent(){
    val tempProdFile = tempFolder.newFile("testfiles/ProdFile.kt")
    val tempProdFileTest = tempFolder.newFile("testfiles/ProdFileTest.kt")

    runScript()
  }

  @Test
  fun testTestFileCheck_missTestFile_testFileIsNotPresent(){
    val tempProdFile1 = tempFolder.newFile("testfiles/ProdFile1.kt")
    val tempProdFile1Test = tempFolder.newFile("testfiles/ProdFile1Test.kt")
    val tempProdFile2 = tempFolder.newFile("testfiles/ProdFile2.kt")

    expectScriptFailure()

    runScript()
  }

  fun runScript() {
    TestFileCheck.main(tempFolder.getRoot().toString(), "testfiles")
  }

  fun expectScriptFailure() {
    thrown.expect(java.lang.Exception::class.java)
    thrown.expectMessage(ScriptResultConstants.TEST_FILE_CHECK_FAILED)
  }
}
