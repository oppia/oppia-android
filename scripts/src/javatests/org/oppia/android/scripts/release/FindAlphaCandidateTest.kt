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

  // region findAlphaCandidate — basic candidate selection (no sinceSha)

  @Test
  fun testFindAlphaCandidate_firstCommitPassing_returnsFound() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.Found("sha-a"))
  }

  @Test
  fun testFindAlphaCandidate_singlePassingCommit_returnsFound() {
    fakeClient.setCommits("sha-only")
    fakeClient.setStatus("sha-only", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.Found("sha-only"))
  }

  @Test
  fun testFindAlphaCandidate_firstFailingSecondPassing_returnsFoundWithSecond() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.Found("sha-b"))
  }

  @Test
  fun testFindAlphaCandidate_firstPendingSecondPassing_returnsFoundWithSecond() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PENDING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.Found("sha-b"))
  }

  @Test
  fun testFindAlphaCandidate_firstCommitHasNoChecks_throwsIllegalStateException() {
    fakeClient.setCommits("sha-a", "sha-b")
    // sha-a defaults to NO_CHECKS (no explicit setStatus call).
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PASSING)

    // NO_CHECKS on any commit is unexpected in oppia-android (every push triggers CI) and
    // should throw immediately rather than silently skipping to sha-b.
    val exception = assertThrows<IllegalStateException> {
      findAlphaCandidate(fakeClient, "develop")
    }
    assertThat(exception).hasMessageThat().contains("sha-a")
    assertThat(exception).hasMessageThat().contains("no CI check runs")
  }

  @Test
  fun testFindAlphaCandidate_mixedStatuses_returnsFoundWithFirstPassing() {
    fakeClient.setCommits("sha-a", "sha-b", "sha-c", "sha-d")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PENDING)
    fakeClient.setStatus("sha-c", GitHubCiClient.CiStatus.PASSING)
    fakeClient.setStatus("sha-d", GitHubCiClient.CiStatus.PASSING)

    // sha-c is the first passing commit; sha-d should never be reached.
    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.Found("sha-c"))
  }

  @Test
  fun testFindAlphaCandidate_emptyCommitList_returnsNoPassingCommit() {
    // No commits set — fakeClient returns an empty list.

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.NoPassingCommit(commitsChecked = 0))
  }

  @Test
  fun testFindAlphaCandidate_allFailing_returnsNoPassingCommit() {
    fakeClient.setCommits("sha-a", "sha-b", "sha-c")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-c", GitHubCiClient.CiStatus.FAILING)

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.NoPassingCommit(commitsChecked = 3))
  }

  @Test
  fun testFindAlphaCandidate_allPending_returnsNoPassingCommit() {
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PENDING)
    fakeClient.setStatus("sha-b", GitHubCiClient.CiStatus.PENDING)

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.NoPassingCommit(commitsChecked = 2))
  }

  @Test
  fun testFindAlphaCandidate_commitWithNoChecks_throwsIllegalStateException() {
    fakeClient.setCommits("sha-a", "sha-b")
    // Both default to NO_CHECKS. The script should throw on the very first one
    // rather than treating it as a non-passing status and returning NoPassingCommit.

    val exception = assertThrows<IllegalStateException> {
      findAlphaCandidate(fakeClient, "develop")
    }
    assertThat(exception).hasMessageThat().contains("sha-a")
    assertThat(exception).hasMessageThat().contains("no CI check runs")
  }

  @Test
  fun testFindAlphaCandidate_passingCommitOnSecondPage_returnsFound() {
    // First 100 commits all FAILING, commit #101 PASSING — requires pagination to find.
    val shas = (1..101).map { "sha-$it" }
    fakeClient.setCommits(*shas.toTypedArray())
    shas.take(100).forEach { fakeClient.setStatus(it, GitHubCiClient.CiStatus.FAILING) }
    fakeClient.setStatus("sha-101", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop"))
      .isEqualTo(AlphaCandidateResult.Found("sha-101"))
  }

  @Test
  fun testFindAlphaCandidate_sinceShaOnSecondPage_returnsNoPassingCommit() {
    // 101 commits newest-first; sinceSha = sha-101 (oldest) is on page 2.
    // sha-1..sha-100 are all FAILING; after paginating and finding sha-101 on page 2 the
    // script should stop and return NoPassingCommit with commitsChecked = 100.
    val shas = (1..101).map { "sha-$it" }
    fakeClient.setCommits(*shas.toTypedArray())
    shas.take(100).forEach { fakeClient.setStatus(it, GitHubCiClient.CiStatus.FAILING) }

    assertThat(findAlphaCandidate(fakeClient, "develop", sinceSha = "sha-101"))
      .isEqualTo(AlphaCandidateResult.NoPassingCommit(commitsChecked = 100))
  }

  // endregion

  // region findAlphaCandidate — sinceSha behaviour

  @Test
  fun testFindAlphaCandidate_withSinceSha_noNewCommits_returnsNoNewCommits() {
    // latest-alpha points to "sha-a", which is also the most recent commit — nothing new.
    fakeClient.setCommits("sha-a")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop", sinceSha = "sha-a"))
      .isEqualTo(AlphaCandidateResult.NoNewCommits)
  }

  @Test
  fun testFindAlphaCandidate_withSinceSha_newCommitPasses_returnsFound() {
    // sha-new is newer than sha-old (latest-alpha); sha-new passes CI.
    fakeClient.setCommits("sha-new", "sha-old")
    fakeClient.setStatus("sha-new", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop", sinceSha = "sha-old"))
      .isEqualTo(AlphaCandidateResult.Found("sha-new"))
  }

  @Test
  fun testFindAlphaCandidate_withSinceSha_newCommitsAllFailing_returnsNoPassingCommit() {
    // sha-new1 and sha-new2 are newer than sha-old; both fail CI.
    fakeClient.setCommits("sha-new1", "sha-new2", "sha-old")
    fakeClient.setStatus("sha-new1", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-new2", GitHubCiClient.CiStatus.FAILING)
    fakeClient.setStatus("sha-old", GitHubCiClient.CiStatus.PASSING)

    // sha-old is NOT inspected (it's the reference point itself); only sha-new1 and sha-new2 are.
    assertThat(findAlphaCandidate(fakeClient, "develop", sinceSha = "sha-old"))
      .isEqualTo(AlphaCandidateResult.NoPassingCommit(commitsChecked = 2))
  }

  @Test
  fun testFindAlphaCandidate_withSinceSha_notInCommitList_treatsAllCommitsAsNew() {
    // If sinceSha isn't in the fetched page (e.g. very old tag), all fetched commits are inspected.
    fakeClient.setCommits("sha-a", "sha-b")
    fakeClient.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)

    assertThat(findAlphaCandidate(fakeClient, "develop", sinceSha = "sha-ancient"))
      .isEqualTo(AlphaCandidateResult.Found("sha-a"))
  }

  @Test
  fun testFindAlphaCandidate_withSinceSha_firstNewPasses_doesNotCheckOlderCommits() {
    // sha-new passes; sha-old is the reference and sha-older is before the reference.
    fakeClient.setCommits("sha-new", "sha-old", "sha-older")
    fakeClient.setStatus("sha-new", GitHubCiClient.CiStatus.PASSING)
    fakeClient.setStatus("sha-old", GitHubCiClient.CiStatus.PASSING)
    fakeClient.setStatus("sha-older", GitHubCiClient.CiStatus.PASSING)

    // Only sha-new is newer than sha-old; sha-old and sha-older must not be returned.
    assertThat(findAlphaCandidate(fakeClient, "develop", sinceSha = "sha-old"))
      .isEqualTo(AlphaCandidateResult.Found("sha-new"))
  }

  // endregion

  // region main()

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

      // args: github_token, branch, latest_alpha_sha ("" = none), override_api_base_url
      main(arrayOf("fake-token", "develop", "", serverUrl))

      // Verify both requests were made: one for commits, one for check-runs.
      assertThat(server.requestCount).isEqualTo(2)
    } finally {
      server.shutdown()
    }
  }

  @Test
  fun testMain_withCustomBranch_forwardsBranchNameToApi() {
    val server = MockWebServer()
    server.start()
    try {
      val sha = "aabbccddeeff00112233445566778899aabbccdd"
      // Enqueue a passing candidate so main() exits normally (no System.exit call).
      server.enqueue(
        MockResponse().setResponseCode(200).setBody(
          """[{"sha":"$sha","commit":{"message":"fix: something"}}]"""
        )
      )
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

      main(arrayOf("fake-token", "my-branch", "", serverUrl))

      // The first request (listCommits) must target the custom branch.
      val recordedRequest = server.takeRequest()
      assertThat(recordedRequest.path).contains("my-branch")
    } finally {
      server.shutdown()
    }
  }

  // endregion
}
