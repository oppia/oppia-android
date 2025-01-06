package org.oppia.android.scripts.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Tests for [AndroidBuildSdkProperties].
 *
 * Note that these tests have a few caveats:
 * 1. They are a bit fragile since they're directly testing the local Bazel configuration. As the
 *   team changes its dependency versions in the future, this suite will need to be updated.
 * 2. There's no way to test the "failing state" since the build graph guarantees that the SDK info
 *   file is included at runtime. The implementation makes this assumption, as well.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class AndroidBuildSdkPropertiesTest {
  @Test
  fun testBuildSdkVersion_isTheCorrectSdkVersion() {
    val properties = AndroidBuildSdkProperties()

    assertThat(properties.buildSdkVersion).isEqualTo(34)
  }

  @Test
  fun testBuildToolsVersion_isTheCorrectVersion() {
    val properties = AndroidBuildSdkProperties()

    assertThat(properties.buildToolsVersion).isEqualTo("32.0.0")
  }
}
