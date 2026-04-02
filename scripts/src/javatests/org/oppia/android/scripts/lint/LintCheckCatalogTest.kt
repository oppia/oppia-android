package org.oppia.android.scripts.lint

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [LintCheckCatalog]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class LintCheckCatalogTest {

  @Test
  fun testAllKnownChecks_equalsUnionOfAllBuckets() {
    val expectedUnion = LintCheckCatalog.gradleChecksToIgnore +
      LintCheckCatalog.checksNotNeedingSources +
      LintCheckCatalog.checksForIncrementalSources +
      LintCheckCatalog.checksRequiringFullProject

    assertThat(LintCheckCatalog.allKnownChecks).isEqualTo(expectedUnion)
  }

  @Test
  fun testBuckets_areNonOverlapping() {
    val buckets = listOf(
      LintCheckCatalog.gradleChecksToIgnore,
      LintCheckCatalog.checksNotNeedingSources,
      LintCheckCatalog.checksForIncrementalSources,
      LintCheckCatalog.checksRequiringFullProject
    )

    for (i in buckets.indices) {
      for (j in i + 1 until buckets.size) {
        val intersection = buckets[i].intersect(buckets[j])
        assertThat(intersection).isEmpty()
      }
    }
  }

  @Test
  fun testGetChecksToDisable_fastMode_disablesGradleAndProjectScoped() {
    val disabled = LintCheckCatalog.getChecksToDisable("fast")

    assertThat(disabled).containsAtLeastElementsIn(LintCheckCatalog.gradleChecksToIgnore)
    assertThat(disabled).containsAtLeastElementsIn(LintCheckCatalog.checksRequiringFullProject)
    assertThat(disabled).containsNoneIn(LintCheckCatalog.checksNotNeedingSources)
    assertThat(disabled).containsNoneIn(LintCheckCatalog.checksForIncrementalSources)
  }

  @Test
  fun testGetChecksToDisable_fullMode_disablesOnlyGradle() {
    val disabled = LintCheckCatalog.getChecksToDisable("full")

    assertThat(disabled).isEqualTo(LintCheckCatalog.gradleChecksToIgnore)
  }

  @Test
  fun testGetChecksToDisable_listChecksMode_returnsEmpty() {
    val disabled = LintCheckCatalog.getChecksToDisable("list-checks")

    assertThat(disabled).isEmpty()
  }

  @Test
  fun testUseIncrementalSources_fastMode_returnsTrue() {
    assertThat(LintCheckCatalog.useIncrementalSources("fast")).isTrue()
  }

  @Test
  fun testUseIncrementalSources_fullMode_returnsFalse() {
    assertThat(LintCheckCatalog.useIncrementalSources("full")).isFalse()
  }

  @Test
  fun testReportUnusedEnum_fullMode_returnsTrue() {
    assertThat(LintCheckCatalog.reportUnusedEnum("full")).isTrue()
  }

  @Test
  fun testReportUnusedEnum_fastMode_returnsFalse() {
    assertThat(LintCheckCatalog.reportUnusedEnum("fast")).isFalse()
  }

  @Test
  fun testAllKnownChecks_isNotEmpty() {
    assertThat(LintCheckCatalog.allKnownChecks).isNotEmpty()
  }

  @Test
  fun testGradleChecksToIgnore_containsExpectedChecks() {
    assertThat(LintCheckCatalog.gradleChecksToIgnore).contains("GradleCompatible")
    assertThat(LintCheckCatalog.gradleChecksToIgnore).contains("GradleDependency")
    assertThat(LintCheckCatalog.gradleChecksToIgnore).contains("AndroidGradlePluginVersion")
  }

  @Test
  fun testChecksNotNeedingSources_containsExpectedChecks() {
    assertThat(LintCheckCatalog.checksNotNeedingSources).contains("HardcodedText")
    assertThat(LintCheckCatalog.checksNotNeedingSources).contains("ContentDescription")
    assertThat(LintCheckCatalog.checksNotNeedingSources).contains("IconDensities")
  }

  @Test
  fun testChecksForIncrementalSources_containsExpectedChecks() {
    assertThat(LintCheckCatalog.checksForIncrementalSources).contains("NewApi")
    assertThat(LintCheckCatalog.checksForIncrementalSources).contains("CheckResult")
    assertThat(LintCheckCatalog.checksForIncrementalSources).contains("MissingSuperCall")
  }
}
