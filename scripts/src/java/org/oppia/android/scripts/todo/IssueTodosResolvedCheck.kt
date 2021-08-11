package org.oppia.android.scripts.todo

import org.oppia.android.scripts.todo.model.Todo
import java.io.File

/**
 * Script for ensuring that all TODOs of the closed issue are resolved.
 *
 * Usage:
 *   bazel run //scripts:issue_todos_resolved_check -- <path_to_directory_root>
 *   <closed_issue_number> <github_sha>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - closed_issue_number: issue number of the closed issue.
 * - github_sha: sha of the latest commit on the develop branch.
 *
 * Example:
 *   bazel run //scripts:issue_todos_resolved_check -- $(pwd) 6
 *   77ff8361b4bde52f695ceb91aa1aab36932a94fe
 *
 * NOTE TO DEVELOPERS: The script is executed in the CI enviornment.
 */
fun main(vararg args: String) {
  // Path of the repo to be analyzed.
  val repoPath = "${args[0]}/"

  // Issue number of the closed issue.
  val closedIssueNumber = args[1]

  val commitSha = args[2]

  val githubPermalinkUrl = "https://github.com/oppia/oppia-android/blob/$commitSha"

  val allTodos = TodoCollector.collectCorrectlyFormattedTodos(TodoCollector.collectTodos(repoPath))

  val issueTodosResolvedFailures = allTodos.filter { todo ->
    checkIfIssueTodosResolvedFailure(
      codeLine = todo.lineContent,
      closedIssueNumber = closedIssueNumber
    )
  }

  logFailures(
    issueTodosResolvedFailures = issueTodosResolvedFailures,
    failureMessage = "The following TODOs are unresolved for the closed issue:"
  )

  if (issueTodosResolvedFailures.isNotEmpty()) {
    generateTodoListFile(repoPath, issueTodosResolvedFailures, githubPermalinkUrl)
    throw Exception("ISSUE TODOS RESOLVED CHECK FAILED")
  } else {
    println("ISSUE TODOS RESOLVED CHECK PASSED")
  }
}

/**
 * Checks whether a todo corresponds to the closed issue.
 *
 * @param codeLine line content corresponding to the todo
 * @param closedIssueNumber issue number of the closed issue
 */
private fun checkIfIssueTodosResolvedFailure(codeLine: String, closedIssueNumber: String): Boolean {
  val parsedIssueNumberFromTodo = TodoCollector.parseIssueNumberFromTodo(codeLine)
  return parsedIssueNumberFromTodo == closedIssueNumber
}

/**
 * Generates a file containing all the todos corresponding to the closed issue.
 *
 * @param repoPath path of the repo to be analyzed
 * @param issueTodosResolvedFailures list of all the unresolved todos corresponding to the closed issue
 * @param githubPermalinkUrl the GitHub url for the permalinks
 */
private fun generateTodoListFile(
  repoPath: String,
  issueTodosResolvedFailures: List<Todo>,
  githubPermalinkUrl: String
) {
  val todoListFile = File(repoPath + "script_failures.txt")
  todoListFile.appendText("The issue is reopened because of the following unresolved TODOs:")
  todoListFile.appendText("\n")
  issueTodosResolvedFailures.sortedWith(compareBy({ it.filePath }, { it.lineNumber }))
    .forEach { todo ->
      todoListFile.appendText(
        "$githubPermalinkUrl/${(todo.filePath).removePrefix(repoPath)}#L${todo.lineNumber}"
      )
      todoListFile.appendText("\n")
    }
}

/**
 * Logs the issue todos resolved check failures.
 *
 * @param issueTodosResolvedFailures list of all the unresolved todos for the closed issue
 * @param failureMessage the failure message to be logged
 */
private fun logFailures(issueTodosResolvedFailures: List<Todo>, failureMessage: String) {
  if (issueTodosResolvedFailures.isNotEmpty()) {
    println(failureMessage)
    issueTodosResolvedFailures.sortedWith(compareBy({ it.filePath }, { it.lineNumber })).forEach {
      println("- ${it.filePath}:${it.lineNumber}")
    }
    println()
  }
}
