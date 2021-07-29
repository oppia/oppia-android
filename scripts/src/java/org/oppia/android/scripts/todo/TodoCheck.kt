package org.oppia.android.scripts.todo

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.proto.TodoExemption
import org.oppia.android.scripts.proto.TodoExemptions
import org.oppia.android.scripts.todo.data.Issue
import org.oppia.android.scripts.todo.data.Todo
import java.io.File
import java.io.FileInputStream

/**
 * Script for ensuring that all TODOs present in the repository are correctly formatted and
 * corresponds to an open issue on GitHub.
 *
 * Usage:
 *   bazel run //scripts:todo_check -- <path_to_directory_root> <path_to_proto_binary>
 *   <path_to_json_file>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_proto_binary: relative path to the exemption .pb file.
 * - path_to_json_file: path to the json file containing the list of all open issues on Github
 *
 * Example:
 *   bazel run //scripts:todo_check -- $(pwd) scripts/assets/todo_exemptions.pb open_issues.json
 *
 * NOTE TO DEVELOPERS: The script is executed in the CI enviornment. The CI workflow creates a
 * json file from the GitHub api which contains a list of all open issues of the
 * oppia/oppia-android repository. To execute it without the CI, please create an open issues json
 * file and provide its path to the script in the format as stated above.
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  val pathToProtoBinary = args[1]

  // Path to the JSON file containing the list of open issues.
  val openIssuesJsonFile = File(repoPath, args[2])

  val todoExemptionTextProtoFilePath = "scripts/assets/todo_exemptions"

  // List of all the open issues on GitHub of this repository.
  val openIssueList = retrieveOpenIssueList(openIssuesJsonFile)

  val todoExemptionList =
    loadTodoExemptionsProto(pathToProtoBinary).getTodoExemptionList()

  val todoRegex = "\\bTODO\\b\\(".toRegex()

  val todoStartingRegex = Regex(
    pattern = "(//|<!--|#|\\*)[\\s]*\\bTODO\\b",
    option = RegexOption.IGNORE_CASE
  )

  val correctTodoFormatRegex = "\\bTODO\\b\\(#(\\d+)\\): .+".toRegex()

  val allTodos = TodoCollector.collectTodos(repoPath)

  val poorlyFormattedTodos = allTodos.filter { todo ->
    checkIfTodoIsPoorlyFormatted(
      codeLine = todo.lineContent,
      todoRegex = todoRegex,
      todoStartingRegex = todoStartingRegex,
      correctTodoFormatRegex = correctTodoFormatRegex
    )
  }

  val openIssueFailureTodos = (allTodos - poorlyFormattedTodos).filter { todo ->
    checkIfOpenIssueFailure(
      codeLine = todo.lineContent,
      openIssueList = openIssueList,
      correctTodoFormatRegex = correctTodoFormatRegex
    )
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

/**
 * Retrieves the todo failures list after filtering them from the exemptions.
 *
 * @param todos the list of all the failure causing todos
 * @param todoExemptionList the list contating the todo exemptions
 * @param repoPath path of the repo to be analyzed
 * @return list obtained after filtering the exemptions
 */
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

/**
 * Retrieves a list of redundant exemptions.
 *
 * @param todos the list of all the failure causing todos
 * @param todoExemptionList the list contating the todo exemptions
 * @param repoPath path of the repo to be analyzed
 * @return a list of all the redundant exemptions
 */
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
        }
      ) {
        Pair(exemption.exemptedFilePath, exemptedLineNumber)
      } else {
        null
      }
    }
  }
}

/**
 * Checks whether a line of code contains a poorly formatted todo.
 *
 * @param codeLine the line of code to be checked
 * @param todoRegex regex of todo
 * @param todoStartingRegex regex for the starting of the todo
 * @param correctTodoFormatRegex regex of the correct todo format
 * @return whether the line contains a poorly formatted TODO
 */
private fun checkIfTodoIsPoorlyFormatted(
  codeLine: String,
  todoRegex: Regex,
  todoStartingRegex: Regex,
  correctTodoFormatRegex: Regex
): Boolean {
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

/**
 * Checks whether a todo does not corresponds to an open issue on GitHub.
 *
 * @param codeLine the line of code to be checked
 * @param openIssueList the list of all the open issues of this repository on GitHub
 * @param correctTodoFormatRegex regex of the correct todo format
 * @return whether the todo does not corresponds to an open issue
 */
private fun checkIfOpenIssueFailure(
  codeLine: String,
  openIssueList: List<Issue>,
  correctTodoFormatRegex: Regex
): Boolean {
  if (!correctTodoFormatRegex.containsMatchIn(codeLine)) {
    return false
  }
  val match = correctTodoFormatRegex.find(codeLine)
  val parsedIssueNumberFromTodo = match?.groupValues?.get(1)
  return !openIssueList.any { it -> it.issueNumber == parsedIssueNumberFromTodo }
}

/**
 * Logs the redundant exemptions.
 *
 * @param redundantExemptions list of redundant exemptions
 * @param todoExemptionTextProtoFilePath the location of the todo exemption textproto file
 */
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
 * Logs the Todo check failures.
 *
 * @param invalidTodos a list of all the invalid TODOs present in the repository. A TODO is
 *     considered to be invalid if it is poorly formatted or if it does not corresponds to an open
 *     issue on GitHub.
 * @param failureMessage the failure message to be logged
 * @param failureNote the failure note
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
 * Retrieves the list of all open issues on GitHub by parsing the JSON file generated by the GitHub
 * API.
 *
 * @param openIssuesJsonFile file containing all the open issues of the repository
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
