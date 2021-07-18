package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [TodoCheck]. */
class TodoCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val TODO_CHECK_PASSED_OUTPUT_INDICATOR: String = "TODO CHECK PASSED"
  private val TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR: String = "TODO CHECK FAILED"

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
  fun testTodoCheck_noJsonFilePresent_checkShouldFail() {
    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(
      "${retrieveTestFilesDirectoryPath()}/open_issues.json (No such file or directory)"
    )
  }

  @Test
  fun testTodoCheck_multipleTodosPresent_allAreValid_checkShouldPass() {
    val testJSONContent =
      """
      [{"number":100},{"number":99},{"number":4},{"number":1}]  
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO(#100): test summary 1.
      #TODO(#4): test summary 2.
      test content 1
      test content 2
      """.trimIndent()
    val testContent2 =
      """
      // TODO(#99): test summary 1.
      "TODO"
      test content 3
      <!-- TODO(#1): test summary 3-->
      
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(TODO_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTodoCheck_onlyPoorlyFormattedTodosPresent_checkShouldFail() {
    val testJSONContent =
      """
      []  
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile = tempFolder.newFile("testfiles/TempFile.txt")
    val testContent =
      """
      // TODO (#1044): test
      # TODO(102)
      <!-- TODO(#   101)-->
      """.trimIndent()
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.txt:1: TODO is poorly formatted
      ${retrieveTestFilesDirectoryPath()}/TempFile.txt:2: TODO is poorly formatted
      ${retrieveTestFilesDirectoryPath()}/TempFile.txt:3: TODO is poorly formatted
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_onlyOpenIssueFailureTodosPresent_checkShouldFail() {
    val testJSONContent =
      """
      [{"number":3498},{"number":3497}]  
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile = tempFolder.newFile("testfiles/TempFile.txt")
    val testContent =
      """
      // TODO(#1044): test summary 1.
      #TODO(#102): test summary 2.
      <!-- TODO(#101): test summary 3-->
      """.trimIndent()
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile.txt:1: TODO does not corresponds to an open issue
      ${retrieveTestFilesDirectoryPath()}/TempFile.txt:2: TODO does not corresponds to an open issue
      ${retrieveTestFilesDirectoryPath()}/TempFile.txt:3: TODO does not corresponds to an open issue
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_multipleFailuresPresent_allFailuresShouldBeReported() {
    val testJSONContent =
      """
      [{"number":3498},{"number":3497}]  
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO(#1044): test summary 1.
      #TODO (#102): test summary 2.
      <!-- TODO(#3498): test summary 3-->
      
      """.trimIndent()
    val testContent2 =
      """
      // TODO (#3497): test summary 1.
      "TODO"
      <!-- TODO(#1): test summary 3-->
      
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:2: TODO is poorly formatted
      ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:1: TODO is poorly formatted
     
      ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:1: TODO does not corresponds to an open issue
      ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:3: TODO does not corresponds to an open issue
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_multipleFailuresPresent_loggingShouldBeAsPerLexicographicalOrder() {
    val testJSONContent =
      """
      [{"number":3498},{"number":3497}]  
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/Presenter.kt")
    val tempFile2 = tempFolder.newFile("testfiles/Fragment.kt")
    val tempFile3 = tempFolder.newFile("testfiles/Activity.kt")
    val testContent1 =
      """
      // TODO(#1044): test summary 1.
      #TODO (#102): test summary 2.
      <!-- TODO(#3498): test summary 3-->
      
      """.trimIndent()
    val testContent2 =
      """
      // TODO (#3497): test summary 1.
      "TODO"
      <!-- TODO(#1): test summary 3-->
      
      """.trimIndent()
    val testContent3 =
      """
      test content 
      // TODO (#3497): test summary 1.  
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      ${retrieveTestFilesDirectoryPath()}/Activity.kt:2: TODO is poorly formatted
      ${retrieveTestFilesDirectoryPath()}/Fragment.kt:1: TODO is poorly formatted
      ${retrieveTestFilesDirectoryPath()}/Presenter.kt:2: TODO is poorly formatted
     
      ${retrieveTestFilesDirectoryPath()}/Fragment.kt:3: TODO does not corresponds to an open issue
      ${retrieveTestFilesDirectoryPath()}/Presenter.kt:1: TODO does not corresponds to an open issue
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /** Runs the xml_syntax_check. */
  private fun runScript() {
    main(retrieveTestFilesDirectoryPath())
  }
}
