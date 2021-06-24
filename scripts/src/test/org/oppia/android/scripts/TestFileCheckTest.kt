package org.oppia.android.scripts

import java.io.File
import org.oppia.android.scripts.TestFileCheck
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

class TestFileCheckTest {
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
  fun testTestFileCheck_prodFileWithCorrespondingTestFile_testFileIsPresent() {
    tempFolder.newFile("testfiles/ProdFile.kt")
    tempFolder.newFile("testfiles/ProdFileTest.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(
      ScriptResultConstants.TEST_FILE_CHECK_PASSED
    )
  }

  @Test
  fun testTestFileCheck_missTestFile_testFileIsNotPresent() {
    tempFolder.newFile("testfiles/ProdFile1.kt")
    tempFolder.newFile("testfiles/ProdFile1Test.kt")
    tempFolder.newFile("testfiles/ProdFile2.kt")

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.TEST_FILE_CHECK_FAILED)
    assertThat(outContent.toString().trim()).isEqualTo(
      "No test file found for:\nProdFile2.kt"
    )
  }

  @Test
  fun testTestFileCheck_missTestFilesForMultipleProdFiles_testFileIsNotPresent() {
    tempFolder.newFile("testfiles/ProdFile1.kt")
    tempFolder.newFile("testfiles/ProdFile1Test.kt")
    tempFolder.newFile("testfiles/ProdFile2.kt")
    tempFolder.newFile("testfiles/ProdFile3.kt")

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(ScriptResultConstants.TEST_FILE_CHECK_FAILED)
    assertThat(outContent.toString().trim()).isEqualTo(
      "No test file found for:\nProdFile3.kt\nProdFile2.kt"
    )
  }

  fun runScript() {
    TestFileCheck.main(tempFolder.getRoot().toString(), "testfiles")
  }
}
