package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [findAlphaCandidate]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class FindAlphaCandidateTest {
  private lateinit var fakeClient: FakeGitHubCiClient

  @Before
  fun setUp() {
    fakeClient = FakeGitHubCiClient()
  }

  // ---------------------------------------------------------------------------
  // Happy-path tests
  // ---------------------------------------------------------------------------

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

  // ---------------------------------------------------------------------------
  // No-candidate cases
  // ---------------------------------------------------------------------------

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

  // ---------------------------------------------------------------------------
  // commitLimit tests
  // ---------------------------------------------------------------------------

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
}
