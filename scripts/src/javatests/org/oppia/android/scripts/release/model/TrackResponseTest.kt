package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test

/**
 * Tests for [TrackResponse] and [TrackResponse.ReleaseEntry] JSON serialization and
 * deserialization.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TrackResponseTest {
  private val moshi = Moshi.Builder().build()
  private val adapter = moshi.adapter(TrackResponse::class.java)

  // ---------------------------------------------------------------------------
  // TrackResponse
  // ---------------------------------------------------------------------------

  @Test
  fun testTrackResponse_fromJson_withReleases_parsesReleasesArray() {
    val response = adapter.fromJson(
      """{"releases":[{"versionCodes":["300"],"status":"completed"}]}"""
    )

    assertThat(response!!.releases).hasSize(1)
    assertThat(response.releases!!.first().status).isEqualTo("completed")
  }

  @Test
  fun testTrackResponse_fromJson_withNullReleases_parsesAsNull() {
    val response = adapter.fromJson("""{"releases":null}""")

    assertThat(response!!.releases).isNull()
  }

  @Test
  fun testTrackResponse_fromJson_withExtraFields_ignoresUnknownFields() {
    val response = adapter.fromJson(
      """{"releases":[],"trackId":"alpha","unknown":"value"}"""
    )

    assertThat(response!!.releases).isEmpty()
  }

  @Test
  fun testTrackResponse_toJson_withEmptyReleases_serialisesReleasesKey() {
    val json = adapter.toJson(TrackResponse(releases = emptyList()))

    assertThat(json).contains("\"releases\"")
  }

  // ---------------------------------------------------------------------------
  // ReleaseEntry
  // ---------------------------------------------------------------------------

  @Test
  fun testReleaseEntry_fromJson_withVersionCodesAndStatus_parsesCorrectly() {
    val response = adapter.fromJson(
      """{"releases":[{"versionCodes":["300","301"],"status":"inProgress"}]}"""
    )

    val entry = response!!.releases!!.first()
    assertThat(entry.versionCodes).containsExactly("300", "301")
    assertThat(entry.status).isEqualTo("inProgress")
    assertThat(entry.userFraction).isNull()
  }

  @Test
  fun testReleaseEntry_fromJson_withUserFraction_parsesUserFraction() {
    val response = adapter.fromJson(
      """{"releases":[{"versionCodes":["300"],"status":"inProgress","userFraction":0.25}]}"""
    )

    assertThat(response!!.releases!!.first().userFraction).isEqualTo(0.25)
  }

  @Test
  fun testReleaseEntry_fromJson_withNullVersionCodes_parsesAsNull() {
    val response = adapter.fromJson(
      """{"releases":[{"status":"statusUnspecified","versionCodes":null}]}"""
    )

    assertThat(response!!.releases!!.first().versionCodes).isNull()
  }
}
