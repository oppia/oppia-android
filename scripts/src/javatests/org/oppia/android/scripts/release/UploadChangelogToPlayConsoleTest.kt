package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows

// Note: FakePlayConsoleClient and PlayConsoleClient are provided by PR 1.4
// (upload-binary-to-play-console). This file will not compile until that PR merges into develop.

/**
 * Tests for [auditLiveTracks], [detectChangelogDiff], and [uploadChangelogToTrack] in the
 * upload_changelog_to_play_console script.
 *
 * These functions are pure business logic (no real Play Console credentials needed); the
 * [FakePlayConsoleClient] stubs out all API interactions.
 */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
class UploadChangelogToPlayConsoleTest {
  @field:[Rule JvmField] val tempFolder = TemporaryFolder()

  private lateinit var fakeClient: FakePlayConsoleClient

  private val testPackageName = "org.oppia.android"

  @Before
  fun setUp() {
    fakeClient = FakePlayConsoleClient()
  }

  // ---------------------------------------------------------------------------
  // auditLiveTracks — status filtering
  // ---------------------------------------------------------------------------

  @Test
  fun testAuditLiveTracks_completedRelease_returnsTrack() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )

    val result = auditLiveTracks(fakeClient, testPackageName, listOf("alpha"))

    assertThat(result.keys).containsExactly("alpha")
  }

  @Test
  fun testAuditLiveTracks_inProgressRelease_returnsTrack() {
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "inProgress"))
    )

    val result = auditLiveTracks(fakeClient, testPackageName, listOf("beta"))

    assertThat(result.keys).containsExactly("beta")
  }

  @Test
  fun testAuditLiveTracks_draftRelease_excludesTrack() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "draft"))
    )

    val result = auditLiveTracks(fakeClient, testPackageName, listOf("alpha"))

    assertThat(result).isEmpty()
  }

  @Test
  fun testAuditLiveTracks_haltedRelease_excludesTrack() {
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(300L), status = "halted"))
    )

    val result = auditLiveTracks(fakeClient, testPackageName, listOf("production"))

    assertThat(result).isEmpty()
  }

  @Test
  fun testAuditLiveTracks_emptyTrack_excludesTrack() {
    // No releases configured — getTrackReleases returns empty list.

    val result = auditLiveTracks(fakeClient, testPackageName, listOf("alpha"))

    assertThat(result).isEmpty()
  }

  @Test
  fun testAuditLiveTracks_allTracksEmpty_returnsEmptyMap() {
    val result = auditLiveTracks(fakeClient, testPackageName)

    assertThat(result).isEmpty()
  }

  @Test
  fun testAuditLiveTracks_multipleTracksWithMixedStatuses_returnsOnlyLiveTracks() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"))
    )
    fakeClient.setTrackReleases(
      "beta",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(200L), status = "draft"))
    )
    fakeClient.setTrackReleases(
      "production",
      listOf(PlayConsoleClient.TrackRelease(versionCodes = listOf(300L), status = "inProgress"))
    )

    val result = auditLiveTracks(fakeClient, testPackageName)

    assertThat(result.keys).containsExactly("alpha", "production")
  }

  @Test
  fun testAuditLiveTracks_trackWithMixedReleaseStatuses_returnsOnlyLiveReleases() {
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = listOf(100L), status = "completed"),
        PlayConsoleClient.TrackRelease(versionCodes = listOf(99L), status = "halted")
      )
    )

    val result = auditLiveTracks(fakeClient, testPackageName, listOf("alpha"))

    assertThat(result.keys).containsExactly("alpha")
    // Only the live release is included.
    assertThat(result["alpha"]).hasSize(1)
    assertThat(result["alpha"]!![0].status).isEqualTo("completed")
  }

  @Test
  fun testAuditLiveTracks_completedRelease_returnsCorrectVersionCodes() {
    val expectedVersionCodes = listOf(105L, 106L)
    fakeClient.setTrackReleases(
      "alpha",
      listOf(
        PlayConsoleClient.TrackRelease(versionCodes = expectedVersionCodes, status = "completed")
      )
    )

    val result = auditLiveTracks(fakeClient, testPackageName, listOf("alpha"))

    assertThat(result["alpha"]!![0].versionCodes).containsExactlyElementsIn(expectedVersionCodes)
  }

  // ---------------------------------------------------------------------------
  // detectChangelogDiff — string comparison
  // ---------------------------------------------------------------------------

  @Test
  fun testDetectChangelogDiff_identicalNotes_returnsFalse() {
    val notes = "Fixed a crash on startup."

    val result = detectChangelogDiff(notes, notes)

    assertThat(result).isFalse()
  }

  @Test
  fun testDetectChangelogDiff_notesWithLeadingAndTrailingWhitespace_returnsFalse() {
    val localNotes = "  Fixed a crash on startup.\n"
    val deployedNotes = "Fixed a crash on startup."

    val result = detectChangelogDiff(localNotes, deployedNotes)

    assertThat(result).isFalse()
  }

  @Test
  fun testDetectChangelogDiff_differentNotes_returnsTrue() {
    val localNotes = "Fixed a crash on startup."
    val deployedNotes = "Old release notes."

    val result = detectChangelogDiff(localNotes, deployedNotes)

    assertThat(result).isTrue()
  }

  @Test
  fun testDetectChangelogDiff_bothEmptyNotes_returnsFalse() {
    val result = detectChangelogDiff("", "")

    assertThat(result).isFalse()
  }

  @Test
  fun testDetectChangelogDiff_localNotesEmptyDeployedNonEmpty_returnsTrue() {
    val result = detectChangelogDiff("", "Some deployed notes.")

    assertThat(result).isTrue()
  }

  @Test
  fun testDetectChangelogDiff_localNotesNonEmptyDeployedEmpty_returnsTrue() {
    val result = detectChangelogDiff("New local notes.", "")

    assertThat(result).isTrue()
  }

  @Test
  fun testDetectChangelogDiff_whitespaceDifferencesOnly_returnsFalse() {
    val localNotes = "\n\nFixed a crash.\n\n"
    val deployedNotes = "Fixed a crash."

    val result = detectChangelogDiff(localNotes, deployedNotes)

    assertThat(result).isFalse()
  }

  // ---------------------------------------------------------------------------
  // uploadChangelogToTrack — API interaction
  // ---------------------------------------------------------------------------

  @Test
  fun testUploadChangelogToTrack_validInput_createsEditThenSetsNotesThenCommits() {
    val versionCode = 100L
    val notes = mapOf("en-US" to "Fixed a crash.")

    uploadChangelogToTrack(fakeClient, testPackageName, "alpha", versionCode, notes)

    assertThat(fakeClient.createdEdits).hasSize(1)
    assertThat(fakeClient.trackUpdates).hasSize(1)
    assertThat(fakeClient.committedEdits).hasSize(1)
  }

  @Test
  fun testUploadChangelogToTrack_validInput_setsCorrectTrackAndVersionCode() {
    val versionCode = 200L
    val notes = mapOf("en-US" to "Improved performance.")

    uploadChangelogToTrack(fakeClient, testPackageName, "beta", versionCode, notes)

    val update = fakeClient.trackUpdates.single()
    assertThat(update.track).isEqualTo("beta")
    assertThat(update.versionCode).isEqualTo(versionCode)
  }

  @Test
  fun testUploadChangelogToTrack_validInput_setsCorrectReleaseNotes() {
    val notes = mapOf("en-US" to "Fixed a crash.")

    uploadChangelogToTrack(fakeClient, testPackageName, "alpha", 100L, notes)

    val update = fakeClient.trackUpdates.single()
    assertThat(update.releaseNotes).containsEntry("en-US", "Fixed a crash.")
  }

  @Test
  fun testUploadChangelogToTrack_validInput_usesFullRolloutFraction() {
    uploadChangelogToTrack(
      fakeClient, testPackageName, "alpha", 100L, mapOf("en-US" to "Fixed a crash.")
    )

    val update = fakeClient.trackUpdates.single()
    assertThat(update.rolloutFraction).isEqualTo(1.0)
  }

  @Test
  fun testUploadChangelogToTrack_validInput_commitsTheSameEditThatWasCreated() {
    uploadChangelogToTrack(
      fakeClient, testPackageName, "production", 300L, mapOf("en-US" to "Stability improvements.")
    )

    val createdEditId = fakeClient.createdEdits.single()
    val committedEditId = fakeClient.committedEdits.single()
    assertThat(committedEditId).isEqualTo(createdEditId)
  }

  @Test
  fun testUploadChangelogToTrack_multipleLanguageNotes_setsAllNotes() {
    val notes = mapOf(
      "en-US" to "Fixed a crash.",
      "fr-FR" to "Correction d'un crash.",
      "de-DE" to "Absturz behoben."
    )

    uploadChangelogToTrack(fakeClient, testPackageName, "alpha", 100L, notes)

    val update = fakeClient.trackUpdates.single()
    assertThat(update.releaseNotes).containsExactlyEntriesIn(notes)
  }

  @Test
  fun testUploadChangelogToTrack_missingEnUsKey_throwsIllegalArgumentException() {
    val notesWithoutEnUs = mapOf("fr-FR" to "Correction d'un crash.")

    val exception = assertThrows<IllegalArgumentException> {
      uploadChangelogToTrack(fakeClient, testPackageName, "alpha", 100L, notesWithoutEnUs)
    }

    assertThat(exception).hasMessageThat().contains("en-US")
  }

  @Test
  fun testUploadChangelogToTrack_emptyNotes_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      uploadChangelogToTrack(fakeClient, testPackageName, "alpha", 100L, emptyMap())
    }

    assertThat(exception).hasMessageThat().contains("en-US")
  }

  @Test
  fun testUploadChangelogToTrack_notesThatExceedMaxLength_throwsIllegalArgumentException() {
    val tooLongNote = "A".repeat(501)

    val exception = assertThrows<IllegalArgumentException> {
      uploadChangelogToTrack(fakeClient, testPackageName, "alpha", 100L, mapOf("en-US" to tooLongNote))
    }

    assertThat(exception).hasMessageThat().contains("500")
  }

  @Test
  fun testUploadChangelogToTrack_notesAtExactMaxLength_doesNotThrow() {
    val exactLengthNote = "A".repeat(500)

    // Should not throw — 500 chars is exactly at the limit.
    uploadChangelogToTrack(fakeClient, testPackageName, "alpha", 100L, mapOf("en-US" to exactLengthNote))

    assertThat(fakeClient.committedEdits).hasSize(1)
  }

  @Test
  fun testUploadChangelogToTrack_createEditFails_propagatesException() {
    fakeClient.shouldFailNextCall = true

    val exception = assertThrows<IllegalStateException> {
      uploadChangelogToTrack(
        fakeClient, testPackageName, "alpha", 100L, mapOf("en-US" to "Fixed a crash.")
      )
    }

    assertThat(exception).hasMessageThat().contains("simulated failure")
    // No edit should have been committed.
    assertThat(fakeClient.committedEdits).isEmpty()
  }

  @Test
  fun testUploadChangelogToTrack_setTrackReleaseFails_propagatesException() {
    // Let createEdit succeed, then fail on setTrackRelease.
    fakeClient.nextEditId = 1
    // Schedule the failure for the second call (setTrackRelease).
    uploadChangelogToTrack( // warm up createEdit to succeed
      FakePlayConsoleClient(), testPackageName, "alpha", 99L, mapOf("en-US" to "Warmup")
    )
    fakeClient.shouldFailNextCall = false
    // Now on the real client: createEdit succeeds, shouldFailNextCall triggers on setTrackRelease.
    // Reset and set shouldFailNextCall after createEdit manually.
    val capturingClient = object : PlayConsoleClient {
      val inner = FakePlayConsoleClient()
      var failOnSetTrackRelease = true
      override fun createEdit(packageName: String) = inner.createEdit(packageName)
      override fun getTrackReleases(packageName: String, track: String) =
        inner.getTrackReleases(packageName, track)
      override fun uploadAab(packageName: String, editId: String, aabPath: String) =
        inner.uploadAab(packageName, editId, aabPath)
      override fun setTrackRelease(
        packageName: String, editId: String, track: String,
        versionCode: Long, rolloutFraction: Double, releaseNotes: Map<String, String>
      ) {
        if (failOnSetTrackRelease) error("simulated failure in setTrackRelease")
      }
      override fun commitEdit(packageName: String, editId: String) =
        inner.commitEdit(packageName, editId)
    }

    val exception = assertThrows<IllegalStateException> {
      uploadChangelogToTrack(
        capturingClient, testPackageName, "alpha", 100L, mapOf("en-US" to "Fixed a crash.")
      )
    }

    assertThat(exception).hasMessageThat().contains("simulated failure")
    assertThat(capturingClient.inner.committedEdits).isEmpty()
  }
}
