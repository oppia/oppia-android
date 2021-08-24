package org.oppia.android.scripts.todo

import org.oppia.android.scripts.todo.model.Todo
import java.io.File

/**
 * Script for ensuring that all TODOs of the closed issue are resolved.
 *
 * Usage:
 *   bazel run //scripts:todo_issue_resolved_check -- <path_to_directory_root>
 *   <closed_issue_number> <github_sha>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - closed_issue_number: issue number of the closed issue.
 * - github_sha: sha of the latest commit on the develop branch.
 *
 * Example:
 *   bazel run //scripts:todo_issue_resolved_check -- $(pwd)
 *   6 77ff8361b4bde52f695ceb91aa1aab36932a94fe
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

  val todoIssueResolvedFailures = allTodos.filter { todo ->
    checkIfTodoIssueResolvedFailure(
      codeLine = todo.lineContent,
      closedIssueNumber = closedIssueNumber
    )
  }

  logFailures(
    todoIssueResolvedFailures = todoIssueResolvedFailures,
    failureMessage = "The following TODOs are unresolved for the closed issue:"
  )

  if (todoIssueResolvedFailures.isNotEmpty()) {
    println(
      "Refer to https://github.com/oppia/oppia-android/wiki/Static-Analysis-Checks" +
        "#todo-issue-resolved-check for more details on how to fix this.\n"
    )
  }

  if (todoIssueResolvedFailures.isNotEmpty()) {
    generateTodoListFile(repoPath, todoIssueResolvedFailures, githubPermalinkUrl)
    throw Exception("TODO ISSUE RESOLVED CHECK FAILED")
  } else {
    println("TODO ISSUE RESOLVED CHECK PASSED")
  }
}

/**
 * Checks whether a TODO corresponds to the closed issue.
 *
 * @param codeLine line content corresponding to the todo
 * @param closedIssueNumber issue number of the closed issue
 */
private fun checkIfTodoIssueResolvedFailure(codeLine: String, closedIssueNumber: String): Boolean {
  val parsedIssueNumberFromTodo = TodoCollector.parseIssueNumberFromTodo(codeLine)
  return parsedIssueNumberFromTodo == closedIssueNumber
}

/**
 * Generates a file containing all the todos corresponding to the closed issue.
 *
 * @param repoPath path of the repo to be analyzed
 * @param todoIssueResolvedFailures list of all the unresolved todos corresponding to the closed
 *     issue.
 * @param githubPermalinkUrl the GitHub url for the permalinks
 */
private fun generateTodoListFile(
  repoPath: String,
  todoIssueResolvedFailures: List<Todo>,
  githubPermalinkUrl: String
) {
  val todoListFile = File(repoPath + "script_failures.txt")
  todoListFile.appendText("The issue is reopened because of the following unresolved TODOs:\n")
  todoIssueResolvedFailures.sortedWith(compareBy({ it.filePath }, { it.lineNumber }))
    .forEach { todo ->
      todoListFile.appendText(
        "$githubPermalinkUrl/${(todo.filePath).removePrefix(repoPath)}#L${todo.lineNumber}\n"
      )
    }
}

/**
 * Logs the TODO issue resolved check failures.
 *
 * @param todoIssueResolvedFailures list of all the unresolved todos for the closed issue
 * @param failureMessage the failure message to be logged
 */
private fun logFailures(todoIssueResolvedFailures: List<Todo>, failureMessage: String) {
  if (todoIssueResolvedFailures.isNotEmpty()) {
    println(failureMessage)
    todoIssueResolvedFailures.sortedWith(compareBy({ it.filePath }, { it.lineNumber })).forEach {
      println("- ${it.filePath}:${it.lineNumber}")
    }
    println()
  }
}
