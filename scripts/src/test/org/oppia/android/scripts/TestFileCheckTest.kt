package org.oppia.android.scripts

import java.io.File
import kotlin.test.assertEquals
import org.oppia.android.scripts.TestFileCheck
import org.junit.Test
import org.junit.Rule
import org.junit.Before
import org.junit.rules.TemporaryFolder
import org.junit.rules.ExpectedException

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
  fun test_testFilePresent_scriptCheckShouldPass(){
    val tempProdFile = tempFolder.newFile("testfiles/ProdFile.kt")
    val tempProdFileTest = tempFolder.newFile("testfiles/ProdFileTest.kt")

    runScript()
  }

  @Test
  fun test_testFileNotPresent_scriptCheckShouldFail(){
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
    thrown.expectMessage("TEST FILE CHECK FAILED")
  }
}
