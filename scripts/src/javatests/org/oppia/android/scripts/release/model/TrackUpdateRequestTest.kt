package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import org.junit.Test

/**
 * Tests for [TrackUpdateRequest], [TrackUpdateRequest.ReleaseEntry], and
 * [TrackUpdateRequest.LocalizedText] JSON serialization.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TrackUpdateRequestTest {
  private val moshi = Moshi.Builder().build()
  private val adapter = moshi.adapter(TrackUpdateRequest::class.java)

  // ---------------------------------------------------------------------------
  // TrackUpdateRequest
  // ---------------------------------------------------------------------------

  @Test
  fun testTrackUpdateRequest_toJson_withTrackAndReleases_serialisesTrackField() {
    val request = TrackUpdateRequest(track = "alpha", releases = emptyList())

    val json = adapter.toJson(request)

    assertThat(json).contains("\"track\"")
    assertThat(json).contains("\"alpha\"")
  }

  @Test
  fun testTrackUpdateRequest_toJson_withReleases_serialisesReleasesField() {
    val request = TrackUpdateRequest(track = "alpha", releases = emptyList())

    val json = adapter.toJson(request)

    assertThat(json).contains("\"releases\"")
  }

  // ---------------------------------------------------------------------------
  // ReleaseEntry
  // ---------------------------------------------------------------------------

  @Test
  fun testReleaseEntry_toJson_withVersionCodeAndStatus_serialisesCorrectly() {
    val request = TrackUpdateRequest(
      track = "beta",
      releases = listOf(
        TrackUpdateRequest.ReleaseEntry(
          versionCodes = listOf("301"),
          status = "completed",
          releaseNotes = emptyList()
        )
      )
    )

    val json = adapter.toJson(request)

    assertThat(json).contains("\"versionCodes\"")
    assertThat(json).contains("\"301\"")
    assertThat(json).contains("\"status\"")
    assertThat(json).contains("\"completed\"")
  }

  @Test
  fun testReleaseEntry_toJson_withUserFraction_serialisesUserFraction() {
    val request = TrackUpdateRequest(
      track = "alpha",
      releases = listOf(
        TrackUpdateRequest.ReleaseEntry(
          versionCodes = listOf("301"),
          status = "inProgress",
          releaseNotes = emptyList(),
          userFraction = 0.25
        )
      )
    )

    val json = adapter.toJson(request)

    assertThat(json).contains("\"userFraction\"")
    assertThat(json).contains("0.25")
  }

  // ---------------------------------------------------------------------------
  // LocalizedText
  // ---------------------------------------------------------------------------

  @Test
  fun testLocalizedText_toJson_withLanguageAndText_serialisesCorrectly() {
    val request = TrackUpdateRequest(
      track = "alpha",
      releases = listOf(
        TrackUpdateRequest.ReleaseEntry(
          versionCodes = listOf("301"),
          status = "completed",
          releaseNotes = listOf(
            TrackUpdateRequest.LocalizedText(language = "en-US", text = "Bug fixes.")
          )
        )
      )
    )

    val json = adapter.toJson(request)

    assertThat(json).contains("\"language\"")
    assertThat(json).contains("\"en-US\"")
    assertThat(json).contains("\"text\"")
    assertThat(json).contains("Bug fixes.")
  }
}
