package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [BundleResponse]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BundleResponseTest {

  @Test
  fun testBundleResponse_constructor_setsVersionCode() {
    val response = BundleResponse(versionCode = "301")

    assertThat(response.versionCode).isEqualTo("301")
  }

  @Test
  fun testBundleResponse_equality_sameVersionCode_isEqual() {
    val a = BundleResponse(versionCode = "100")
    val b = BundleResponse(versionCode = "100")

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testBundleResponse_equality_differentVersionCode_isNotEqual() {
    val a = BundleResponse(versionCode = "100")
    val b = BundleResponse(versionCode = "200")

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testBundleResponse_copy_updatesVersionCode() {
    val original = BundleResponse(versionCode = "100")
    val copy = original.copy(versionCode = "200")

    assertThat(copy.versionCode).isEqualTo("200")
    assertThat(original.versionCode).isEqualTo("100")
  }

  @Test
  fun testBundleResponse_hashCode_equalObjects_haveSameHashCode() {
    val a = BundleResponse(versionCode = "42")
    val b = BundleResponse(versionCode = "42")

    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }
}
