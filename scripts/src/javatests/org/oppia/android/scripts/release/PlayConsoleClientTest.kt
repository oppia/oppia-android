package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/** Tests for [PlayConsoleClient] — specifically the [PlayConsoleClient.TrackRelease] data class. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class PlayConsoleClientTest {

  @Test
  fun testTrackRelease_constructor_setsVersionCodesAndStatus() {
    val release = PlayConsoleClient.TrackRelease(
      versionCodes = listOf(300L, 301L),
      status = "completed"
    )

    assertThat(release.versionCodes).containsExactly(300L, 301L)
    assertThat(release.status).isEqualTo("completed")
  }

  @Test
  fun testTrackRelease_constructor_emptyVersionCodes_isAllowed() {
    val release = PlayConsoleClient.TrackRelease(versionCodes = emptyList(), status = "draft")

    assertThat(release.versionCodes).isEmpty()
  }

  @Test
  fun testTrackRelease_equality_sameFields_isEqual() {
    val a = PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed")
    val b = PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed")

    assertThat(a).isEqualTo(b)
  }

  @Test
  fun testTrackRelease_equality_differentStatus_isNotEqual() {
    val a = PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed")
    val b = PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "halted")

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testTrackRelease_equality_differentVersionCodes_isNotEqual() {
    val a = PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed")
    val b = PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "completed")

    assertThat(a).isNotEqualTo(b)
  }

  @Test
  fun testTrackRelease_copy_updatesStatus() {
    val original = PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "draft")
    val copy = original.copy(status = "completed")

    assertThat(copy.status).isEqualTo("completed")
    assertThat(original.status).isEqualTo("draft")
  }

  @Test
  fun testTrackRelease_copy_updatesVersionCodes() {
    val original = PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed")
    val copy = original.copy(versionCodes = listOf(200L, 201L))

    assertThat(copy.versionCodes).containsExactly(200L, 201L)
    assertThat(original.versionCodes).containsExactly(100L)
  }

  @Test
  fun testTrackRelease_hashCode_equalObjects_haveSameHashCode() {
    val a = PlayConsoleClient.TrackRelease(versionCodes = listOf(42L), status = "inProgress")
    val b = PlayConsoleClient.TrackRelease(versionCodes = listOf(42L), status = "inProgress")

    assertThat(a.hashCode()).isEqualTo(b.hashCode())
  }
}
