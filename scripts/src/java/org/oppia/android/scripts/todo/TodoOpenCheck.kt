package org.oppia.android.scripts.todo

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.proto.TodoOpenExemption
import org.oppia.android.scripts.proto.TodoOpenExemptions
import org.oppia.android.scripts.todo.model.Issue
import org.oppia.android.scripts.todo.model.Todo
import java.io.File
import java.io.FileInputStream

/**
 * Script for ensuring that all TODOs present in the repository are correctly formatted and
 * corresponds to open issues on GitHub.
 *
 * Usage:
 *   bazel run //scripts:todo_open_check -- <path_to_directory_root> <path_to_proto_binary>
 *   <path_to_json_file>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_proto_binary: relative path to the exemption .pb file.
 * - path_to_json_file: path to the json file containing the list of all open issues on Github
 *
 * Example:
 *   bazel run //scripts:todo_open_check -- $(pwd) scripts/assets/todo_open_exemptions.pb
 *   open_issues.json
 *
 * NOTE TO DEVELOPERS: The script is executed in the CI enviornment. The CI workflow creates a
 * json file from the GitHub api which contains a list of all open issues of the
 * oppia/oppia-android repository. To execute it without the CI, please create open issues json
 * file and provide its path to the script in the format as stated above.
 *
 * Instructions to create the open_issues.json file:
 * 1. Set up Github CLI Tools locally.
 * 2. cd to the oppia-android repository.
 * 3. Run the command: gh issue list --limit 2000 --repo oppia/oppia-android
 * --json number > $(pwd)/open_issues.json
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  val pathToProtoBinary = args[1]

  // Path to the JSON file containing the list of open issues.
  val openIssuesJsonFile = File(repoPath, args[2])

  check(openIssuesJsonFile.exists()) { "$repoPath${args[2]}: No such file exists" }

  val todoExemptionTextProtoFilePath = "scripts/assets/todo_exemptions"

  // List of all the open issues on GitHub of this repository.
  val openIssueList = retrieveOpenIssueList(openIssuesJsonFile)

  val todoExemptionList =
    loadTodoExemptionsProto(pathToProtoBinary).getTodoOpenExemptionList()

  val allTodos = TodoCollector.collectTodos(repoPath)

  val poorlyFormattedTodos = TodoCollector.collectPoorlyFormattedTodos(allTodos)

  val correctlyFormattedTodos = TodoCollector.collectCorrectlyFormattedTodos(
    allTodos - poorlyFormattedTodos
  )

  val openIssueFailureTodos = correctlyFormattedTodos.filter { todo ->
    checkIfIssueDoesNotMatchOpenIssue(codeLine = todo.lineContent, openIssueList = openIssueList)
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
  )

  if (poorlyFormattedTodosAfterExemption.isNotEmpty()) {
    println(
      "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks for more" +
        " details on how to fix this.\n"
    )
  }

  logFailures(
    invalidTodos = openIssueFailureTodosAfterExemption,
    failureMessage = "TODOs not corresponding to open issues on GitHub:",
  )

  if (openIssueFailureTodosAfterExemption.isNotEmpty()) {
    println(
      "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks for more" +
        " details on how to fix this.\n"
    )
  }

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
 * Retrieves the TODO open check failures list after filtering them from the exemptions.
 *
 * @param todos the list of all the failure causing TODOs
 * @param todoExemptionList the list contating the TODO exemptions
 * @param repoPath path of the repo to be analyzed
 * @return list obtained after filtering the exemptions
 */
private fun retrieveTodosAfterExemption(
  todos: List<Todo>,
  todoExemptionList: List<TodoOpenExemption>,
  repoPath: String
): List<Todo> {
  return todos.filter { todo ->
    todoExemptionList.none { it ->
      it.exemptedFilePath == todo.filePath.removePrefix(repoPath) &&
        todo.lineNumber in it.getLineNumberList()
    }
  }
}

/**
 * Retrieves a list of redundant exemptions.
 *
 * @param todos the list of all the failure causing TODOs
 * @param todoExemptionList the list contating the TODO exemptions
 * @param repoPath path of the repo to be analyzed
 * @return a list of all the redundant exemptions
 */
private fun retrieveRedundantExemptions(
  todos: List<Todo>,
  todoExemptionList: List<TodoOpenExemption>,
  repoPath: String
): List<Pair<String, Int>> {
  return todoExemptionList.flatMap { exemption ->
    exemption.getLineNumberList().mapNotNull { exemptedLineNumber ->
      val isRedundantExemption = todos.none {
        it.filePath.removePrefix(repoPath) == exemption.exemptedFilePath &&
          it.lineNumber == exemptedLineNumber
      }
      if (isRedundantExemption) {
        Pair(exemption.exemptedFilePath, exemptedLineNumber)
      } else {
        null
      }
    }
  }
}

/**
 * Checks whether a TODO does not corresponds to open issues on GitHub.
 *
 * @param codeLine the line of code to be checked
 * @param openIssueList the list of all the open issues of this repository on GitHub
 * @return whether the TODO does not corresponds to open issues on GitHub
 */
private fun checkIfIssueDoesNotMatchOpenIssue(
  codeLine: String,
  openIssueList: List<Issue>,
): Boolean {
  val parsedIssueNumberFromTodo = TodoCollector.parseIssueNumberFromTodo(codeLine)
  return openIssueList.none { it -> it.issueNumber == parsedIssueNumberFromTodo }
}

/**
 * Logs the redundant exemptions.
 *
 * @param redundantExemptions list of redundant exemptions
 * @param todoExemptionTextProtoFilePath the location of the TODO exemption textproto file
 */
private fun logRedundantExemptions(
  redundantExemptions: List<Pair<String, Int>>,
  todoExemptionTextProtoFilePath: String
) {
  if (redundantExemptions.isNotEmpty()) {
    println("Redundant exemptions (there are no TODOs corresponding to these lines):")
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
 * Logs the TODO open check failures.
 *
 * @param invalidTodos a list of all the invalid TODOs present in the repository. A TODO is
 *     considered to be invalid if it is poorly formatted or if it does not corresponds to open
 *     issues on GitHub.
 * @param failureMessage the failure message to be logged
 */
private fun logFailures(invalidTodos: List<Todo>, failureMessage: String) {
  if (invalidTodos.isNotEmpty()) {
    println(failureMessage)
    invalidTodos.sortedWith(compareBy({ it.filePath }, { it.lineNumber })).forEach {
      println("- ${it.filePath}:${it.lineNumber}")
    }
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
 * Loads the TODO open check exemptions list corresponding to a text proto file.
 *
 * @param pathToProtoBinary the location of the exemption textproto file
 * @return proto class from the parsed textproto file
 */
private fun loadTodoExemptionsProto(pathToProtoBinary: String): TodoOpenExemptions {
  val protoBinaryFile = File(pathToProtoBinary)
  val builder = TodoOpenExemptions.getDefaultInstance().newBuilderForType()

  // This cast is type-safe since proto guarantees type consistency from mergeFrom(),
  // and this method is bounded by the generic type T.
  @Suppress("UNCHECKED_CAST")
  val protoObj: TodoOpenExemptions =
    FileInputStream(protoBinaryFile).use {
      builder.mergeFrom(it)
    }.build() as TodoOpenExemptions
  return protoObj
}
