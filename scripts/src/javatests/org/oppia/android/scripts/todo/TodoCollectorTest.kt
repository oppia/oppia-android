package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.todo.data.Todo

/** Tests for [TodoCollector]. */
class TodoCollectorTest {
  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    tempFolder.newFolder("testfiles")
  }

  @Test
  fun testTodoCollector_EmptyDirectory_noTodoShouldBeCollected() {
    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).isEmpty()
  }

  @Test
  fun testTodoCollector_multipleTodos_allShouldBeCollected() {
    val testContent =
      """
      test line 1
      test line 2
      test line 3
      test line 4
      test line 5
      todo
      test line 7
      test line 8
      TODO(#ISSUE_NUMBER): Revert ownership to @USERNAME after YYYY-MM-DD.
      test line 10
      """.trimIndent()
    tempFolder.newFolder("testfiles", ".github")
    val tempFile = tempFolder.newFile("testfiles/.github/CODEOWNERS")
    tempFile.writeText(testContent)
    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).hasSize(2)
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 6,
        lineContent = "todo"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 9,
        lineContent = "TODO(#ISSUE_NUMBER): Revert ownership to @USERNAME after YYYY-MM-DD."
      )
    )
  }

  @Test
  fun testTodoCollector_poorlyFormattedTodosAcrossMultipleFiles_allShouldBeCollected() {
    val testContent1 =
      """
      // TODO (#121): test todo.
      # TODO(    110)
      <!--TODO(# 101)-->
      """.trimIndent()
    val testContent2 =
      """
      <!--
      TODO(# 105)
      -->
      """.trimIndent()
    val testContent3 =
      """
      // TODO (#178): test todo.
      
      
      # TODO(    210)
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.sh")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).hasSize(6)
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 1,
        lineContent = "// TODO (#121): test todo."
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 2,
        lineContent = "# TODO(    110)"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 3,
        lineContent = "<!--TODO(# 101)-->"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile2.toString(),
        lineNumber = 2,
        lineContent = "TODO(# 105)"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 1,
        lineContent = "// TODO (#178): test todo."
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 4,
        lineContent = "# TODO(    210)"
      )
    )
  }

  @Test
  fun testTodoCollector_multipleTodosAcrossFiles_allShouldBeCollected() {
    val testContent1 =
      """
      # TODO (#121): test todo.
      <!--TODO(#101)-->
      """.trimIndent()
    val testContent2 =
      """
      # TODO(#10500): Test description
      """.trimIndent()
    val testContent3 =
      """
      // TODO(#17800): test todo.
      // TODO(    210)
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.bazel")
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.kt")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).hasSize(5)
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 1,
        lineContent = "# TODO (#121): test todo."
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 2,
        lineContent = "<!--TODO(#101)-->"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile2.toString(),
        lineNumber = 1,
        lineContent = "# TODO(#10500): Test description"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 1,
        lineContent = "// TODO(#17800): test todo."
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 2,
        lineContent = "// TODO(    210)"
      )
    )
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles/"
  }
}
