package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.scripts.release.ChangelogPushResolver.ChangedFile
import org.oppia.android.scripts.release.ChangelogPushResolver.ResolutionResult

/**
 * Tests for [ChangelogPushResolver].
 *
 * Covers all push eligibility rules for automatic changelog deployment including new changelog
 * guards, release freeze commits, flavor grouping, and multi-version rejection.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class ChangelogPushResolverTest {

  private lateinit var resolver: ChangelogPushResolver

  @Before
  fun setUp() {
    resolver = ChangelogPushResolver()
  }

  @Test
  fun testAddedChangelog_skipsDeployment() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.19.md", status = "added")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Skipped::class.java)
    val skipped = result as ResolutionResult.Skipped
    assertThat(skipped.reason).contains("No eligible changelog files found")
  }

  @Test
  fun testModifiedChangelog_schedulesDeployment() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.18.md", status = "modified")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Scheduled::class.java)
    val scheduled = result as ResolutionResult.Scheduled
    assertThat(scheduled.version).isEqualTo("0.18")
    assertThat(scheduled.files).containsExactly("config/changelogs/0.18.md")
  }

  @Test
  fun testDeletedChangelog_doesNotDeploy() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.18.md", status = "removed")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Skipped::class.java)
  }

  @Test
  fun testVersionBumpAndChangelogChange_skipsWithExplanation() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.18.md", status = "modified"),
      ChangedFile(path = "version.bzl", status = "modified")
    )

    val result = resolver.resolve(versionBzlChanged = true, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Skipped::class.java)
    val skipped = result as ResolutionResult.Skipped
    assertThat(skipped.reason).contains("version.bzl was changed in this push")
    assertThat(skipped.reason).contains("release-freeze")
  }

  @Test
  fun testMultipleFilesForSameVersion_resolvesSingleVersion() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.19.md", status = "modified"),
      ChangedFile(path = "config/changelogs/0.19_alpha.md", status = "modified")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Scheduled::class.java)
    val scheduled = result as ResolutionResult.Scheduled
    assertThat(scheduled.version).isEqualTo("0.19")
    assertThat(scheduled.files).containsExactly(
      "config/changelogs/0.19.md",
      "config/changelogs/0.19_alpha.md"
    )
  }

  @Test
  fun testMultipleVersionsChanged_failsWithClearMessage() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.18.md", status = "modified"),
      ChangedFile(path = "config/changelogs/0.19.md", status = "modified")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Failed::class.java)
    val failed = result as ResolutionResult.Failed
    assertThat(failed.errorMessage).contains("Multiple distinct changelog versions detected")
    assertThat(failed.errorMessage).contains("0.18")
    assertThat(failed.errorMessage).contains("0.19")
    assertThat(failed.errorMessage).contains("workflow_dispatch")
  }

  @Test
  fun testRenamedChangelog_doesNotDeploy() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.18.md", status = "renamed")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Skipped::class.java)
  }

  @Test
  fun testAddedFlavorChangelog_forExistingVersion_schedulesDeployment() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.18_alpha.md", status = "added")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Scheduled::class.java)
    val scheduled = result as ResolutionResult.Scheduled
    assertThat(scheduled.version).isEqualTo("0.18")
    assertThat(scheduled.files).containsExactly("config/changelogs/0.18_alpha.md")
  }

  @Test
  fun testAddedFlavorChangelog_whenBaseIsAlsoAdded_skipsDeployment() {
    val changedFiles = listOf(
      ChangedFile(path = "config/changelogs/0.19.md", status = "added"),
      ChangedFile(path = "config/changelogs/0.19_alpha.md", status = "added")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Skipped::class.java)
  }

  @Test
  fun testNonChangelogFilesOnly_skipsDeployment() {
    val changedFiles = listOf(
      ChangedFile(path = "app/src/main/AndroidManifest.xml", status = "modified"),
      ChangedFile(path = "README.md", status = "modified")
    )

    val result = resolver.resolve(versionBzlChanged = false, changedFiles = changedFiles)

    assertThat(result).isInstanceOf(ResolutionResult.Skipped::class.java)
    val skipped = result as ResolutionResult.Skipped
    assertThat(skipped.reason).contains("No changelog files changed in this push")
  }

  @Test
  fun testEmptyChanges_skipsDeployment() {
    val result = resolver.resolve(versionBzlChanged = false, changedFiles = emptyList())

    assertThat(result).isInstanceOf(ResolutionResult.Skipped::class.java)
  }
}
