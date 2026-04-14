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
  fun testComputeChecksToDisableInFullRun_returnsNonEmptySet() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    assertThat(disabled).isNotEmpty()
  }

  @Test
  fun testComputeChecksToDisableInFullRun_containsGradleChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    assertThat(disabled).contains("GradleCompatible")
    assertThat(disabled).contains("GradleDependency")
    assertThat(disabled).contains("AndroidGradlePluginVersion")
  }

  @Test
  fun testComputeChecksToDisableInFullRun_doesNotContainSourceChecks() {
    val disabled = LintCheckCatalog.computeChecksToDisableInFullRun()

    assertThat(disabled).doesNotContain("NewApi")
    assertThat(disabled).doesNotContain("HardcodedText")
    assertThat(disabled).doesNotContain("CheckResult")
    // UnusedAttribute must run in full mode — it needs all sources to avoid false positives.
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

    assertThat(disabled).contains("CutPasteId")
    assertThat(disabled).contains("DuplicateIncludedIds")
    assertThat(disabled).contains("SwitchIntDef")
    // UnusedAttribute requires full sources to avoid false positives.
    assertThat(disabled).contains("UnusedAttribute")
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
}
