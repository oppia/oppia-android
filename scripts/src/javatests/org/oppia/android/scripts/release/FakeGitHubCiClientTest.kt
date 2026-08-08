package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

/** Tests for [FakeGitHubCiClient]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class FakeGitHubCiClientTest {
  private lateinit var fake: FakeGitHubCiClient

  @Before
  fun setUp() {
    fake = FakeGitHubCiClient()
  }

  @Test
  fun testListCommits_noCommitsConfigured_returnsEmptyList() {
    assertThat(fake.listCommits("develop", limit = 10)).isEmpty()
  }

  @Test
  fun testListCommits_commitsConfigured_returnsCommitsInNewestFirstOrder() {
    fake.setCommits("sha-a", "sha-b", "sha-c")

    val commits = fake.listCommits("develop", limit = 100)

    assertThat(commits.map { it.sha }).containsExactly("sha-a", "sha-b", "sha-c").inOrder()
  }

  @Test
  fun testListCommits_limitSmallerThanConfiguredCount_returnsOnlyFirstLimitEntries() {
    fake.setCommits("sha-a", "sha-b", "sha-c")

    val commits = fake.listCommits("develop", limit = 2)

    assertThat(commits.map { it.sha }).containsExactly("sha-a", "sha-b").inOrder()
  }

  @Test
  fun testListCommits_limitLargerThanConfiguredCount_returnsAllConfiguredCommits() {
    fake.setCommits("sha-a", "sha-b")

    val commits = fake.listCommits("develop", limit = 100)

    assertThat(commits).hasSize(2)
  }

  @Test
  fun testListCommits_setCommitsCalledTwice_replacesFirstList() {
    fake.setCommits("sha-a", "sha-b")
    fake.setCommits("sha-c", "sha-d")

    val commits = fake.listCommits("develop", limit = 100)

    assertThat(commits.map { it.sha }).containsExactly("sha-c", "sha-d").inOrder()
  }

  @Test
  fun testListCommits_branchArgumentIgnored_alwaysReturnsConfiguredCommits() {
    fake.setCommits("sha-a")

    val commitsDevelop = fake.listCommits("develop", limit = 10)
    val commitsMain = fake.listCommits("main", limit = 10)

    assertThat(commitsDevelop.map { it.sha }).containsExactly("sha-a")
    assertThat(commitsMain.map { it.sha }).containsExactly("sha-a")
  }

  @Test
  fun testGetCheckRunStatus_noStatusConfigured_returnsNoChecks() {
    assertThat(fake.getCheckRunStatus("unknown-sha"))
      .isEqualTo(GitHubCiClient.CiStatus.NO_CHECKS)
  }

  @Test
  fun testGetCheckRunStatus_passingConfigured_returnsPassing() {
    fake.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)

    assertThat(fake.getCheckRunStatus("sha-a")).isEqualTo(GitHubCiClient.CiStatus.PASSING)
  }

  @Test
  fun testGetCheckRunStatus_failingConfigured_returnsFailing() {
    fake.setStatus("sha-a", GitHubCiClient.CiStatus.FAILING)

    assertThat(fake.getCheckRunStatus("sha-a")).isEqualTo(GitHubCiClient.CiStatus.FAILING)
  }

  @Test
  fun testGetCheckRunStatus_pendingConfigured_returnsPending() {
    fake.setStatus("sha-a", GitHubCiClient.CiStatus.PENDING)

    assertThat(fake.getCheckRunStatus("sha-a")).isEqualTo(GitHubCiClient.CiStatus.PENDING)
  }

  @Test
  fun testGetCheckRunStatus_multipleStatusesConfigured_returnsCorrectStatusForEachSha() {
    fake.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)
    fake.setStatus("sha-b", GitHubCiClient.CiStatus.FAILING)
    fake.setStatus("sha-c", GitHubCiClient.CiStatus.PENDING)

    assertThat(fake.getCheckRunStatus("sha-a")).isEqualTo(GitHubCiClient.CiStatus.PASSING)
    assertThat(fake.getCheckRunStatus("sha-b")).isEqualTo(GitHubCiClient.CiStatus.FAILING)
    assertThat(fake.getCheckRunStatus("sha-c")).isEqualTo(GitHubCiClient.CiStatus.PENDING)
  }

  @Test
  fun testGetCheckRunStatus_statusUpdated_returnsLatestStatus() {
    fake.setStatus("sha-a", GitHubCiClient.CiStatus.PENDING)
    fake.setStatus("sha-a", GitHubCiClient.CiStatus.PASSING)

    assertThat(fake.getCheckRunStatus("sha-a")).isEqualTo(GitHubCiClient.CiStatus.PASSING)
  }
}
