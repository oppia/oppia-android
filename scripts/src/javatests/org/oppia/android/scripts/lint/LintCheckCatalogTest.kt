package org.oppia.android.scripts.lint

import com.android.tools.lint.checks.BuiltinIssueRegistry
import com.android.tools.lint.client.api.LintClient
import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [LintCheckCatalog]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class LintCheckCatalogTest {

  @Test
  fun testAllKnownChecks_isNotEmpty() {
    assertThat(LintCheckCatalog.allKnownChecks).isNotEmpty()
  }

  @Test
  fun testComputeChecksToDisableInFullRun_containsAlwaysSuppressedChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    // Full mode disables the project-specific always-suppressed checks.
    assertThat(disabled).contains("MissingTranslation")
    assertThat(disabled).contains("SyntheticAccessor")
    assertThat(disabled).contains("DuplicateStrings")
  }

  @Test
  fun testComputeChecksToDisableInFullRun_doesNotDisableGradleChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    // Full mode does NOT disable Gradle-specific checks (they simply produce no findings
    // since there are no .gradle files in this Bazel project).
    assertThat(disabled).doesNotContain("GradleCompatible")
    assertThat(disabled).doesNotContain("GradleDependency")
    assertThat(disabled).doesNotContain("AndroidGradlePluginVersion")
  }

  @Test
  fun testComputeChecksToDisableInFullRun_doesNotContainSourceChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    assertThat(disabled).doesNotContain("NewApi")
    assertThat(disabled).doesNotContain("HardcodedText")
    assertThat(disabled).doesNotContain("CheckResult")
  }

  @Test
  fun testComputeChecksToDisableInIncrementalRun_containsGradleChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInIncrementalRun()

    assertThat(disabled).contains("GradleCompatible")
    assertThat(disabled).contains("GradleDependency")
  }

  @Test
  fun testComputeChecksToDisableInIncrementalRun_containsProjectScopedChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInIncrementalRun()

    assertThat(disabled).contains("SwitchIntDef")
    assertThat(disabled).contains("Registered")
    assertThat(disabled).contains("UnusedResources")
    // These checks need both class hierarchy (source) and manifest data. A manifest-only
    // change in incremental mode would not include the source file, producing false results.
    assertThat(disabled).contains("ExportedPreferenceActivity")
    assertThat(disabled).contains("JobSchedulerService")
    assertThat(disabled).contains("MissingIntentFilterForMediaSearch")
    assertThat(disabled).contains("MissingMediaBrowserServiceIntentFilter")
    assertThat(disabled).contains("MissingOnPlayFromSearch")
    // CutPasteId is a single-file source check — it belongs in incremental, not full project.
    assertThat(disabled).doesNotContain("CutPasteId")
    // DuplicateIncludedIds is a pure XML check — no sources needed, not full project.
    assertThat(disabled).doesNotContain("DuplicateIncludedIds")
  }

  @Test
  fun testComputeChecksToDisableInIncrementalRun_containsAlwaysSuppressedChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInIncrementalRun()

    assertThat(disabled).contains("MissingTranslation")
    assertThat(disabled).contains("SyntheticAccessor")
  }

  @Test
  fun testComputeChecksToDisableInIncrementalRun_doesNotContainIncrementalSourceChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInIncrementalRun()

    assertThat(disabled).doesNotContain("NewApi")
    assertThat(disabled).doesNotContain("CheckResult")
    assertThat(disabled).doesNotContain("MissingSuperCall")
  }

  @Test
  fun testComputeChecksToDisableInIncrementalRun_isSupersetOfFullRunDisabledChecks() {
    val fullDisabled = LintCheckCatalog.computeChecksToDisableInFullRun()
    val incrementalDisabled = LintCheckCatalog.computeChecksToDisableInIncrementalRun()

    assertThat(incrementalDisabled).containsAtLeastElementsIn(fullDisabled)
  }

  @Test
  fun testComputeChecksToDisableInIncrementalRun_hasMoreChecksThanFullRun() {
    val fullDisabled = LintCheckCatalog.computeChecksToDisableInFullRun()
    val incrementalDisabled = LintCheckCatalog.computeChecksToDisableInIncrementalRun()

    assertThat(incrementalDisabled.size).isGreaterThan(fullDisabled.size)
  }

  @Test
  fun testAllKnownChecks_containsExpectedSourceChecks() {
    assertThat(LintCheckCatalog.allKnownChecks).contains("NewApi")
    assertThat(LintCheckCatalog.allKnownChecks).contains("CheckResult")
    assertThat(LintCheckCatalog.allKnownChecks).contains("MissingSuperCall")
  }

  @Test
  fun testAllKnownChecks_containsExpectedNonSourceChecks() {
    assertThat(LintCheckCatalog.allKnownChecks).contains("HardcodedText")
    assertThat(LintCheckCatalog.allKnownChecks).contains("ContentDescription")
    assertThat(LintCheckCatalog.allKnownChecks).contains("IconDensities")
  }

  @Test
  fun testAllKnownChecks_containsExpectedGradleChecks() {
    assertThat(LintCheckCatalog.allKnownChecks).contains("GradleCompatible")
    assertThat(LintCheckCatalog.allKnownChecks).contains("GradleDependency")
    assertThat(LintCheckCatalog.allKnownChecks).contains("AndroidGradlePluginVersion")
  }

  @Test
  fun testRegistryChecks_isNotEmpty() {
    assertThat(loadRegistryChecks()).isNotEmpty()
  }

  @Test
  fun testAllKnownChecks_matchesRegistryChecks() {
    val catalogChecks = LintCheckCatalog.allKnownChecks
    val registryChecks = loadRegistryChecks()

    val missingFromCatalog = registryChecks - catalogChecks
    val extraInCatalog = catalogChecks - registryChecks

    assertThat(missingFromCatalog).isEmpty()
    assertThat(extraInCatalog).isEmpty()
  }

  /**
   * Loads the complete set of check IDs from [BuiltinIssueRegistry] at runtime.
   *
   * [LintClient.clientName] must be initialized before [BuiltinIssueRegistry] can be
   * instantiated (some detectors check [LintClient.isStudio] during static init).
   * Always setting it to [LintClient.CLIENT_CLI] is safe and idempotent.
   */
  private fun loadRegistryChecks(): Set<String> {
    LintClient.clientName = LintClient.CLIENT_CLI
    return BuiltinIssueRegistry().issues.map { it.id }.toSet()
  }
}
