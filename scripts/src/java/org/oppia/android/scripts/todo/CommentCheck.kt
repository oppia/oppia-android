package org.oppia.android.scripts.todo

import java.io.File
import kotlin.system.exitProcess

fun main(vararg args: String) {
  val repoPath = args[0]

  val latestCommentFilePath = args[1]

  val failureCommentFilePath = args[2]

  val latestCommentContentList = File(repoPath, latestCommentFilePath).readText().trim().lines()

  val failureCommentContentList = File(repoPath, failureCommentFilePath).readText().trim().lines()

  if (latestCommentContentList.size != failureCommentContentList.size) exitProcess(0)

  if (latestCommentContentList.first() != failureCommentContentList.first()) exitProcess(0)

  for (index in 1 until latestCommentContentList.size) {
    val latestCommentLineContent = latestCommentContentList[index].substring(85)
    val failureCommentLineContent = failureCommentContentList[index].substring(85)
    if (latestCommentLineContent != failureCommentLineContent) exitProcess(0)
  }

  throw Exception("COMMENT ALREADY EXISTS")
}
