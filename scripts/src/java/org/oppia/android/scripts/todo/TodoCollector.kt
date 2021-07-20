package org.oppia.android.scripts.todo

import org.oppia.android.scripts.common.RepositoryFile
import org.oppia.android.scripts.proto.TodoExemptions
import java.io.File
import java.io.FileInputStream

/** Helper class to collect all the TODOs in the repository. */
class TodoCollector {
  companion object {
    private val todoDetectorRegex = Regex(
      pattern = "(?!\".*)\\bTODO\\b(?!.*\")",
      option = RegexOption.IGNORE_CASE
    )

    private val todoExemptionTextProto = "scripts/assets/todo_exemptions"

    // List of all the false todos.
    private val todoExemptionList = loadTodoExemptionsProto(todoExemptionTextProto)
      .getTodoExemptionList()

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
            findTodoInFile(file, lineContent, lineIndex, repoPath)
          }
      }
      return allTodos
    }

    /**
     * Searches for a TODO at a specific line of the file.
     *
     * @param file the file containing the TODOs
     * @param lineContent the line string
     * @param lineIndex the index of the line sequence which is to be searched for a TODO
     * @param repoPath the path to the repository
     * @return a pair of file and line index for the matched TODO. If not matched, it returns null
     */
    private fun findTodoInFile(
      file: File,
      lineContent: String,
      lineIndex: Int,
      repoPath: String
    ): Pair<File, Int>? {
      val relativeFilePath = RepositoryFile.retrieveRelativeFilePath(file, repoPath)
      if (isTodoNonReal(relativeFilePath, lineIndex + 1)) {
        return null
      }
      return if (todoDetectorRegex.containsMatchIn(lineContent)) Pair(file, lineIndex) else null
    }

    /**
     * Checks whether a detected TODO is non-real.
     *
     * @param file the file containing the TODOs
     * @param lineNumber the line number of the line sequence which is to be searched for a TODO
     * @return whether the TODO is unreal
     */
    private fun isTodoNonReal(file: String, lineNumber: Int): Boolean {
      return todoExemptionList.any { it ->
        it.exemptedFilePath == file && lineNumber in it.getLineNumberList()
      }
    }

    /**
     * Loads the todo exemptions list to proto.
     *
     * @param todoExemptionTextProto the location of the todo exemption textproto file
     * @return proto class from the parsed textproto file
     */
    private fun loadTodoExemptionsProto(todoExemptionTextProto: String): TodoExemptions {
      val protoBinaryFile = File("$todoExemptionTextProto.pb")
      val builder = TodoExemptions.getDefaultInstance().newBuilderForType()

      // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
      // and this method is bounded by the generic type T.
      @Suppress("UNCHECKED_CAST")
      val protoObj: TodoExemptions =
        FileInputStream(protoBinaryFile).use {
          builder.mergeFrom(it)
        }.build() as TodoExemptions
      return protoObj
    }
  }
}
