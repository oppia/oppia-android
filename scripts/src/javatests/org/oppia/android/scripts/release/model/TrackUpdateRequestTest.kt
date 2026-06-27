package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [TrackUpdateRequest] and its nested types. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TrackUpdateRequestTest {

  // ---------------------------------------------------------------------------
  // TrackUpdateRequest
  // ---------------------------------------------------------------------------

  @Test
  fun testTrackUpdateRequest_constructor_setsTrackAndReleases() {
    val request = TrackUpdateRequest(track = "alpha", releases = emptyList())

    assertThat(request.track).isEqualTo("alpha")
    assertThat(request.releases).isEmpty()
  }

  @Test
  fun testTrackUpdateRequest_equality_sameFields_isEqual() {
    val a = TrackUpdateRequest(track = "beta", releases = emptyList())
    val b = TrackUpdateRequest(track = "beta", releases = emptyList())

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testTrackUpdateRequest_equality_differentTrack_isNotEqual() {
    val a = TrackUpdateRequest(track = "alpha", releases = emptyList())
    val b = TrackUpdateRequest(track = "beta", releases = emptyList())

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testTrackUpdateRequest_copy_updatesTrack() {
    val original = TrackUpdateRequest(track = "alpha", releases = emptyList())
    val copy = original.copy(track = "production")

    assertThat(copy.track).isEqualTo("production")
    assertThat(original.track).isEqualTo("alpha")
  }

  // ---------------------------------------------------------------------------
  // ReleaseEntry
  // ---------------------------------------------------------------------------

  @Test
  fun testReleaseEntry_constructor_setsAllFields() {
    val notes = listOf(TrackUpdateRequest.LocalizedText(language = "en-US", text = "Bug fixes."))
    val entry = TrackUpdateRequest.ReleaseEntry(
      versionCodes = listOf("301"),
      status = "completed",
      releaseNotes = notes
    )

    assertThat(entry.versionCodes).containsExactly("301")
    assertThat(entry.status).isEqualTo("completed")
    assertThat(entry.releaseNotes).hasSize(1)
    assertThat(entry.userFraction).isNull()
  }

  @Test
  fun testReleaseEntry_constructor_withUserFraction_setsUserFraction() {
    val entry = TrackUpdateRequest.ReleaseEntry(
      versionCodes = listOf("301"),
      status = "inProgress",
      releaseNotes = emptyList(),
      userFraction = 0.1
    )

    assertThat(entry.userFraction).isEqualTo(0.1)
    assertThat(entry.status).isEqualTo("inProgress")
  }

  @Test
  fun testReleaseEntry_equality_sameFields_isEqual() {
    val a = TrackUpdateRequest.ReleaseEntry(listOf("300"), "completed", emptyList())
    val b = TrackUpdateRequest.ReleaseEntry(listOf("300"), "completed", emptyList())

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testReleaseEntry_equality_differentStatus_isNotEqual() {
    val a = TrackUpdateRequest.ReleaseEntry(listOf("300"), "completed", emptyList())
    val b = TrackUpdateRequest.ReleaseEntry(listOf("300"), "inProgress", emptyList())

    assertThat(a).isNotEqualTo(b)
  }

  // ---------------------------------------------------------------------------
  // LocalizedText
  // ---------------------------------------------------------------------------

  @Test
  fun testLocalizedText_constructor_setsLanguageAndText() {
    val text = TrackUpdateRequest.LocalizedText(language = "en-US", text = "Bug fixes.")

    assertThat(text.language).isEqualTo("en-US")
    assertThat(text.text).isEqualTo("Bug fixes.")
  }

  @Test
  fun testLocalizedText_equality_sameFields_isEqual() {
    val a = TrackUpdateRequest.LocalizedText("en-US", "Bug fixes.")
    val b = TrackUpdateRequest.LocalizedText("en-US", "Bug fixes.")

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testLocalizedText_equality_differentLanguage_isNotEqual() {
    val a = TrackUpdateRequest.LocalizedText("en-US", "Bug fixes.")
    val b = TrackUpdateRequest.LocalizedText("fr-FR", "Bug fixes.")

    assertThat(a).isNotEqualTo(b)
  }
}
