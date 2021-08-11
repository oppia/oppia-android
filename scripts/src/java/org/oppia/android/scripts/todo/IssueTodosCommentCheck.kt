package org.oppia.android.scripts.todo

import java.io.File

/**
 * Script for ensuring that the [IssueTodosResolvedCheck] failure comment is not the same as the
 * latest comment of the closed issue.
 *
 * Usage:
 *   bazel run //scripts:issue_todos_comment_check -- <path_to_directory_root>
 *   <path_to_latest_comment_file> <path_to_script_failure_comment_file>
 *
 * Arguments:
 * - path_to_directory_root: directory path to the root of the Oppia Android repository.
 * - path_to_latest_comment_file: file path to the latest comment body.
 * - path_to_script_failure_comment_file: file path to the script failure comment body.
 *
 * Example:
 *   bazel run //scripts:issue_todos_comment_check -- $(pwd) latest_comment.txt script_failures.txt
 *
 * NOTE TO DEVELOPERS: The script is executed in the CI enviornment. The CI workflow provides the
 * file path to the latest comment body of the closed issue and the file path to the script failure
 * comment body.
 */
fun main(vararg args: String) {
  val repoPath = args[0]

  val latestCommentFilePath = args[1]

  val failureCommentFilePath = args[2]

  val permaLinkPrefix = "https://github.com/oppia/oppia-android/blob/"

  // Here, we are adding 40 to account for the commit Sha size
  val compareStartIndex = permaLinkPrefix.length + 40

  val latestCommentContentList = File(repoPath, latestCommentFilePath).readText().trim().lines()

  val failureCommentContentList = File(repoPath, failureCommentFilePath).readText().trim().lines()

  if (latestCommentContentList.size != failureCommentContentList.size) return

  if (latestCommentContentList.first() != failureCommentContentList.first()) return

  for (index in 1 until latestCommentContentList.size) {
    // The commit SHA can vary from workflow to workflow. This can make the permalinks different.
    // Hence, we are comparing by the relative file path.
    val latestCommentLineContent = latestCommentContentList[index].substring(compareStartIndex)
    val failureCommentLineContent = failureCommentContentList[index].substring(compareStartIndex)
    if (latestCommentLineContent != failureCommentLineContent) return
  }

  throw Exception("LATEST COMMENT IS SAME AS THE FAILURE COMMENT")
}
