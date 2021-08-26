package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.proto.TodoOpenExemption
import org.oppia.android.scripts.proto.TodoOpenExemptions
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for [TodoOpenCheck]. */
class TodoOpenCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val TODO_CHECK_PASSED_OUTPUT_INDICATOR: String = "TODO CHECK PASSED"
  private val TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR: String = "TODO CHECK FAILED"
  private val pathToProtoBinary = "scripts/assets/todo_exemptions.pb"
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#todo-open-checks for more details on how to fix this."

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    tempFolder.newFolder("scripts", "assets")
    tempFolder.newFile(pathToProtoBinary)
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
      "${retrieveTestFilesDirectoryPath()}/open_issues.json: No such file exists"
    )
  }

  @Test
  fun testTodoCheck_multipleTodosPresent_allAreValid_checkShouldPass() {
    val testJSONContent =
      """
      [{"number":11004},{"number":11003},{"number":11002},{"number":11001}]
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO(#11002): test summary 1.
      # TODO(#11004): test summary 2.
      test Todo
      test TODO
      """.trimIndent()
    val testContent2 =
      """
      // TODO(#11001): test summary 3.
      todo
      <!-- TODO(#11003): test summary 4-->

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
      // some test conent TODO(#1020000): test description.
      some test content TODO(#100002): some description.
      """.trimIndent()
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not in correct format:
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:1
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:2
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:3
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:4
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:5
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_onlyOpenIssueFailureTodosPresent_checkShouldFail() {
    val testJSONContent =
      """
      [{"number":10000000},{"number":100000004}]
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile = tempFolder.newFile("testfiles/TempFile.txt")
    val testContent =
      """
      // TODO(#104444444): test summary 1.
      # TODO(#10210110): test summary 2.
      test todo
      some test content Todo
      <!-- TODO(#101000000): test summary 3-->
      """.trimIndent()
    tempFile.writeText(testContent)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not corresponding to open issues on GitHub:
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:1
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:2
      - ${retrieveTestFilesDirectoryPath()}/TempFile.txt:5
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_multipleFailuresPresent_allFailuresShouldBeReported() {
    val testJSONContent =
      """
      [{"number":349888},{"number":349777}]
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO(#10444444): test summary 1.
      #Todo(#102): test summary 2.
      <!-- TODO(#349888): test summary 3-->

      """.trimIndent()
    val testContent2 =
      """
      // TODO (#349777): test summary 1.
      todo
      <!-- TODO(#10000000): test summary 3-->

      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not in correct format:
      - ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:2
      - ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:1
      
      TODOs not corresponding to open issues on GitHub:
      - ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:1
      - ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:3
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_multipleFailuresPresent_loggingShouldBeAsPerLexicographicalOrder() {
    val testJSONContent =
      """
      [{"number":349888},{"number":349777}]
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/Presenter.kt")
    val tempFile2 = tempFolder.newFile("testfiles/Fragment.kt")
    val tempFile3 = tempFolder.newFile("testfiles/Activity.kt")
    val testContent1 =
      """
      // TODO(#104444444444): test summary 1.
      #TODO (#102): test summary 2.
      <!-- TODO(#349888): test summary 3-->

      """.trimIndent()
    val testContent2 =
      """
      // TODO (#349777): test summary 1.
      some line todo test content
      <!-- TODO(#100000000): test summary 3-->

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
      TODOs not in correct format:
      - ${retrieveTestFilesDirectoryPath()}/Activity.kt:2
      - ${retrieveTestFilesDirectoryPath()}/Fragment.kt:1
      - ${retrieveTestFilesDirectoryPath()}/Presenter.kt:2
      
      TODOs not corresponding to open issues on GitHub:
      - ${retrieveTestFilesDirectoryPath()}/Fragment.kt:3
      - ${retrieveTestFilesDirectoryPath()}/Presenter.kt:1
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_addExemptions_exemptedTodosAreInvalid_checkShouldPass() {
    val testJSONContent =
      """
      [{"number":11004},{"number":11003},{"number":11002},{"number":11001}]
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO (152440222): test description 1
      """.trimIndent()
    val testContent2 =
      """
      # TODO(#1000000): test description 2
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")
    val exemptions = TodoOpenExemptions.newBuilder().apply {
      this.addAllTodoOpenExemption(
        listOf(
          TodoOpenExemption.newBuilder().apply {
            this.exemptedFilePath = "TempFile1.kt"
            this.addAllLineNumber(listOf(1)).build()
          }.build(),
          TodoOpenExemption.newBuilder().apply {
            this.exemptedFilePath = "TempFile2.kt"
            this.addAllLineNumber(listOf(1)).build()
          }.build()
        )
      )
    }.build()
    exemptions.writeTo(exemptionFile.outputStream())

    runScript()

    assertThat(outContent.toString().trim()).isEqualTo(TODO_CHECK_PASSED_OUTPUT_INDICATOR)
  }

  @Test
  fun testTodoCheck_allTodosAreValid_redundantExemption_checkShouldFail() {
    val testJSONContent =
      """
      [{"number":1000000},{"number":152440222},{"number":152440223},{"number":11001}]
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO(#152440222): test description 1
      // TODO(#152440223): test description 1
      """.trimIndent()
    val testContent2 =
      """
      # TODO(#1000000): test description 2
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")
    val exemptions = TodoOpenExemptions.newBuilder().apply {
      this.addAllTodoOpenExemption(
        listOf(
          TodoOpenExemption.newBuilder().apply {
            this.exemptedFilePath = "TempFile1.kt"
            this.addAllLineNumber(listOf(1, 2)).build()
          }.build(),
          TodoOpenExemption.newBuilder().apply {
            this.exemptedFilePath = "TempFile2.kt"
            this.addAllLineNumber(listOf(1)).build()
          }.build()
        )
      )
    }.build()
    exemptions.writeTo(exemptionFile.outputStream())

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      Redundant exemptions (there are no TODOs corresponding to these lines):
      - TempFile1.kt:1
      - TempFile1.kt:2
      - TempFile2.kt:1
      Please remove them from scripts/assets/todo_exemptions.textproto
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_combineMultipleFailures_checkShouldFailWithAllErrorsLogged() {
    val testJSONContent =
      """
      [{"number":1000000},{"number":152440222},{"number":152440223},{"number":11001}]
      """.trimIndent()
    val testJSONFile = tempFolder.newFile("testfiles/open_issues.json")
    testJSONFile.writeText(testJSONContent)
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO(#15244): test content 1
      // TODO(#152440223): test description 1
      // TODO(#10000000000000): test description 2
      """.trimIndent()
    val testContent2 =
      """
      # test content TODO(#11001): test description 2
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    val exemptionFile = File("${tempFolder.root}/$pathToProtoBinary")
    val exemptions = TodoOpenExemptions.newBuilder().apply {
      this.addAllTodoOpenExemption(
        listOf(
          TodoOpenExemption.newBuilder().apply {
            this.exemptedFilePath = "TempFile1.kt"
            this.addAllLineNumber(listOf(1, 2)).build()
          }.build()
        )
      )
    }.build()
    exemptions.writeTo(exemptionFile.outputStream())

    val exception = assertThrows(Exception::class) {
      runScript()
    }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      Redundant exemptions (there are no TODOs corresponding to these lines):
      - TempFile1.kt:2
      Please remove them from scripts/assets/todo_exemptions.textproto
      
      TODOs not in correct format:
      - ${retrieveTestFilesDirectoryPath()}/TempFile2.kt:1
      
      TODOs not corresponding to open issues on GitHub:
      - ${retrieveTestFilesDirectoryPath()}/TempFile1.kt:3
      
      $wikiReferenceNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }

  /** Runs the todo_open_check. */
  private fun runScript() {
    main(
      retrieveTestFilesDirectoryPath(),
      "${tempFolder.root}/$pathToProtoBinary",
      "open_issues.json"
    )
  }
}
