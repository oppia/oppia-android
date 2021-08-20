package org.oppia.android.scripts.todo

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertion.assertThrows
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/** Tests for [TodoIssueCommentCheck]. */
class TodoIssueCommentCheckTest {
  private val outContent: ByteArrayOutputStream = ByteArrayOutputStream()
  private val originalOut: PrintStream = System.out
  private val permalinkPrefix = "https://github.com/oppia/oppia-android/blob"
  private val dummySha1 = "51ab6a0341cfb86d95a387438fc993b5eb977b83"
  private val dummySha2 = "74cd6a0341cfb86d95a387438fc993b5eb977b83"

  @Rule
  @JvmField
  var tempFolder = TemporaryFolder()

  @Before
  fun setUp() {
    System.setOut(PrintStream(outContent))
  }

  @After
  fun restoreStreams() {
    System.setOut(originalOut)
  }

  /**
   * This is the case when no comment is present on the issue. In this case the GitHub API produces
   * an empty comment body.
   */
  @Test
  fun testFailureComment_emptyLatestCommentBody_checkShouldFail() {
    val latestCommentFile = tempFolder.newFile("latest_comment.txt")
    val scriptFailureCommentFile = tempFolder.newFile("script_failures.txt")
    val latestCommentContent = ""
    val scriptFailureCommentContent =
      """
      The issue is reopened because of the following unresolved TODOs:
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L184
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L185
      
      """.trimIndent()
    latestCommentFile.writeText(latestCommentContent)
    scriptFailureCommentFile.writeText(scriptFailureCommentContent)

    val exception = assertThrows(Exception::class) {
      main(tempFolder.root.toString(), "latest_comment.txt", "script_failures.txt")
    }
    assertThat(exception).hasMessageThat().contains("NEW COMMENT SHOULD BE POSTED")
  }

  @Test
  fun testFailureComment_differentScriptFailures_withSameSha_checkShouldFail() {
    val latestCommentFile = tempFolder.newFile("latest_comment.txt")
    val scriptFailureCommentFile = tempFolder.newFile("script_failures.txt")
    val latestCommentContent =
      """
      The issue is reopened because of the following unresolved TODOs:
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L184
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L185
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L186
      
      
      """.trimIndent()
    val scriptFailureCommentContent =
      """
      The issue is reopened because of the following unresolved TODOs:
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L184
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L185
      
      """.trimIndent()
    latestCommentFile.writeText(latestCommentContent)
    scriptFailureCommentFile.writeText(scriptFailureCommentContent)

    val exception = assertThrows(Exception::class) {
      main(tempFolder.root.toString(), "latest_comment.txt", "script_failures.txt")
    }
    assertThat(exception).hasMessageThat().contains("NEW COMMENT SHOULD BE POSTED")
  }

  @Test
  fun testFailureComment_differentScriptFailures_withDifferentSha_checkShouldFail() {
    val latestCommentFile = tempFolder.newFile("latest_comment.txt")
    val scriptFailureCommentFile = tempFolder.newFile("script_failures.txt")
    val latestCommentContent =
      """
      The issue is reopened because of the following unresolved TODOs:
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L184
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L185
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L186
      
      
      """.trimIndent()
    val scriptFailureCommentContent =
      """
      The issue is reopened because of the following unresolved TODOs:
      $permalinkPrefix/$dummySha2/scripts/BUILD.bazel#L184
      $permalinkPrefix/$dummySha2/scripts/BUILD.bazel#L185
      
      """.trimIndent()
    latestCommentFile.writeText(latestCommentContent)
    scriptFailureCommentFile.writeText(scriptFailureCommentContent)

    val exception = assertThrows(Exception::class) {
      main(tempFolder.root.toString(), "latest_comment.txt", "script_failures.txt")
    }
    assertThat(exception).hasMessageThat().contains("NEW COMMENT SHOULD BE POSTED")
  }

  @Test
  fun testFailureComment_sameScriptFailures_withDifferentSha_checkShouldFail() {
    val latestCommentFile = tempFolder.newFile("latest_comment.txt")
    val scriptFailureCommentFile = tempFolder.newFile("script_failures.txt")
    val latestCommentContent =
      """
      The issue is reopened because of the following unresolved TODOs:
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L184
      $permalinkPrefix/$dummySha1/scripts/BUILD.bazel#L185
      
      
      """.trimIndent()
    val scriptFailureCommentContent =
      """
      The issue is reopened because of the following unresolved TODOs:
      $permalinkPrefix/$dummySha2/scripts/BUILD.bazel#L184
      $permalinkPrefix/$dummySha2/scripts/BUILD.bazel#L185
      
      """.trimIndent()
    latestCommentFile.writeText(latestCommentContent)
    scriptFailureCommentFile.writeText(scriptFailureCommentContent)

    main(tempFolder.root.toString(), "latest_comment.txt", "script_failures.txt")

    assertThat(outContent.toString().trim()).isEqualTo(
      "LATEST COMMENT IS SAME AS THE FAILURE COMMENT"
    )
  }
}
