package org.oppia.android.scripts.todo

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.todo.data.Issue
import java.io.File
import org.oppia.android.scripts.proto.TodoExemptions
import org.oppia.android.scripts.proto.TodoExemption
import java.io.FileInputStream
import org.oppia.android.scripts.todo.data.Todo

/**
 * Script for ensuring that all TODOs present in the repository are correctly formatted and
 * corresponds to an open issue on Github.
 *
 * Usage:
 *   bazel run //scripts:todo_check -- <path_to_directory_root>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 *
 * Example:
 *   bazel run //scripts:todo_check -- $(pwd)
 *
 * NOTE TO DEVELOPERS: The script is meant only to be run during the CI, as this expects a JSON
 *     file at the root level of the repository which contains the list of all open issues. This
 *     file is generated during the CI. So, executing this anywhere except CI will lead to the
 *     failure of the script.
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  val pathToProtoBinary = args[1]

  // Path to the JSON file containing the list of open issues.
  val openIssuesJsonFile = File(repoPath, args[2])

  val todoExemptionTextProtoFilePath = "scripts/assets/todo_exemptions"

  // List of all the open issues on GitHub for this repository.
  val openIssueList = retrieveOpenIssueList(openIssuesJsonFile)

  val todoExemptionList =
    loadTodoExemptionsProto(pathToProtoBinary).getTodoExemptionList()

  val allTodos = TodoCollector.collectTodos(repoPath)

  val poorlyFormattedTodos = allTodos.filter { todo ->
    checkIfTodoIsPoorlyFormatted(todo.lineContent)
  }

  val openIssueFailureTodos = (allTodos - poorlyFormattedTodos).filter { todo ->
    checkIfOpenIssueFailure(todo.lineContent, openIssueList)
  }

  val redundantExemptions = retrieveRedundantExemptions(
    todos = poorlyFormattedTodos + openIssueFailureTodos,
    todoExemptionList = todoExemptionList,
    repoPath = repoPath
  )

  val poorlyFormattedTodosAfterExemption = retrieveTodosAfterExemption(
    todos = poorlyFormattedTodos,
    todoExemptionList = todoExemptionList,
    repoPath = repoPath
  )

  val openIssueFailureTodosAfterExemption = retrieveTodosAfterExemption(
    todos = openIssueFailureTodos,
    todoExemptionList = todoExemptionList,
    repoPath = repoPath
  )

  logRedundantExemptions(redundantExemptions, todoExemptionTextProtoFilePath)

  logFailures(
    invalidTodos = poorlyFormattedTodosAfterExemption,
    failureMessage = "TODOs not in correct format:",
    failureNote =
    "The TODO should be in the format: TODO(#ISSUE_NUMBER): <todo_description>"
  )

  logFailures(
    invalidTodos = openIssueFailureTodosAfterExemption,
    failureMessage = "TODOs not corresponding to an open issue:",
    failureNote =
    "Note that, every TODO must correspond to an open issue on GitHub"
  )

  if (
    redundantExemptions.isNotEmpty() ||
    poorlyFormattedTodosAfterExemption.isNotEmpty() ||
    openIssueFailureTodosAfterExemption.isNotEmpty()
  ) {
    throw Exception("TODO CHECK FAILED")
  } else {
    println("TODO CHECK PASSED")
  }
}

private fun retrieveTodosAfterExemption(
  todos: List<Todo>,
  todoExemptionList: List<TodoExemption>,
  repoPath: String
): List<Todo> {
  return todos.filter { todo ->
    !todoExemptionList.any { it ->
      it.exemptedFilePath == todo.filePath.removePrefix(repoPath) &&
        todo.lineNumber in it.getLineNumberList()
    }
  }
}

private fun retrieveRedundantExemptions(
  todos: List<Todo>,
  todoExemptionList: List<TodoExemption>,
  repoPath: String
): List<Pair<String, Int>> {
  return todoExemptionList.flatMap { exemption ->
    exemption.getLineNumberList().mapNotNull { exemptedLineNumber ->
      if (
        !todos.any {
          it.filePath.removePrefix(repoPath) == exemption.exemptedFilePath &&
            it.lineNumber == exemptedLineNumber
        }) {
        Pair(exemption.exemptedFilePath, exemptedLineNumber)
      } else {
        null
      }
    }
  }
}

private fun checkIfTodoIsPoorlyFormatted(codeLine: String): Boolean {
  val todoStartingRegex = Regex(
    pattern = "(//|<!--|#|\\*)[\\s]*\\bTODO\\b",
    option = RegexOption.IGNORE_CASE
  )
  val todoRegex = "\\bTODO\\b\\(".toRegex()
  val correctTodoFormat = "\\bTODO\\b\\(#(\\d+)\\): .+".toRegex()
  if (todoStartingRegex.containsMatchIn(codeLine)) {
    if (!correctTodoFormat.containsMatchIn(codeLine)) {
      return true
    }
    return false
  }
  if (todoRegex.containsMatchIn(codeLine)) {
    return true
  }
  return false
}

private fun checkIfOpenIssueFailure(codeLine: String, openIssueList: List<Issue>): Boolean {
  val correctTodoFormatRegex = "\\bTODO\\b\\(#(\\d+)\\): .+".toRegex()
  if (!correctTodoFormatRegex.containsMatchIn(codeLine)) {
    return false
  }
  val match = correctTodoFormatRegex.find(codeLine)
  val parsedIssueNumberFromTodo = match?.groupValues?.get(1)
  return !openIssueList.any { it -> it.issueNumber == parsedIssueNumberFromTodo }
}

private fun logRedundantExemptions(
  redundantExemptions: List<Pair<String, Int>>,
  todoExemptionTextProtoFilePath: String
) {
  if (redundantExemptions.isNotEmpty()) {
    println("Redundant exemptions:")
    redundantExemptions.sortedWith(compareBy({ it.first }, { it.second })).forEach { exemption ->
      println("- ${exemption.first}:${exemption.second}")
    }
    println(
      "Please remove them from $todoExemptionTextProtoFilePath.textproto"
    )
    println()
  }
}

/**
 * Logs the TODO failures.
 *
 * @param invalidTodos a list of all the invalid TODOs present in the repository. A TODO is
 *     considered to be invalid if it is poorly formatted or if it does not corresponds to an open
 *     issue on Github.
 * @param failureMessage the failure message to be logged
 */
private fun logFailures(invalidTodos: List<Todo>, failureMessage: String, failureNote: String) {
  if (invalidTodos.isNotEmpty()) {
    println(failureMessage)
    invalidTodos.sortedWith(compareBy({ it.filePath }, { it.lineNumber })).forEach {
      println("- ${it.filePath}:${it.lineNumber}")
    }
    println(failureNote)
    println()
  }
}

/**
 * Retrieves the list of all open issues on Github by parsing the JSON file generated by the Github
 * API.
 *
 * @param pathToOpenIssuesJsonFile path to the JSON file containing the list of open issues
 * @return list of all open issues
 */
private fun retrieveOpenIssueList(openIssuesJsonFile: File): List<Issue> {
  val openIssuesJsonText = openIssuesJsonFile
    .inputStream()
    .bufferedReader()
    .use { it.readText() }
  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val listType = Types.newParameterizedType(List::class.java, Issue::class.java)
  val adapter: JsonAdapter<List<Issue>> = moshi.adapter(listType)
  return adapter.fromJson(openIssuesJsonText)
    ?: throw Exception("Failed to parse $openIssuesJsonFile")
}

/**
 * Loads the todo exemptions list from a text proto file.
 *
 * @param pathToProtoBinary the location of the exemption textproto file
 * @return proto class from the parsed textproto file
 */
private fun loadTodoExemptionsProto(pathToProtoBinary: String): TodoExemptions {
  val protoBinaryFile = File(pathToProtoBinary)
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

