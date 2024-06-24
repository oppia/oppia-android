package org.oppia.android.scripts.testfile

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.TestFileExemptions
import org.oppia.android.scripts.proto.TestFileExemptions.TestFileExemption
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [TestFileCheck]. */
class TestFileCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR = "TEST FILE CHECK PASSED"
  private val TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR = "TEST FILE CHECK FAILED"
  private val errorMessage = "does not have a corresponding test file."
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#test-file-presence-check for more details on how to fix this."

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

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
  fun testTestFileCheck_prodFileWithCorrespondingTestFile_testFileIsPresent() {
    tempFolder.newFile("testfiles/ProdFile.kt")
    tempFolder.newFile("testfiles/ProdFileTest.kt")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTestFileCheck_missTestFile_testFileIsNotPresent() {
    tempFolder.newFile("testfiles/ProdFile1.kt")
    tempFolder.newFile("testfiles/ProdFile1Test.kt")
    tempFolder.newFile("testfiles/ProdFile2.kt")

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      File ${retrieveTestFilesDirectoryPath()}/ProdFile2.kt $errorMessage

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTestFileCheck_missTestFilesForMultipleProdFiles_testFileIsNotPresent() {
    tempFolder.newFile("testfiles/ProdFile1.kt")
    tempFolder.newFile("testfiles/ProdFile1Test.kt")
    tempFolder.newFile("testfiles/ProdFile2.kt")
    tempFolder.newFile("testfiles/ProdFile3.kt")

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      File ${retrieveTestFilesDirectoryPath()}/ProdFile2.kt does not have a corresponding test file.
      File ${retrieveTestFilesDirectoryPath()}/ProdFile3.kt does not have a corresponding test file.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTestFileCheck_missTestFilesForMultipleProdFiles_logsShouldBeLexicographicallySorted() {
    tempFolder.newFile("testfiles/ProdFile1.kt")
    tempFolder.newFile("testfiles/ProdFile1Test.kt")
    tempFolder.newFile("testfiles/ProdFile3.kt")
    tempFolder.newFile("testfiles/ProdFile2.kt")

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      File ${retrieveTestFilesDirectoryPath()}/ProdFile2.kt does not have a corresponding test file.
      File ${retrieveTestFilesDirectoryPath()}/ProdFile3.kt does not have a corresponding test file.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTestFileCheck_exemptedFile_testFileIsNotRequired() {
    tempFolder.newFolder(
      "testfiles", "app", "src", "main", "java", "org", "oppia", "android", "app", "activity"
    )
    tempFolder.newFile(
      "testfiles/app/src/main/java/org/oppia/android/app/activity/ActivityModule.kt"
    )

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTestFileCheck_emptyDirectory_scriptCheckShouldPass() {
    tempFolder.newFolder("testfiles", "testfolder")

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTestFileCheck_minCoverageExemptedFile_testFileIsRequired() {
    tempFolder.newFile("testfiles/ProdFile1.kt")
    tempFolder.newFile("testfiles/ProdFile2.kt")
    tempFolder.newFile("testfiles/ProdFile2Test.kt")

    val testFileExemptions = TestFileExemptions.newBuilder()
      .addTestFileExemption(
        TestFileExemption.newBuilder()
          .setExemptedFilePath("testfiles/ProdFile1.kt")
          .setTestFileNotRequired(true)
      )
      .addTestFileExemption(
        TestFileExemption.newBuilder()
          .setExemptedFilePath("testfiles/ProdFile2.kt")
          .setOverrideMinCoveragePercentRequired(5)
      )
      .build()

    val coverageTestExemptiontextProto = tempFolder.newFile("test_exemption.pb")
    coverageTestExemptiontextProto.outputStream().use {
      (testFileExemptions.writeTo(it))
    }

    TestFileCheck(
      "${tempFolder.root}",
      "${tempFolder.root}/test_exemption"
    ).execute()

    assertThat(outContent.toString().trim()).isEqualTo(TEST_FILE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTestFileCheck_missTestFilesForCoverageExemptedFile_testFileIsNotPresent() {

    tempFolder.newFile("testfiles/ProdFile1.kt")
    tempFolder.newFile("testfiles/ProdFile2.kt")

    val testFileExemptions = TestFileExemptions.newBuilder()
      .addTestFileExemption(
        TestFileExemption.newBuilder()
          .setExemptedFilePath("testfiles/ProdFile1.kt")
          .setTestFileNotRequired(true)
      )
      .addTestFileExemption(
        TestFileExemption.newBuilder()
          .setExemptedFilePath("testfiles/ProdFile2.kt")
          .setOverrideMinCoveragePercentRequired(5)
      )
      .build()

    val coverageTestExemptiontextProto = tempFolder.newFile("test_exemption.pb")
    coverageTestExemptiontextProto.outputStream().use {
      (testFileExemptions.writeTo(it))
    }

    val exception = assertThrows<Exception>() {
      TestFileCheck(
        "${tempFolder.root}",
        "${tempFolder.root}/test_exemption"
      ).execute()
    }

    assertThat(exception).hasMessageThat().contains(TEST_FILE_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      File ${retrieveTestFilesDirectoryPath()}/ProdFile2.kt does not have a corresponding test file.

      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /** Runs the test_file_check. */
  private fun runScript() {
    main(retrieveTestFilesDirectoryPath())
  }
}
