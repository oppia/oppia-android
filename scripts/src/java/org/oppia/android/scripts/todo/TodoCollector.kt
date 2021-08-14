package org.oppia.android.scripts.todo

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.todo.model.Todo

/** Collects code lines containing the 'todo' keyword (case-insensitive). */
class TodoCollector {
  companion object {
    private val todoDetectorRegex = Regex(pattern = "\\bTODO\\b", option = RegexOption.IGNORE_CASE)
    private val todoRegex = Regex(pattern = "\\bTODO\\b\\s*\\(", option = RegexOption.IGNORE_CASE)
    private val todoStartingRegex = Regex(
      pattern = "(//|<!--|#|\\*)\\s\\bTODO\\b",
      option = RegexOption.IGNORE_CASE
    )
    private val correctTodoFormatRegex = "\\bTODO\\b\\(#(\\d+)\\): .+".toRegex()

    /**
     * Collects all the TODOs in the repository.
     *
     * @param repoPath the path to the repository
     * @return a list of all the Todos
     */
    fun collectTodos(repoPath: String): List<Todo> {
      val searchFiles = RepositoryFile.collectSearchFiles(repoPath)
      return searchFiles.flatMap { file ->
        file.bufferedReader()
          .lineSequence()
          .mapIndexedNotNull { lineIndex, lineContent ->
            checkIfContainsTodo(
              filePath = file.toString(),
              lineContent = lineContent,
              lineIndex = lineIndex
            )
          }
      }
    }

    /**
     * Collects all the poorly formatted TODOs in the repository.
     *
     * @param todoList a list of TODOs of the repository
     * @return a list of all poorly formatted Todos
     */
    fun collectPoorlyFormattedTodos(todoList: List<Todo>): List<Todo> {
      return todoList.filter { todo ->
        checkIfTodoIsPoorlyFormatted(todo.lineContent)
      }
    }

    /**
     * Collects all the correctly formatted TODOs in the repository.
     *
     * @param todoList a list of TODOs of the repository
     * @return a list of all correctly formatted Todos
     */
    fun collectCorrectlyFormattedTodos(todoList: List<Todo>): List<Todo> {
      return todoList.filter { todo ->
        correctTodoFormatRegex.containsMatchIn(todo.lineContent)
      }
    }

    /**
     * Parses the issue number from a TODO.
     *
     * @param codeLine the line of code to be checked
     * @return the parsed issue number
     */
    fun parseIssueNumberFromTodo(codeLine: String): String? {
      return correctTodoFormatRegex.find(codeLine)?.groupValues?.get(1)
    }

    /**
     * Computes whether a line of code contains the 'todo' keyword.
     *
     * @param filePath the path of the file
     * @param lineContent the line string
     * @param lineIndex the index of the line sequence which is to be searched for a TODO
     * @return a Todo instance if the todo detector regex matches, else returns null
     */
    private fun checkIfContainsTodo(filePath: String, lineContent: String, lineIndex: Int): Todo? {
      if (todoDetectorRegex.containsMatchIn(lineContent)) {
        return Todo(filePath = filePath, lineNumber = lineIndex + 1, lineContent = lineContent)
      }
      return null
    }

    /**
     * Checks whether a line of code contains a poorly formatted TODO.
     *
     * @param codeLine the line of code to be checked
     * @return whether the line contains a poorly formatted TODO
     */
    private fun checkIfTodoIsPoorlyFormatted(codeLine: String): Boolean {
      if (todoStartingRegex.containsMatchIn(codeLine)) {
        if (!correctTodoFormatRegex.containsMatchIn(codeLine)) {
          return true
        }
        return false
      }
      if (todoRegex.containsMatchIn(codeLine)) {
        return true
      }
      return false
    }
  }
}
