package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.scripts.todo.model.Todo

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
  fun testTodoCollector_emptyDirectory_invokeTodoCollector_noTodoShouldBeCollected() {
    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).isEmpty()
  }

  @Test
  fun testTodoCollector_addTodosAcrossSingleFile_invokeTodoCollector() {
    val testContent =
      """
      test line 1
      test line 2
      # TODO(#457741): test description 1
      # TODO (#457742): test description 2
      # TODO(#457743) : test description 3
      # TODO(457744): test description 4
      // TODO(#457741)
      // TODO(#457745):
      // TODO(#457747) test description 5
      // some comment which has a TODO(#12182992): some description
      // TODO(test description 7)
      // Todo(#4577413): test description 8
      // Todo (#4577423): test description 9
      // Todo(#4577433) : test description 10
      // Todo(4577443): test description 11
      // Todo(#4577413)
      // Todo(#4577453):
      // Todo(#4577473) test description 12
      // some comment which has a Todo(#12182999): some description
      // todo(#4577413): test description 14
      // todo (#4577423): test description 15
      // todo(#4577433) : test description 16
      // todo(4577443): test description 17
      // todo(#4577413)
      // todo(#4577453):
      // todo(#4577473) test description 18
      // some comment which has a todo(#12182999): some description
      test line 4
      test line 5
      todo
      test line 7
      test line 8
      TODO(#ISSUE_NUMBER): Revert ownership to @USERNAME after YYYY-MM-DD.
      //TODO(#161614): some another test description
      test line 10
      """.trimIndent()
    tempFolder.newFolder("testfiles", ".github")
    val tempFile = tempFolder.newFile("testfiles/.github/CODEOWNERS")
    tempFile.writeText(testContent)
    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).hasSize(28)
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 3,
        lineContent = "# TODO(#457741): test description 1"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 4,
        lineContent = "# TODO (#457742): test description 2"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 5,
        lineContent = "# TODO(#457743) : test description 3"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 6,
        lineContent = "# TODO(457744): test description 4"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 7,
        lineContent = "// TODO(#457741)"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 8,
        lineContent = "// TODO(#457745):"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 9,
        lineContent = "// TODO(#457747) test description 5"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 10,
        lineContent = "// some comment which has a TODO(#12182992): some description"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 11,
        lineContent = "// TODO(test description 7)"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 12,
        lineContent = "// Todo(#4577413): test description 8"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 13,
        lineContent = "// Todo (#4577423): test description 9"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 14,
        lineContent = "// Todo(#4577433) : test description 10"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 15,
        lineContent = "// Todo(4577443): test description 11"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 16,
        lineContent = "// Todo(#4577413)"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 17,
        lineContent = "// Todo(#4577453):"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 18,
        lineContent = "// Todo(#4577473) test description 12"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 19,
        lineContent = "// some comment which has a Todo(#12182999): some description"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 20,
        lineContent = "// todo(#4577413): test description 14"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 21,
        lineContent = "// todo (#4577423): test description 15"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 22,
        lineContent = "// todo(#4577433) : test description 16"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 23,
        lineContent = "// todo(4577443): test description 17"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 24,
        lineContent = "// todo(#4577413)"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 25,
        lineContent = "// todo(#4577453):"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 26,
        lineContent = "// todo(#4577473) test description 18"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 27,
        lineContent = "// some comment which has a todo(#12182999): some description"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 30,
        lineContent = "todo"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 33,
        lineContent = "TODO(#ISSUE_NUMBER): Revert ownership to @USERNAME after YYYY-MM-DD."
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile.toString(),
        lineNumber = 34,
        lineContent = "//TODO(#161614): some another test description"
      )
    )
  }

  @Test
  fun testTodoCollector_addTodosAcrossMultipleFiles_invokeTodoCollector() {
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

  @Test
  fun testTodoCollector_addTodos_invokePoorlyFormattedTodoCollector() {
    val testContent1 =
      """
      //TODO(#1215545): test todo.
      # TODO(    110)
      //todo(#15444)
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

    val allTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    val poorlyFormattedTodos = TodoCollector.collectPoorlyFormattedTodos(allTodos)
    assertThat(poorlyFormattedTodos).hasSize(7)
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 1,
        lineContent = "//TODO(#1215545): test todo."
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 2,
        lineContent = "# TODO(    110)"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 3,
        lineContent = "//todo(#15444)"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 4,
        lineContent = "<!--TODO(# 101)-->"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile2.toString(),
        lineNumber = 2,
        lineContent = "TODO(# 105)"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 1,
        lineContent = "// TODO (#178): test todo."
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 4,
        lineContent = "# TODO(    210)"
      )
    )
  }

  @Test
  fun testTodoCollector_addTodosWithIncorrectCase_invokePoorlyFormattedTodoCollector() {
    val testContent1 =
      """
      // Todo(#1215157): test content 1
      # todo(#110484844): test content 2
      // TODo(#15444): test content 3
      <!-- todo(#101484884): test content 4 -->
      """.trimIndent()
    val testContent2 =
      """
      <!-- tODo(#10554548): test content 5 -->
      """.trimIndent()
    val testContent3 =
      """
      // ToDo(#17878788): test content 6
      
      
      # some todo(#21084884): test content 7
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.sh")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val allTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    val poorlyFormattedTodos = TodoCollector.collectPoorlyFormattedTodos(allTodos)
    assertThat(poorlyFormattedTodos).hasSize(7)
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 1,
        lineContent = "// Todo(#1215157): test content 1"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 2,
        lineContent = "# todo(#110484844): test content 2"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 3,
        lineContent = "// TODo(#15444): test content 3"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 4,
        lineContent = "<!-- todo(#101484884): test content 4 -->"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile2.toString(),
        lineNumber = 1,
        lineContent = "<!-- tODo(#10554548): test content 5 -->"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 1,
        lineContent = "// ToDo(#17878788): test content 6"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 4,
        lineContent = "# some todo(#21084884): test content 7"
      )
    )
  }

  @Test
  fun testTodoCollector_addNotActualTodos_invokePoorlyFormattedTodoCollector() {
    val testContent1 =
      """
      // some comment involving todo.
      # a TODO comment.
      // Another Todo keyword containg comment.
      <!-- Yet another todo comment.  -->
      """.trimIndent()
    val testContent2 =
      """
      <!-- test todo comment -->
      """.trimIndent()
    val testContent3 =
      """
      // Another Todo comment
      
      
      # some test comment including todo
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.sh")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val allTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    val poorlyFormattedTodos = TodoCollector.collectPoorlyFormattedTodos(allTodos)
    assertThat(poorlyFormattedTodos).isEmpty()
  }

  @Test
  fun testTodoCollector_addTodos_invokeCorrectlyFormattedTodoCollector() {
    val testContent1 =
      """
      // TODO(#12111): some description 1.
      # TODO(#110000): some description 2.
      <!-- TODO(#1011010): some description 3. -->
      """.trimIndent()
    val testContent2 =
      """
      <!--
      TODO(# 105)
      -->
      """.trimIndent()
    val testContent3 =
      """
      // TODO(#1788888): some description 5.
      
      
      # TODO(#210000): some description 6.
      // TODO (#457742): test description 2
      // TODO(#457743) : test description 3
      // TODO(457744): test description 4
      // TODO(#457741)
      // TODO(#457745):
      // TODO(#457747) test description 5
      // TODO(test description 7)
      // Todo(#4577413): test description 8
      // Todo (#4577423): test description 9
      // Todo(#4577433) : test description 10
      // Todo(4577443): test description 11
      // Todo(#4577413)
      // Todo(#4577453):
      // Todo(#4577473) test description 12
      // todo(#4577413): test description 14
      // todo (#4577423): test description 15
      // todo(#4577433) : test description 16
      // todo(4577443): test description 17
      // todo(#4577413)
      // todo(#4577453):
      // todo(#4577473) test description 18
      //Todo(#6336363): test description 19
      test line 4
      test line 5
      todo
      test line 7
      test line 8
      TODO(#ISSUE_NUMBER): Revert ownership to @USERNAME after YYYY-MM-DD.
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    val tempFile2 = tempFolder.newFile("testfiles/TempFile2.kt")
    val tempFile3 = tempFolder.newFile("testfiles/TempFile3.sh")
    tempFile1.writeText(testContent1)
    tempFile2.writeText(testContent2)
    tempFile3.writeText(testContent3)

    val allTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    val correctlyFormattedTodos = TodoCollector.collectCorrectlyFormattedTodos(allTodos)
    assertThat(correctlyFormattedTodos).hasSize(5)
    assertThat(correctlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 1,
        lineContent = "// TODO(#12111): some description 1."
      )
    )
    assertThat(correctlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 2,
        lineContent = "# TODO(#110000): some description 2."
      )
    )
    assertThat(correctlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 3,
        lineContent = "<!-- TODO(#1011010): some description 3. -->"
      )
    )
    assertThat(correctlyFormattedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 1,
        lineContent = "// TODO(#1788888): some description 5."
      )
    )
    assertThat(correctlyFormattedTodos).contains(
      Todo(
        filePath = tempFile3.toString(),
        lineNumber = 4,
        lineContent = "# TODO(#210000): some description 6."
      )
    )
  }

  @Test
  fun testTodoCollector_parseIssueNumber_correctIssueNumberShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO(#1548774): some test description."
    )

    assertThat(parsedIssueNumber).isEqualTo("1548774")
  }

  @Test
  fun testTodoCollector_malformedTodo1_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO (#1548774): some test description."
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo2_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO(#1548775) : some test description."
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo3_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO(#1548779):"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo4_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO(# 1548778): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo5_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO(1548772): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo6_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO(some description)"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo7_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// Todo (#1548774): some test description."
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo8_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// Todo(#1548775) : some test description."
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo9_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// Todo(#1548779):"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo10_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// Todo(# 1548778): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo11_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// Todo(1548772): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo12_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "//Todo(1548772): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo13_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// todo (#1548774): some test description."
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo14_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// todo(#1548775) : some test description."
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo15_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// todo(#1548779):"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo16_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// todo(# 1548778): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo17_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// todo(1548772): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo18_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "//todo(1548772): some description"
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo19_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// TODO(#1234478 "
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo20_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// Todo(#1234478 "
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_malformedTodo21_parseIssueNumber_nullShouldBeObtained() {
    val parsedIssueNumber = TodoCollector.parseIssueNumberFromTodo(
      "// todo(#1234478 "
    )

    assertThat(parsedIssueNumber).isEqualTo(null)
  }

  @Test
  fun testTodoCollector_incompleteTodos_invokeTodoCollector() {
    val testContent1 =
      """
      // TODO(#1234478
      // Todo(#1234478
      // todo(#1234478
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
     tempFile1.writeText(testContent1)

    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    assertThat(collectedTodos).hasSize(3)
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 1,
        lineContent = "// TODO(#1234478"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 2,
        lineContent = "// Todo(#1234478"
      )
    )
    assertThat(collectedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 3,
        lineContent = "// todo(#1234478"
      )
    )
  }

  @Test
  fun testTodoCollector_incompleteTodos_invokePoorlyFormattedTodoCollector() {
    val testContent1 =
      """
      // TODO(#1234478
      // Todo(#1234478
      // todo(#1234478
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    tempFile1.writeText(testContent1)

    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    val poorlyFormattedTodos = TodoCollector.collectPoorlyFormattedTodos(collectedTodos)
    assertThat(poorlyFormattedTodos).hasSize(3)
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 1,
        lineContent = "// TODO(#1234478"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 2,
        lineContent = "// Todo(#1234478"
      )
    )
    assertThat(poorlyFormattedTodos).contains(
      Todo(
        filePath = tempFile1.toString(),
        lineNumber = 3,
        lineContent = "// todo(#1234478"
      )
    )
  }

  @Test
  fun testTodoCollector_incompleteTodos_invokeCorrectlyFormattedTodoCollector() {
    val testContent1 =
      """
      // TODO(#1234478
      // Todo(#1234478
      // todo(#1234478
      """.trimIndent()
    val tempFile1 = tempFolder.newFile("testfiles/TempFile1.txt")
    tempFile1.writeText(testContent1)

    val collectedTodos = TodoCollector.collectTodos(retrieveTestFilesDirectoryPath())
    val correctlyFormattedTodos = TodoCollector.collectCorrectlyFormattedTodos(collectedTodos)
    assertThat(correctlyFormattedTodos).isEmpty()
  }

  /** Retrieves the absolute path of testfiles directory. */
  private fun retrieveTestFilesDirectoryPath(): String {
    return "${tempFolder.root}/testfiles/"
  }
}
