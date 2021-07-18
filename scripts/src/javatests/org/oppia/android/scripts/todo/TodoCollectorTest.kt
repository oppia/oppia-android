package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

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
  fun testTodoCollector_todosInsideQuotes_noTodoShouldBeCollected() {
    val testContent =
      """
      val testTodoRegexString = "TODO"
      "This is a test TODO which is inside quotes"
      """.trimIndent()
    val tempFile = tempFolder.newFile("testfiles/TempFile.txt")
    tempFile.writeText(testContent)
    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).isEmpty()
  }

  @Test
  fun testTodoCollector_poorlyFormattedTodosAcrossMultipleFiles_allShouldBeCollected() {
    val testTodo1 = "// TODO (#121): test todo."
    val testTodo2 = "# TODO(    110)"
    val testTodo3 = "<!--TODO(# 101)-->"
    val testTodo4 = "// TODO (#178): test todo."
    val testTodo5 = "# TODO(    210)"
    val testTodo6 = "<!--TODO(# 105)-->"
    val testContent1 =
      """
      $testTodo1
      $testTodo2
      $testTodo3
      """.trimIndent()
    val testContent2 =
      """
      $testTodo6
      """.trimIndent()
    val testContent3 =
      """
      $testTodo4
      $testTodo5
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.sh")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).hasSize(6)
    assertThat(collectedTodos).contains(Pair(tempFile1, 0))
    assertThat(collectedTodos).contains(Pair(tempFile1, 1))
    assertThat(collectedTodos).contains(Pair(tempFile1, 2))
    assertThat(collectedTodos).contains(Pair(tempFile2, 0))
    assertThat(collectedTodos).contains(Pair(tempFile3, 0))
    assertThat(collectedTodos).contains(Pair(tempFile3, 1))
  }

  @Test
  fun testTodoCollector_multipleTodosAcrossFiles_allShouldBeCollectedExceptTodosInsideQuotes() {
    val testTodo1 = "// TODO (#121): test todo."
    val testTodo2 = "<!--TODO(#101)-->"
    val testTodo3 = "<!--TODO(#105)-->"
    val testTodo4 = "// TODO (#178): test todo."
    val testTodo5 = "# TODO(    210)"
    val testContent1 =
      """
      $testTodo1
      "This is a test TODO which is inside quotes"
      $testTodo2
      """.trimIndent()
    val testContent2 =
      """
      $testTodo3
      val testTodoRegexString = "TODO"
      """.trimIndent()
    val testContent3 =
      """
      $testTodo4
      $testTodo5
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.yaml")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.bazel")
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.kt")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).hasSize(5)
    assertThat(collectedTodos).contains(Pair(tempFile1, 0))
    assertThat(collectedTodos).contains(Pair(tempFile1, 2))
    assertThat(collectedTodos).contains(Pair(tempFile2, 0))
    assertThat(collectedTodos).contains(Pair(tempFile3, 0))
    assertThat(collectedTodos).contains(Pair(tempFile3, 1))
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles"
  }
}
