package org.oppia.android.scripts.todo

import org.oppia.android.scripts.common.RepositoryFile

/** Collects code lines containing the 'todo' keyword (case-insensitive). */
class TodoCollector {
  companion object {
    private val todoDetectorRegex = Regex(pattern = "\\bTODO\\b", option = RegexOption.IGNORE_CASE)

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

    data class Todo(val filePath: String, val lineNumber: Int, val lineContent: String)
  }
}
