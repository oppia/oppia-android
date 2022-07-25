package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for [TodoIssueResolvedCheck]. */
class TodoIssueResolvedCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val CLOSED_ISSUE_CHECK_PASSED_OUTPUT_INDICATOR: String =
    "TODO ISSUE RESOLVED CHECK PASSED"
  private val CLOSED_ISSUE_CHECK_FAILED_OUTPUT_INDICATOR: String =
    "TODO ISSUE RESOLVED CHECK FAILED"
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#todo-issue-resolved-check for more details on how to fix this."

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
  fun testClosedIssueCheck_noTodosPresent_checkShouldPass() {
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.bazel")
    val testContent1 =
      """
      // Test comment 1
      
      // Test comment 2
      """.trimIndent()
    val testContent2 =
      """
      # test comment 3
      # test todo which is not a todo
      # test comment 4
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    main(retrieveTestFilesDirectoryPath(), "1200", "abmzuyt")

    assertThat(outContent.toString().trim()).isEqualTo(CLOSED_ISSUE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testClosedIssueCheck_noTodoCorrespondsToClosedIssue_checkShouldPass() {
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.bazel")
    val testContent1 =
      """
      // TODO(#169877): test description 1
      
      // TODO(#1021211): test description 2
      """.trimIndent()
    val testContent2 =
      """
      # test comment 3
      # test todo which is not a todo
      # TODO(#1400000): test description 3
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    main(retrieveTestFilesDirectoryPath(), "1200", "abmzuyt")

    assertThat(outContent.toString().trim()).isEqualTo(CLOSED_ISSUE_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testClosedIssueCheck_todosCorrespondsToClosedIssue_checkShouldFail() {
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.bazel")
    val testContent1 =
      """
      // TODO(#169877): test description 1
      
      // TODO(#1021211): test description 2
      """.trimIndent()
    val testContent2 =
      """
      # test comment 3
      # test todo which is not a todo
      # TODO(#169877): test description 3
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    val exception = assertThrows(Exception::class) {
      main(retrieveTestFilesDirectoryPath(), "169877", "abmzuyt")
    }

    assertThat(exception).hasMessageThat().contains(CLOSED_ISSUE_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      The following TODOs are unresolved for the closed issue:
      - ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:1
      - ${retrieveTestFilesDirectoryPath()}/TempFile2.bazel:3
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testClosedIssueCheck_todosCorrespondsToClosedIssue_logsShouldBeLexicographicallySorted() {
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.xml")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.bazel")
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val testContent1 =
      """
      // TODO(#169877): test description 1
      
      // TODO(#1021211): test description 2
      """.trimIndent()
    val testContent2 =
      """
      # test comment 3
      # test todo which is not a todo
      # TODO(#169877): test description 3
      """.trimIndent()
    val testContent3 =
      """
      <!-- TODO(#169877): test description 4 -->
      
      <!-- TODO(#174144): test description 5 -->
      <!-- TODO(#169877): test description 6 -->
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val exception = assertThrows(Exception::class) {
      main(retrieveTestFilesDirectoryPath(), "169877", "abmzuyt")
    }

    assertThat(exception).hasMessageThat().contains(CLOSED_ISSUE_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      The following TODOs are unresolved for the closed issue:
      - ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:1
      - ${retrieveTestFilesDirectoryPath()}/TempFile2.bazel:3
      - ${retrieveTestFilesDirectoryPath()}/TempFile3.xml:1
      - ${retrieveTestFilesDirectoryPath()}/TempFile3.xml:4
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testClosedIssueCheck_triggerFailure_generatedFileContentShouldContainFailureTodoList() {
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.xml")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.bazel")
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val testContent1 =
      """
      // TODO(#169877): test description 1
      
      // TODO(#1021211): test description 2
      """.trimIndent()
    val testContent2 =
      """
      # test comment 3
      # test todo which is not a todo
      # TODO(#169877): test description 3
      """.trimIndent()
    val testContent3 =
      """
      <!-- TODO(#169877): test description 4 -->
      
      <!-- TODO(#174144): test description 5 -->
      <!-- TODO(#169877): test description 6 -->
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    assertThrows(Exception::class) {
      main(retrieveTestFilesDirectoryPath(), "169877", "abmzuyt")
    }
    val fileContentList =
      File("${retrieveTestFilesDirectoryPath()}/script_failures.txt").readLines()
    assertThat(fileContentList).containsExactly(
      "The issue is reopened because of the following unresolved TODOs:",
      "https://github.com/oppia/oppia-android/blob/abmzuyt/TempFile1.kt#L1",
      "https://github.com/oppia/oppia-android/blob/abmzuyt/TempFile2.bazel#L3",
      "https://github.com/oppia/oppia-android/blob/abmzuyt/TempFile3.xml#L1",
      "https://github.com/oppia/oppia-android/blob/abmzuyt/TempFile3.xml#L4"
    ).inOrder()
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }
}
