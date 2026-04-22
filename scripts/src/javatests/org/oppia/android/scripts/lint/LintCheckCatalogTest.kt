package org.oppia.android.scripts.lint

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
  fun testComputeChecksToDisableInFullRun_returnsEmptySet() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    // Full mode runs all checks — nothing is disabled. Gradle-specific checks produce no
    // findings since there are no .gradle files in this Bazel project.
    assertThat(disabled).isEmpty()
  }

  @Test
  fun testComputeChecksToDisableInFullRun_doesNotDisableAnyChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    // In full mode, even Gradle checks are allowed to run. They produce no findings
    // because there are no .gradle files in this Bazel project.
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
    // UnusedAttribute is an API-level compatibility check (not a custom-attr usage scanner) and
    // runs on XML only — it does not need full sources and is not disabled in full runs.
    assertThat(disabled).doesNotContain("UnusedAttribute")
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
    // UnusedAttribute is an API-level XML check (ApiDetector) — not a cross-source scanner.
    assertThat(disabled).doesNotContain("UnusedAttribute")
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
    assertThat(LintCheckCatalog.registryChecks).isNotEmpty()
  }

  @Test
  fun testRegistryChecks_containsMoreChecksThanLintList() {
    // BuiltinIssueRegistry includes dynamically-loaded checks that lint --list misses.
    assertThat(LintCheckCatalog.registryChecks.size).isGreaterThan(152)
  }

  @Test
  fun testAllKnownChecks_matchesRegistryChecks() {
    val catalogChecks = LintCheckCatalog.allKnownChecks
    val registryChecks = LintCheckCatalog.registryChecks

    val missingFromCatalog = registryChecks - catalogChecks
    val extraInCatalog = catalogChecks - registryChecks

    assertThat(missingFromCatalog).isEmpty()
    assertThat(extraInCatalog).isEmpty()
  }
}
