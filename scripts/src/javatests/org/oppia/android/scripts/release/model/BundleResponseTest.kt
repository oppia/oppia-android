package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [BundleResponse]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BundleResponseTest {

  @Test
  fun testBundleResponse_constructor_setsVersionCode() {
    val response = BundleResponse(versionCode = 301L)

    assertThat(response.versionCode).isEqualTo(301L)
  }

  @Test
  fun testBundleResponse_equality_sameVersionCode_isEqual() {
    val a = BundleResponse(versionCode = 100L)
    val b = BundleResponse(versionCode = 100L)

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testBundleResponse_equality_differentVersionCode_isNotEqual() {
    val a = BundleResponse(versionCode = 100L)
    val b = BundleResponse(versionCode = 200L)

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testBundleResponse_copy_updatesVersionCode() {
    val original = BundleResponse(versionCode = 100L)
    val copy = original.copy(versionCode = 200L)

    assertThat(copy.versionCode).isEqualTo(200L)
    assertThat(original.versionCode).isEqualTo(100L)
  }

  @Test
  fun testBundleResponse_hashCode_equalObjects_haveSameHashCode() {
    val a = BundleResponse(versionCode = 42L)
    val b = BundleResponse(versionCode = 42L)

    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }
}
