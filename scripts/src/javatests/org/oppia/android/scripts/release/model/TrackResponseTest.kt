package org.oppia.android.scripts.release.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [TrackResponse] and its nested [TrackResponse.ReleaseEntry]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class TrackResponseTest {

  // ---------------------------------------------------------------------------
  // TrackResponse
  // ---------------------------------------------------------------------------

  @Test
  fun testTrackResponse_constructor_withReleases_setsReleases() {
    val entries = listOf(
      TrackResponse.ReleaseEntry(versionCodes = listOf(300L), status = "completed")
    )
    val response = TrackResponse(releases = entries)

    assertThat(response.releases).hasSize(1)
    assertThat(response.releases!!.first().status).isEqualTo("completed")
  }

  @Test
  fun testTrackResponse_constructor_nullReleases_isNull() {
    val response = TrackResponse(releases = null)

    assertThat(response.releases).isNull()
  }

  @Test
  fun testTrackResponse_equality_sameReleases_isEqual() {
    val entry = TrackResponse.ReleaseEntry(versionCodes = listOf(100L), status = "completed")
    val a = TrackResponse(releases = listOf(entry))
    val b = TrackResponse(releases = listOf(entry))

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testTrackResponse_equality_differentReleases_isNotEqual() {
    val a = TrackResponse(releases = listOf(TrackResponse.ReleaseEntry(listOf(100L), "completed")))
    val b = TrackResponse(releases = listOf(TrackResponse.ReleaseEntry(listOf(200L), "completed")))

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testTrackResponse_copy_updatesReleases() {
    val original = TrackResponse(releases = null)
    val newEntries = listOf(
      TrackResponse.ReleaseEntry(versionCodes = listOf(50L), status = "draft")
    )
    val copy = original.copy(releases = newEntries)

    assertThat(copy.releases).isNotNull()
    assertThat(original.releases).isNull()
  }

  // ---------------------------------------------------------------------------
  // ReleaseEntry
  // ---------------------------------------------------------------------------

  @Test
  fun testReleaseEntry_constructor_setsVersionCodesAndStatus() {
    val entry = TrackResponse.ReleaseEntry(versionCodes = listOf(300L, 301L), status = "inProgress")

    assertThat(entry.versionCodes).containsExactly(300L, 301L)
    assertThat(entry.status).isEqualTo("inProgress")
  }

  @Test
  fun testReleaseEntry_constructor_nullFields_areNull() {
    val entry = TrackResponse.ReleaseEntry(versionCodes = null, status = null)

    assertThat(entry.versionCodes).isNull()
    assertThat(entry.status).isNull()
  }

  @Test
  fun testReleaseEntry_equality_sameFields_isEqual() {
    val a = TrackResponse.ReleaseEntry(versionCodes = listOf(100L), status = "completed")
    val b = TrackResponse.ReleaseEntry(versionCodes = listOf(100L), status = "completed")

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testReleaseEntry_equality_differentStatus_isNotEqual() {
    val a = TrackResponse.ReleaseEntry(versionCodes = listOf(100L), status = "completed")
    val b = TrackResponse.ReleaseEntry(versionCodes = listOf(100L), status = "halted")

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testReleaseEntry_hashCode_equalObjects_haveSameHashCode() {
    val a = TrackResponse.ReleaseEntry(versionCodes = listOf(100L), status = "draft")
    val b = TrackResponse.ReleaseEntry(versionCodes = listOf(100L), status = "draft")

    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }
}
