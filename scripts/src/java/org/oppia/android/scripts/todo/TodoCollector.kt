package org.oppia.android.scripts.todo

import org.oppia.android.scripts.common.RepositoryFile
import java.io.File

/** Helper class to collect all the TODOs in the repository. */
class TodoCollector {
  companion object {
    private val todoDetectorRegex = "(?!\".*)\\bTODO\\b(?!.*\")".toRegex()

    /**
     * Collects all the TODOs in the repository.
     *
     * @param repoPath the path to the repository
     * @return a list of all the TODOs
     */
    fun collectTodos(repoPath: String): List<Pair<File, Int>> {
      val searchFiles = RepositoryFile.collectSearchFiles(repoPath)
      val allTodos = searchFiles.flatMap { file ->
        file.bufferedReader()
          .lineSequence()
          .mapIndexedNotNull { lineIndex, lineContent ->
            findTodoInFile(file, lineContent, lineIndex)
          }
      }
      return allTodos
    }

    /**
     * Searches for a "TODO" at a specific line of the file.
     *
     * @param file the file containing the TODOs
     * @param lineContent the line string
     * @param lineIndex the index of the line sequence which is to be searched for a "TODO"
     * @return a pair of file and line index for the matched "TODO". If not matched, it returns null
     */
    private fun findTodoInFile(file: File, lineContent: String, lineIndex: Int): Pair<File, Int>? {
      if (todoDetectorRegex.containsMatchIn(lineContent)) {
        return Pair(file, lineIndex)
      } else {
        return null
      }
    }
  }
}
