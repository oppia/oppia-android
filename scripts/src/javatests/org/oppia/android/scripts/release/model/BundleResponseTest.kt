package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test

/** Tests for [BundleResponse] JSON serialization and deserialization. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class BundleResponseTest {
  private val moshi = Moshi.Builder().build()
  private val adapter = moshi.adapter(BundleResponse::class.java)

  @Test
  fun testBundleResponse_fromJson_withVersionCode_parsesVersionCode() {
    val response = adapter.fromJson("""{"versionCode":"301"}""")

    assertThat(response!!.versionCode).isEqualTo("301")
  }

  @Test
  fun testBundleResponse_fromJson_withExtraFields_ignoresUnknownFields() {
    val response = adapter.fromJson("""{"versionCode":"200","unknown":"ignored"}""")

    assertThat(response!!.versionCode).isEqualTo("200")
  }

  @Test
  fun testBundleResponse_toJson_withVersionCode_serialisesCorrectFieldName() {
    val json = adapter.toJson(BundleResponse(versionCode = "301"))

    assertThat(json).contains("\"versionCode\"")
    assertThat(json).contains("\"301\"")
  }
}
