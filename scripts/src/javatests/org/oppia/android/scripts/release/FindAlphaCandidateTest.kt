package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [findAlphaCandidate] and [main]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class FindAlphaCandidateTest {
  private lateinit var fakeClient: FakeGitHubCiClient

  @Before
  fun setUp() {
    fakeClient = FakeGitHubCiClient()
  }

  @Test
  fun testFindAlphaCandidate_firstCommitPassing_returnsFirstSha() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop")).isEqualTo("sha-a")
  }

  @Test
  fun testFindAlphaCandidate_singlePassingCommit_returnsThatSha() {
    fakeClient.setCommits("sha-only")
    fakeClient.setStatus("sha-only", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop")).isEqualTo("sha-only")
  }

  @Test
  fun testFindAlphaCandidate_firstFailingSecondPassing_returnsSecondSha() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop")).isEqualTo("sha-b")
  }

  @Test
  fun testFindAlphaCandidate_firstPendingSecondPassing_returnsSecondSha() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PENDING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop")).isEqualTo("sha-b")
  }

  @Test
  fun testFindAlphaCandidate_firstNoChecksSecondPassing_returnsSecondSha() {
    fakeClient.setCommits("sha-a", "sha-b")
    // sha-a defaults to NO_CHECKS (no explicit setStatus call).
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop")).isEqualTo("sha-b")
  }

  @Test
  fun testFindAlphaCandidate_mixedStatuses_returnsFirstPassing() {
    fakeClient.setCommits("sha-a", "sha-b", "sha-c", "sha-d")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PENDING)
    fakeClient.setStatus("sha-c", GitHubCiClient.CiStatus.PASSING)
    fakeClient.setStatus("sha-d", GitHubCiClient.CiStatus.PASSING)

    // sha-c is the first passing commit; sha-d should never be reached.
    assertThat(findAlphaCandidate(fakeClient, "develop")).isEqualTo("sha-c")
  }

  @Test
  fun testFindAlphaCandidate_emptyCommitList_returnsNull() {
    // No commits set — fakeClient returns an empty list.

    assertThat(findAlphaCandidate(fakeClient, "develop")).isNull()
  }

  @Test
  fun testFindAlphaCandidate_allFailing_returnsNull() {
    fakeClient.setCommits("sha-a", "sha-b", "sha-c")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-c", GitHubCiClient.CiStatus.FAILING)

    assertThat(findAlphaCandidate(fakeClient, "develop")).isNull()
  }

  @Test
  fun testFindAlphaCandidate_allPending_returnsNull() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PENDING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PENDING)

    assertThat(findAlphaCandidate(fakeClient, "develop")).isNull()
  }

  @Test
  fun testFindAlphaCandidate_allNoChecks_returnsNull() {
    fakeClient.setCommits("sha-a", "sha-b")
    // Both default to NO_CHECKS.

    assertThat(findAlphaCandidate(fakeClient, "develop")).isNull()
  }

  @Test
  fun testFindAlphaCandidate_passingCommitBeyondLimit_returnsNull() {
    fakeClient.setCommits("sha-a", "sha-b", "sha-c")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-c", GitHubCiClient.CiStatus.PASSING)

    // With commitLimit=2, sha-c is not inspected.
    assertThat(findAlphaCandidate(fakeClient, "develop", commitLimit = 2)).isNull()
  }

  @Test
  fun testFindAlphaCandidate_commitLimitOfOne_returnsFirstIfPassing() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop", commitLimit = 1)).isEqualTo("sha-a")
  }

  @Test
  fun testFindAlphaCandidate_commitLimitZero_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      findAlphaCandidate(fakeClient, "develop", commitLimit = 0)
    }

    assertThat(exception).hasMessageThat().contains("commitLimit")
  }

  @Test
  fun testFindAlphaCandidate_commitLimitAbove100_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      findAlphaCandidate(fakeClient, "develop", commitLimit = 101)
    }

    assertThat(exception).hasMessageThat().contains("commitLimit")
  }

  @Test
  fun testMain_emptyArgs_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf())
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_withPassingCandidate_completesSuccessfully() {
    val server = MockWebServer()
    server.start()
    try {
      val sha = "aabbccddeeff00112233445566778899aabbccdd"
      // 1. listCommits → one commit
      server.enqueue(
        MockResponse().setResponseCode(200).setBody(
          """[{"sha":"$sha","commit":{"message":"feat: something"}}]"""
        )
      )
      // 2. listCheckRuns → all passing (single page, fewer than 100 items = last page)
      server.enqueue(
        MockResponse().setResponseCode(200).setBody(
          """{
            "total_count": 1,
            "check_runs": [
              {"id":1,"name":"build","status":"completed","conclusion":"success"}
            ]
          }"""
        )
      )
      val serverUrl = server.url("/").toString()

      // main() prints the SHA to stdout and returns normally when a candidate is found.
      main(arrayOf("fake-token", "develop", "1", serverUrl))

      // Verify both requests were made: one for commits, one for check-runs.
      assertThat(server.requestCount).isEqualTo(2)
    } finally {
      server.shutdown()
    }
  }

  @Test
  fun testMain_withEmptyCommitList_requestsCommitsFromSpecifiedBranch() {
    val server = MockWebServer()
    server.start()
    try {
      // Return an empty commit list so main() reaches System.exit(1).
      // We catch the resulting exception so the test does not fail on the exit call.
      server.enqueue(MockResponse().setResponseCode(200).setBody("[]"))
      val serverUrl = server.url("/").toString()

      try {
        main(arrayOf("fake-token", "my-branch", "1", serverUrl))
      } catch (e: Exception) {
        // System.exit(1) or equivalent — expected when no candidate is found.
      }

      // Verify the request targeted the correct branch.
      val recordedRequest = server.takeRequest()
      assertThat(recordedRequest.path).contains("my-branch")
    } finally {
      server.shutdown()
    }
  }
}
