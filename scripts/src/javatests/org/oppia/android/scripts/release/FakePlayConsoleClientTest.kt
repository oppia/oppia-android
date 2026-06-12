package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.assertThrows

/** Tests for [FakePlayConsoleClient]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class FakePlayConsoleClientTest {
  private lateinit var fake: FakePlayConsoleClient

  @Before
  fun setUp() {
    fake = FakePlayConsoleClient()
  }

  // ---------------------------------------------------------------------------
  // createEdit
  // ---------------------------------------------------------------------------

  @Test
  fun testCreateEdit_returnsIncrementingEditIds() {
    val first = fake.createEdit("org.oppia.android")
    val second = fake.createEdit("org.oppia.android")

    assertThat(first).isEqualTo("fake-edit-1")
    assertThat(second).isEqualTo("fake-edit-2")
  }

  @Test
  fun testCreateEdit_recordsCreatedEdits() {
    fake.createEdit("org.oppia.android")
    fake.createEdit("org.oppia.android")

    assertThat(fake.createdEdits).containsExactly("fake-edit-1", "fake-edit-2").inOrder()
  }

  @Test
  fun testCreateEdit_simulatedFailure_throwsAndClearsFlag() {
    fake.shouldFailNextCall = true

    val exception = assertThrows<IllegalStateException>() {
      fake.createEdit("org.oppia.android")
    }

    assertThat(exception).hasMessageThat().contains("createEdit")
    // Flag is reset after the simulated failure.
    assertThat(fake.shouldFailNextCall).isFalse()
  }

  // ---------------------------------------------------------------------------
  // getTrackReleases
  // ---------------------------------------------------------------------------

  @Test
  fun testGetTrackReleases_noReleasesConfigured_returnsEmpty() {
    val releases = fake.getTrackReleases("org.oppia.android", "alpha")

    assertThat(releases).isEmpty()
  }

  @Test
  fun testGetTrackReleases_configuredForTrack_returnsReleases() {
    val expected = listOf(PlayConsoleClient.TrackRelease(listOf(300L), "completed"))
    fake.setTrackReleases("alpha", expected)

    val releases = fake.getTrackReleases("org.oppia.android", "alpha")

    assertThat(releases).isEqualTo(expected)
  }

  @Test
  fun testGetTrackReleases_differentTracks_returnIndependently() {
    fake.setTrackReleases("alpha", listOf(PlayConsoleClient.TrackRelease(listOf(1L), "completed")))
    fake.setTrackReleases("beta", listOf(PlayConsoleClient.TrackRelease(listOf(2L), "draft")))

    assertThat(fake.getTrackReleases("org.oppia.android", "alpha").first().versionCodes)
      .containsExactly(1L)
    assertThat(fake.getTrackReleases("org.oppia.android", "beta").first().versionCodes)
      .containsExactly(2L)
  }

  @Test
  fun testGetTrackReleases_simulatedFailure_throws() {
    fake.shouldFailNextCall = true

    assertThrows<IllegalStateException>() {
      fake.getTrackReleases("org.oppia.android", "alpha")
    }
  }

  // ---------------------------------------------------------------------------
  // uploadAab
  // ---------------------------------------------------------------------------

  @Test
  fun testUploadAab_returnsIncrementingVersionCodes() {
    val first = fake.uploadAab("org.oppia.android", "edit-1", "/path/to/aab")
    val second = fake.uploadAab("org.oppia.android", "edit-1", "/path/to/aab")

    assertThat(first).isEqualTo(1L)
    assertThat(second).isEqualTo(2L)
  }

  @Test
  fun testUploadAab_recordsUpload() {
    fake.uploadAab("org.oppia.android", "edit-1", "/path/aab.aab")

    assertThat(fake.uploadedBundles).hasSize(1)
    val (pkg, edit, path) = fake.uploadedBundles.first()
    assertThat(pkg).isEqualTo("org.oppia.android")
    assertThat(edit).isEqualTo("edit-1")
    assertThat(path).isEqualTo("/path/aab.aab")
  }

  @Test
  fun testUploadAab_customVersionCode_returnsConfiguredValue() {
    fake.setNextVersionCode(500L)

    val versionCode = fake.uploadAab("org.oppia.android", "edit-1", "/aab.aab")

    assertThat(versionCode).isEqualTo(500L)
  }

  // ---------------------------------------------------------------------------
  // setTrackRelease
  // ---------------------------------------------------------------------------

  @Test
  fun testSetTrackRelease_recordsTrackUpdate() {
    fake.setTrackRelease(
      packageName = "org.oppia.android",
      editId = "edit-1",
      track = "alpha",
      versionCode = 300L,
      releaseNotes = mapOf("en-US" to "Release notes")
    )

    assertThat(fake.trackUpdates).hasSize(1)
    val update = fake.trackUpdates.first()
    assertThat(update.track).isEqualTo("alpha")
    assertThat(update.versionCode).isEqualTo(300L)
    assertThat(update.releaseNotes).containsEntry("en-US", "Release notes")
  }

  // ---------------------------------------------------------------------------
  // commitEdit
  // ---------------------------------------------------------------------------

  @Test
  fun testCommitEdit_recordsCommit() {
    fake.commitEdit("org.oppia.android", "edit-abc")

    assertThat(fake.committedEdits).containsExactly("edit-abc")
  }

  @Test
  fun testCommitEdit_multipleCommits_recordsAll() {
    fake.commitEdit("org.oppia.android", "edit-1")
    fake.commitEdit("org.oppia.android", "edit-2")

    assertThat(fake.committedEdits).containsExactly("edit-1", "edit-2").inOrder()
  }

  // ---------------------------------------------------------------------------
  // reset()
  // ---------------------------------------------------------------------------

  @Test
  fun testReset_clearsAllState() {
    fake.createEdit("org.oppia.android")
    fake.uploadAab("org.oppia.android", "edit-1", "/aab.aab")
    fake.commitEdit("org.oppia.android", "edit-1")
    fake.shouldFailNextCall = true

    fake.reset()

    assertThat(fake.createdEdits).isEmpty()
    assertThat(fake.uploadedBundles).isEmpty()
    assertThat(fake.committedEdits).isEmpty()
    assertThat(fake.trackUpdates).isEmpty()
    assertThat(fake.shouldFailNextCall).isFalse()
  }

  @Test
  fun testReset_resetsEditIdCounter() {
    fake.createEdit("org.oppia.android")
    fake.createEdit("org.oppia.android")

    fake.reset()

    val afterReset = fake.createEdit("org.oppia.android")
    assertThat(afterReset).isEqualTo("fake-edit-1")
  }
}
