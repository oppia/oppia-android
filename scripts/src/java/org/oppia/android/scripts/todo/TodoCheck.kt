package org.oppia.android.scripts.todo

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.oppia.android.scripts.todo.data.Issue
import java.io.File

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

  // Path to the JSON file containing the list of open issues.
  val pathToOpenIssuesJsonFile = "${args[0]}/open_issues.json"

  // List of all the open issues on Github for this repository.
  val openIssueList = retrieveOpenIssueList(pathToOpenIssuesJsonFile)

  val correctTodoFormatRegex = "TODO[\\(]#(\\d+)[\\)]: .+".toRegex()

  val allTodos = TodoCollector.collectTodos(repoPath)

  val poorlyFormattedTodos = allTodos.filter { todo ->
    checkIfTodoIsPoorlyFormatted(todo.lineContent)
  }

  val openIssueFailureTodos = (allTodos - poorlyFormattedTodos).filter { todo ->
    checkIfOpenIssueFailure(todo.lineContent, openIssueList)
  }

  openIssueFailureTodos.forEach {
    println("${it.filePath}")
  }
//  // List of all the poorly formatted TODOs.
//  val poorlyFormattedTodos = allTodos.filter { todo ->
//    val todoContent = retrieveTodoContent(todo)
//    !checkTodoFormatting(todoContent, correctTodoFormatRegex)
//  }
//
//  // List of all the TODOs which does not correspond to an open issue on Github.
//  val openIssueFailureTodos = allTodos.minus(poorlyFormattedTodos).filter { todo ->
//    val todoContent = retrieveTodoContent(todo)
//    val match = correctTodoFormatRegex.find(todoContent)
//    checkOpenIssueFailure(match, openIssueList)
//  }
//
//  logFailures(poorlyFormattedTodos, "TODO is poorly formatted")
//
//  logFailures(openIssueFailureTodos, "TODO does not corresponds to an open issue")
//
//  if (poorlyFormattedTodos.isNotEmpty() || openIssueFailureTodos.isNotEmpty()) {
//    throw Exception("TODO CHECK FAILED")
//  } else {
//    println("TODO CHECK PASSED")
//  }
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

/**
 * Checks if a TODO does not corresponds to an open issue on Github.
 *
 * @param match result obtained after matching the TODO against the correct TODO format
 * @param openIssueList a list of all the open issues on Github for this repository
 * @return whether the TODO does not corresponds to an open issue
 */
private fun checkOpenIssueFailure(match: MatchResult?, openIssueList: List<Issue>): Boolean {
  val parsedIssueNumberFromTodo = match?.groupValues?.get(1)
  return !openIssueList.any { it -> it.issueNumber == parsedIssueNumberFromTodo }
}

/**
 * Logs the TODO failures.
 *
 * @param invalidTodos a list of all the invalid TODOs present in the repository. A TODO is
 *     considered to be invalid if it is poorly formatted or if it does not corresponds to an open
 *     issue on Github.
 * @param failureMessage the failure message to be logged
 */
private fun logFailures(invalidTodos: List<Todo>, failureMessage: String) {
  if (invalidTodos.isNotEmpty()) {
    invalidTodos.sortedWith(compareBy({ it.filePath }, { it.lineNumber })).forEach {
      val file = it.first
      val lineIndex = it.second
      // Here, we are incrementing by one because the line number is always one greater than
      // than the line index.
      println("$file:${lineIndex + 1}: $failureMessage")
    }
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
private fun retrieveOpenIssueList(pathToOpenIssuesJsonFile: String): List<Issue> {
  val openIssuesJsonText = File(pathToOpenIssuesJsonFile)
    .inputStream()
    .bufferedReader()
    .use { it.readText() }
  val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
  val listType = Types.newParameterizedType(List::class.java, Issue::class.java)
  val adapter: JsonAdapter<List<Issue>> = moshi.adapter(listType)
  return adapter.fromJson(openIssuesJsonText)
    ?: throw Exception("Failed to parse $pathToOpenIssuesJsonFile")
}
