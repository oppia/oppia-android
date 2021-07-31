package org.oppia.android.scripts.todo

import org.oppia.android.scripts.todo.data.Todo
import java.io.File

/**
 * Script for ensuring that all TODOs of the closed issue are resolved.
 *
 * Usage:
 *   bazel run //scripts:closed_issue_check -- <path_to_directory_root> <closed_issue_number>
 *   <github_sha>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - closed_issue_number: issue number of the closed issue.
 * - github_sha: sha of the latest commit on the develop branch.
 *
 * Example:
 *   bazel run //scripts:closed_issue_check -- $(pwd) 6 3898188
 *
 * NOTE TO DEVELOPERS: The script is executed in the CI enviornment.
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  // Issue number of the closed issue.
  val closedIssueNumber = args[1]

  val commitSha = args[2]

  val githubPermalinkUrl = "https://github.com/oppia/oppia-android/blob/$commitSha/"

  val correctTodoFormatRegex = "\\bTODO\\b\\(#(\\d+)\\): .+".toRegex()

  val allTodos = TodoCollector.collectTodos(repoPath)

  val closedIssueFailureTodos = allTodos.filter { todo ->
    checkIfClosedIssueFailure(
      codeLine = todo.lineContent,
      closedIssueNumber = closedIssueNumber,
      correctTodoFormatRegex = correctTodoFormatRegex
    )
  }

  logFailures(
    closedIssueFailureTodos = closedIssueFailureTodos,
    failureMessage = "The following TODOs are unresolved for the closed issue:"
  )

  if (closedIssueFailureTodos.isNotEmpty()) {
    generateTodoListFile(repoPath, closedIssueFailureTodos, githubPermalinkUrl)
    throw Exception("CLOSED ISSUE CHECK PASSED")
  } else {
    println("CLOSED ISSUE CHECK PASSED")
  }
}

/**
 * Checks whether a todo corresponds to the closed issue.
 *
 * @param codeLine line content corresponding to the todo
 * @param closedIssueNumber issue number of the closed issue
 * @param correctTodoFormatRegex regex pattern for the correct todo format
 */
private fun checkIfClosedIssueFailure(
  codeLine: String,
  closedIssueNumber: String,
  correctTodoFormatRegex: Regex
): Boolean {
  if (!correctTodoFormatRegex.containsMatchIn(codeLine)) {
    return false
  }
  val match = correctTodoFormatRegex.find(codeLine)
  val parsedIssueNumberFromTodo = match?.groupValues?.get(1)
  return parsedIssueNumberFromTodo == closedIssueNumber
}

/**
 * Generates a file containing all the todos corresponding to the closed issue.
 *
 * @param repoPath path of the repo to be analyzed
 * @param closedIssueFailureTodos list of all the unresolved todos corresponding to the closed issue
 * @param githubPermalinkUrl the GitHub url for the permalinks
 */
private fun generateTodoListFile(
  repoPath: String,
  closedIssueFailureTodos: List<Todo>,
  githubPermalinkUrl: String
) {
  val todoListFile = File(repoPath + "todo_list.txt")
  todoListFile.appendText("The issue is reopened because of the following unresolved TODOs:")
  todoListFile.appendText("\n")
  closedIssueFailureTodos.forEach { todo ->
    todoListFile.appendText(
      "$githubPermalinkUrl${(todo.filePath).removePrefix(repoPath)}#L${todo.lineNumber}"
    )
    todoListFile.appendText("\n")
  }
}

/**
 * Logs the Closed Issue Check failures.
 *
 * @param closedIssueFailureTodos list of all the unresolved todos for the closed issue
 * @param failureMessage the failure message to be logged
 */
private fun logFailures(closedIssueFailureTodos: List<Todo>, failureMessage: String) {
  if (closedIssueFailureTodos.isNotEmpty()) {
    println(failureMessage)
    closedIssueFailureTodos.sortedWith(compareBy({ it.filePath }, { it.lineNumber })).forEach {
      println("- ${it.filePath}:${it.lineNumber}")
    }
    println()
  }
}
