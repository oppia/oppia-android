package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.common.GitHubClient
import org.oppia.android.scripts.common.ScriptBackgroundCoroutineDispatcher
import org.oppia.android.scripts.common.testing.FakeCommandExecutor
import org.oppia.android.scripts.proto.TodoOpenExemption
import org.oppia.android.scripts.proto.TodoOpenExemptions
import org.oppia.android.testing.assertThrows
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream

/** Tests for [TodoOpenCheck]. */
class TodoOpenCheckTest {
  private companion object {
    private const val TEST_AUTH_TOKEN = "abcdef1234567890"
  }

  private val outContent = ByteArrayOutputStream()
  private val originalOut = System.out
  private val TODO_CHECK_PASSED_OUTPUT_INDICATOR = "TODO CHECK PASSED"
  private val TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR = "TODO CHECK FAILED"
  private val TODO_SYNTAX_CHECK_SKIPPED_OUTPUT_INDICATOR = "TODO CHECK SKIPPED"
  private val pathToProtoBinary = "scripts/assets/todo_exemptions.pb"
  private val wikiReferenceNote =
    "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
      "#todo-open-checks for more details on how to fix this."
  private val regenerateNote =
    "There were failures. Re-run //scripts:todo_open_check with \"regenerate\" at the end to " +
      "regenerate the exemption file with all failures as exempted."

  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private val scriptBgDispatcher by lazy { ScriptBackgroundCoroutineDispatcher() }
  private val fakeCommandExecutor by lazy { FakeCommandExecutor() }

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
    tempFolder.newFolder("scripts", "assets")
    tempFolder.newFile(pathToProtoBinary)
    setUpSupportForGhAuth(TEST_AUTH_TOKEN)
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
    scriptBgDispatcher.close()
  }

  @Test
  fun testTodoCheck_multipleTodosPresent_allAreValid_checkShouldPass() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
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
    setUpGitHubService(issueNumbers = emptyList())
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

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not in correct format:
      - TempFile.txt:1
      - TempFile.txt:2
      - TempFile.txt:3
      - TempFile.txt:4
      - TempFile.txt:5

      $wikiReferenceNote

      $regenerateNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_onlyOpenIssueFailureTodosPresent_checkShouldFail() {
    setUpGitHubService(issueNumbers = listOf(10000000, 100000004))
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

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not corresponding to open issues on GitHub:
      - TempFile.txt:1
      - TempFile.txt:2
      - TempFile.txt:5

      $wikiReferenceNote

      $regenerateNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_multipleFailuresPresent_allFailuresShouldBeReported() {
    setUpGitHubService(issueNumbers = listOf(349888, 349777))
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

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not in correct format:
      - TempFile1.kt:2
      - TempFile2.kt:1

      TODOs not corresponding to open issues on GitHub:
      - TempFile1.kt:1
      - TempFile2.kt:3

      $wikiReferenceNote

      $regenerateNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_multipleFailuresPresent_loggingShouldBeAsPerLexicographicalOrder() {
    setUpGitHubService(issueNumbers = listOf(349888, 349777))
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

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not in correct format:
      - Activity.kt:2
      - Fragment.kt:1
      - Presenter.kt:2

      TODOs not corresponding to open issues on GitHub:
      - Fragment.kt:3
      - Presenter.kt:1

      $wikiReferenceNote

      $regenerateNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_addExemptions_exemptedTodosAreInvalid_checkShouldPass() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
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
    setUpGitHubService(issueNumbers = listOf(1000000, 152440222, 152440223, 11001))
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

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      Redundant exemptions (there are no TODOs corresponding to these lines):
      - TempFile1.kt:1
      - TempFile1.kt:2
      - TempFile2.kt:1
      Please remove them from scripts/assets/todo_exemptions.textproto

      $regenerateNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_combineMultipleFailures_checkShouldFailWithAllErrorsLogged() {
    setUpGitHubService(issueNumbers = listOf(1000000, 152440222, 152440223, 11001))
    tempFolder.newFolder("testfiles/extra_dir")
    val tempFile1 = tempFolder.newFile("testfiles/extra_dir/TempFile1.kt")
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
            this.exemptedFilePath = "extra_dir/TempFile1.kt"
            this.addAllLineNumber(listOf(1, 2)).build()
          }.build()
        )
      )
    }.build()
    exemptions.writeTo(exemptionFile.outputStream())

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      Redundant exemptions (there are no TODOs corresponding to these lines):
      - extra_dir/TempFile1.kt:2
      Please remove them from scripts/assets/todo_exemptions.textproto

      TODOs not in correct format:
      - TempFile2.kt:1

      TODOs not corresponding to open issues on GitHub:
      - extra_dir/TempFile1.kt:3

      $wikiReferenceNote

      $regenerateNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testRegenerate_noTodos_checkShouldSkip() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      test Todo
      test TODO
      """.trimIndent()
    val testContent2 =
      """
      todo
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    val exception = assertThrows<Exception>() { runScript(regenerateFile = true) }

    // 'regenerate' always throws an exception since it's regenerating everything.
    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_SKIPPED_OUTPUT_INDICATOR)
  }

  @Test
  fun testRegenerate_onlyValidTodos_checkShouldSkip() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
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

    val exception = assertThrows<Exception>() { runScript(regenerateFile = true) }

    // 'regenerate' always throws an exception since it's regenerating everything.
    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_SKIPPED_OUTPUT_INDICATOR)
  }

  @Test
  fun testRegenerate_poorlyFormattedTodos_checkShouldSkip() {
    setUpGitHubService(issueNumbers = emptyList())
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

    val exception = assertThrows<Exception>() { runScript(regenerateFile = true) }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_SKIPPED_OUTPUT_INDICATOR)
  }

  @Test
  fun testRegenerate_openIssuesTodos_checkShouldSkip() {
    setUpGitHubService(issueNumbers = listOf(10000000, 100000004))
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

    val exception = assertThrows<Exception>() { runScript(regenerateFile = true) }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_SKIPPED_OUTPUT_INDICATOR)
  }

  @Test
  fun testRegenerate_mixedBadFormattingAndOpenAndValidTodos_checkShouldSkip() {
    setUpGitHubService(issueNumbers = listOf(10000000, 100000004, 11002, 11004))
    val tempFile = tempFolder.newFile("testfiles/TempFile.txt")
    val testContent =
      """
      // TODO (#1044): test
      # TODO(102)
      <!-- TODO(#   101)-->
      // some test conent TODO(#1020000): test description.
      some test content TODO(#100002): some description.
      // TODO(#104444444): test summary 1.
      # TODO(#10210110): test summary 2.
      test todo
      some test content Todo
      <!-- TODO(#101000000): test summary 3-->
      // TODO(#11002): test summary 1.
      # TODO(#11004): test summary 2.
      test Todo
      test TODO
      """.trimIndent()
    tempFile.writeText(testContent)

    val exception = assertThrows<Exception>() { runScript(regenerateFile = true) }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_SKIPPED_OUTPUT_INDICATOR)
  }

  @Test
  fun testRegenerate_todosWithExemptions_checkShouldSkip() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
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

    val exception = assertThrows<Exception>() { runScript(regenerateFile = true) }

    // 'regenerate' always throws an exception since it's regenerating everything.
    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_SKIPPED_OUTPUT_INDICATOR)
  }

  @Test
  fun testRegenerate_noTodos_shouldOutputEmptyTextProto() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      test Todo
      test TODO
      """.trimIndent()
    val testContent2 =
      """
      todo
      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    assertThrows<Exception>() { runScript(regenerateFile = true) }

    val failureMessage =
      """
      Regenerated exemptions:
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testRegenerate_onlyValidTodos_shouldOutputEmptyTextProto() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
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

    assertThrows<Exception>() { runScript(regenerateFile = true) }

    val failureMessage =
      """
      Regenerated exemptions:
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testRegenerate_poorlyFormattedTodos_shouldOutputNewTextProto() {
    setUpGitHubService(issueNumbers = emptyList())
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

    assertThrows<Exception>() { runScript(regenerateFile = true) }

    val failureMessage =
      """
      TODOs not in correct format:
      - TempFile.txt:1
      - TempFile.txt:2
      - TempFile.txt:3
      - TempFile.txt:4
      - TempFile.txt:5

      $wikiReferenceNote

      Regenerated exemptions:

      todo_open_exemption {
        exempted_file_path: "TempFile.txt"
        line_number: 1
        line_number: 2
        line_number: 3
        line_number: 4
        line_number: 5
      }
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testRegenerate_openIssuesTodos_shouldOutputNewTextProto() {
    setUpGitHubService(issueNumbers = listOf(10000000, 100000004))
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

    assertThrows<Exception>() { runScript(regenerateFile = true) }

    val failureMessage =
      """
      TODOs not corresponding to open issues on GitHub:
      - TempFile.txt:1
      - TempFile.txt:2
      - TempFile.txt:5

      $wikiReferenceNote

      Regenerated exemptions:

      todo_open_exemption {
        exempted_file_path: "TempFile.txt"
        line_number: 1
        line_number: 2
        line_number: 5
      }
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testRegenerate_mixedBadFormattingAndOpenAndValidTodos_shouldOutputNewTextProto() {
    setUpGitHubService(issueNumbers = listOf(10000000, 100000004, 11002, 11004))
    val tempFile = tempFolder.newFile("testfiles/TempFile.txt")
    val testContent =
      """
      // TODO (#1044): test
      # TODO(102)
      <!-- TODO(#   101)-->
      // some test conent TODO(#1020000): test description.
      some test content TODO(#100002): some description.
      // TODO(#104444444): test summary 1.
      # TODO(#10210110): test summary 2.
      test todo
      some test content Todo
      <!-- TODO(#101000000): test summary 3-->
      // TODO(#11002): test summary 1.
      # TODO(#11004): test summary 2.
      test Todo
      test TODO
      """.trimIndent()
    tempFile.writeText(testContent)

    assertThrows<Exception>() { runScript(regenerateFile = true) }

    val failureMessage =
      """
      TODOs not in correct format:
      - TempFile.txt:1
      - TempFile.txt:2
      - TempFile.txt:3
      - TempFile.txt:4
      - TempFile.txt:5

      TODOs not corresponding to open issues on GitHub:
      - TempFile.txt:6
      - TempFile.txt:7
      - TempFile.txt:10

      $wikiReferenceNote

      Regenerated exemptions:

      todo_open_exemption {
        exempted_file_path: "TempFile.txt"
        line_number: 1
        line_number: 2
        line_number: 3
        line_number: 4
        line_number: 5
        line_number: 6
        line_number: 7
        line_number: 10
      }
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testRegenerate_todosWithExemptions_shouldOutputNewTextProtoIncludingExemptions() {
    setUpGitHubService(issueNumbers = listOf(11004, 11003, 11002, 11001))
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

    assertThrows<Exception>() { runScript(regenerateFile = true) }

    val failureMessage =
      """
      Regenerated exemptions:

      todo_open_exemption {
        exempted_file_path: "TempFile1.kt"
        line_number: 1
      }
      todo_open_exemption {
        exempted_file_path: "TempFile2.kt"
        line_number: 1
      }
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  @Test
  fun testTodoCheck_PrPresent_checkShouldFail() {
    setUpGitHubService(
      issueNumbers = listOf(11004, 11003, 11002, 11001),
      pullRequestNumbers = listOf(11005)
    )
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.kt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val testContent1 =
      """
      // TODO(#11002): test summary 1.
      # TODO(#11004): test summary 2.
      # TODO(#11001): test summary 3.
      test Todo
      test TODO
      """.trimIndent()
    val testContent2 =
      """
      // TODO(#11005): test summary 3.
      todo
      <!-- TODO(#11003): test summary 4-->

      """.trimIndent()
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)

    val exception = assertThrows<Exception>() { runScript() }

    assertThat(exception).hasMessageThat().contains(TODO_SYNTAX_CHECK_FAILED_OUTPUT_INDICATOR)
    val failureMessage =
      """
      TODOs not corresponding to open issues on GitHub:
      - TempFile2.kt:1

      $wikiReferenceNote

      $regenerateNote
      """.trimIndent()
    assertThat(outContent.toString().trim()).isEqualTo(failureMessage)
  }

  private fun setUpGitHubService(
    issueNumbers: List<Int>,
    pullRequestNumbers: List<Int> = emptyList()
  ) {
    // Create JSON for issues with "pull_request" set to null
    val issueJsons = issueNumbers
      .joinToString(separator = ",") { "{\"number\":$it,\"pull_request\":null}" }

    // Create JSON for pull requests with "pull_request" as an empty object
    val pullRequestJsons = pullRequestNumbers
      .joinToString(separator = ",") { "{\"number\":$it,\"pull_request\":{}}" }

    // Combine issues and pull requests into one JSON array
    val combinedJsons =
      "[$issueJsons${if (pullRequestNumbers.isNotEmpty()) ", $pullRequestJsons" else ""}]"

    val mockWebServer = MockWebServer()
    mockWebServer.enqueue(MockResponse().setBody("[$issueJsons]"))
    mockWebServer.enqueue(MockResponse().setBody(combinedJsons))
    mockWebServer.enqueue(MockResponse().setBody("[]"))
    GitHubClient.remoteApiUrl = mockWebServer.url("/").toString()
  }

  private fun setUpSupportForGhAuth(authToken: String) {
    fakeCommandExecutor.registerHandler("gh") { _, args, outputStream, _ ->
      when (args) {
        listOf("help") -> 0
        listOf("auth", "token") -> 0.also { outputStream.print(authToken) }
        else -> 1
      }
    }
  }

  // TODO(#5314): Replace this (& other script tests) with using main() directly and swap out
  // dependencies using Dagger rather than needing to call into a separately created instance of an
  // internal helper class for the script.
  private fun runScript(regenerateFile: Boolean = false) {
    val repoRoot = File(tempFolder.root, "testfiles")
    TodoOpenCheck(repoRoot, scriptBgDispatcher, fakeCommandExecutor).runTodoOpenCheck(
      pathToProtoBinary = "${tempFolder.root}/$pathToProtoBinary",
      regenerateFile
    )
  }
}
