package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/**
 * Tests for [GoogleGitHubCiClient].
 *
 * Uses [MockWebServer] to intercept HTTP calls and verify request structure and response handling
 * without making real network calls to GitHub's REST API.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class GoogleGitHubCiClientTest {
  private lateinit var server: MockWebServer
  private lateinit var client: GoogleGitHubCiClient

  @Before
  fun setUp() {
    server = MockWebServer()
    server.start()
    client = GoogleGitHubCiClient(
      accessToken = "test-token",
      repoOwner = "test-owner",
      repoName = "test-repo",
      overrideApiBaseUrl = server.url("/").toString()
    )
  }

  @After
  fun tearDown() {
    server.shutdown()
  }

  @Test
  fun testListCommits_successResponse_returnsCorrectCommitSummaries() {
    server.enqueue(
      MockResponse().setResponseCode(200).setBody(
        """[
          {"sha":"aaaa0000000000000000000000000000000000aa","commit":{"message":"First"}},
          {"sha":"bbbb0000000000000000000000000000000000bb","commit":{"message":"Second"}}
        ]"""
      )
    )

    val commits = client.listCommits("develop", limit = 10)

    assertThat(commits).hasSize(2)
    assertThat(commits[0].sha).isEqualTo("aaaa0000000000000000000000000000000000aa")
    assertThat(commits[1].sha).isEqualTo("bbbb0000000000000000000000000000000000bb")
  }

  @Test
  fun testListCommits_emptyList_returnsEmptyList() {
    server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

    val commits = client.listCommits("develop", limit = 10)

    assertThat(commits).isEmpty()
  }

  @Test
  fun testListCommits_errorResponse_throwsIllegalStateExceptionWithBranchName() {
    server.enqueue(MockResponse().setResponseCode(401).setBody("""{"message":"Bad credentials"}"""))

    val exception = assertThrows<IllegalStateException> {
      client.listCommits("develop", limit = 10)
    }

    assertThat(exception).hasMessageThat().contains("develop")
    assertThat(exception).hasMessageThat().contains("401")
  }

  @Test
  fun testListCommits_sendsAuthorizationHeader() {
    server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

    client.listCommits("develop", limit = 10)

    val request = server.takeRequest()
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-token")
  }

  @Test
  fun testListCommits_sendsGitHubApiVersionHeader() {
    server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))

    client.listCommits("develop", limit = 10)

    val request = server.takeRequest()
    assertThat(request.getHeader("X-GitHub-Api-Version")).isEqualTo("2022-11-28")
  }

  @Test
  fun testGetCheckRunStatus_noRuns_returnsNoChecks() {
    server.enqueue(checkRunsPage(totalCount = 0, runs = emptyList()))

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.NO_CHECKS)
  }

  @Test
  fun testGetCheckRunStatus_allSuccessRuns_returnsPassing() {
    server.enqueue(
      checkRunsPage(
        totalCount = 2,
        runs = listOf(
          checkRunJson(status = "completed", conclusion = "success"),
          checkRunJson(status = "completed", conclusion = "skipped")
        )
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.PASSING)
  }

  @Test
  fun testGetCheckRunStatus_neutralAndSkipped_returnsPassing() {
    server.enqueue(
      checkRunsPage(
        totalCount = 2,
        runs = listOf(
          checkRunJson(status = "completed", conclusion = "neutral"),
          checkRunJson(status = "completed", conclusion = "skipped")
        )
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.PASSING)
  }

  @Test
  fun testGetCheckRunStatus_anyRunFailed_returnsFailing() {
    server.enqueue(
      checkRunsPage(
        totalCount = 2,
        runs = listOf(
          checkRunJson(status = "completed", conclusion = "success"),
          checkRunJson(status = "completed", conclusion = "failure")
        )
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.FAILING)
  }

  @Test
  fun testGetCheckRunStatus_anyRunTimedOut_returnsFailing() {
    server.enqueue(
      checkRunsPage(
        totalCount = 1,
        runs = listOf(checkRunJson(status = "completed", conclusion = "timed_out"))
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.FAILING)
  }

  @Test
  fun testGetCheckRunStatus_anyRunCancelled_returnsFailing() {
    server.enqueue(
      checkRunsPage(
        totalCount = 1,
        runs = listOf(checkRunJson(status = "completed", conclusion = "cancelled"))
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.FAILING)
  }

  @Test
  fun testGetCheckRunStatus_anyRunInProgress_returnsPending() {
    server.enqueue(
      checkRunsPage(
        totalCount = 2,
        runs = listOf(
          checkRunJson(status = "completed", conclusion = "success"),
          checkRunJson(status = "in_progress", conclusion = null)
        )
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.PENDING)
  }

  @Test
  fun testGetCheckRunStatus_anyRunQueued_returnsPending() {
    server.enqueue(
      checkRunsPage(
        totalCount = 1,
        runs = listOf(checkRunJson(status = "queued", conclusion = null))
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.PENDING)
  }

  @Test
  fun testGetCheckRunStatus_pendingAndFailing_returnsPendingNotFailing() {
    // Per deriveStatus(): pending is checked before failure, so a mix of in-progress +
    // failed runs produces PENDING, not FAILING. This ensures we don't prematurely conclude
    // that a commit is failing while CI is still running.
    server.enqueue(
      checkRunsPage(
        totalCount = 2,
        runs = listOf(
          checkRunJson(status = "in_progress", conclusion = null),
          checkRunJson(status = "completed", conclusion = "failure")
        )
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.PENDING)
  }

  @Test
  fun testGetCheckRunStatus_paginatesAcrossMultiplePages() {
    // Page 1: exactly 100 runs (all success), totalCount = 101 → must fetch page 2.
    server.enqueue(
      checkRunsPage(
        totalCount = 101,
        runs = List(100) { checkRunJson(status = "completed", conclusion = "success") }
      )
    )
    // Page 2: one failing run. Because it's on a later page it would be missed without
    // pagination — this test confirms the client collects all pages.
    server.enqueue(
      checkRunsPage(
        totalCount = 101,
        runs = listOf(checkRunJson(status = "completed", conclusion = "failure"))
      )
    )

    assertThat(client.getCheckRunStatus("abc123")).isEqualTo(GitHubCiClient.CiStatus.FAILING)
  }

  @Test
  fun testGetCheckRunStatus_errorResponse_throwsIllegalStateExceptionWithCommitSha() {
    server.enqueue(MockResponse().setResponseCode(404).setBody("""{"message":"Not Found"}"""))

    val exception = assertThrows<IllegalStateException> {
      client.getCheckRunStatus("deadbeef")
    }

    assertThat(exception).hasMessageThat().contains("deadbeef")
    assertThat(exception).hasMessageThat().contains("404")
  }

  /** Produces the JSON string for a single check-run entry. */
  private fun checkRunJson(status: String, conclusion: String?): String {
    val conclusionField = if (conclusion != null) "\"$conclusion\"" else "null"
    return """{"id":1,"name":"test-check","status":"$status","conclusion":$conclusionField}"""
  }

  /** Wraps a list of check-run JSON strings in a full check-runs page response. */
  private fun checkRunsPage(totalCount: Int, runs: List<String>): MockResponse {
    val body =
      """{"total_count":$totalCount,"check_runs":[${runs.joinToString(",")}]}"""
    return MockResponse().setResponseCode(200).setBody(body)
  }
}
