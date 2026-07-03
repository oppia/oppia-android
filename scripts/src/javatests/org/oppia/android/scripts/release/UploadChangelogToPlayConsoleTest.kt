package org.oppia.android.scripts.release

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.oppia.android.testing.assertThrows

/**
 * Tests for [main], [auditLiveTracks], [detectChangelogDiff], [uploadChangelogToTrack], and
 * [resolveNotesForTrack] in the upload_changelog_to_play_console script.
 *
 * [main] argument-validation tests cover the [require] blocks without a real Play Console
 * connection. All other functions are pure business logic tested via [FakePlayConsoleClient].
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
    assertThat(update.rolloutFraction).isEqualTo(1000)
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
      uploadChangelogToTrack(
        fakeClient, testPackageName, "alpha", 100L, mapOf("en-US" to tooLongNote)
      )
    }

    assertThat(exception).hasMessageThat().contains("500")
  }

  @Test
  fun testUploadChangelogToTrack_notesAtExactMaxLength_doesNotThrow() {
    val exactLengthNote = "A".repeat(500)

    // Should not throw — 500 chars is exactly at the limit.
    uploadChangelogToTrack(
      fakeClient, testPackageName, "alpha", 100L, mapOf("en-US" to exactLengthNote)
    )

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
        packageName: String,
        editId: String,
        track: String,
        versionCode: Long,
        rolloutFraction: Int,
        releaseNotes: Map<String, String>
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

  // ---------------------------------------------------------------------------
  // resolveNotesForTrack — per-track changelog file resolution
  // ---------------------------------------------------------------------------

  @Test
  fun testResolveNotesForTrack_sharedFileExists_returnsSharedNotes() {
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    changelogsDir.resolve("0.18.md").writeText("Shared release notes.")

    val result = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")

    assertThat(result).containsEntry("en-US", "Shared release notes.")
  }

  @Test
  fun testResolveNotesForTrack_trackSpecificFileExists_returnsTrackSpecificNotes() {
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    changelogsDir.resolve("0.18.md").writeText("Shared notes.")
    changelogsDir.resolve("0.18_alpha.md").writeText("Alpha-specific notes.")

    val result = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")

    assertThat(result).containsEntry("en-US", "Alpha-specific notes.")
  }

  @Test
  fun testResolveNotesForTrack_trackSpecificFileExists_otherTrackUsesSharedFile() {
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    changelogsDir.resolve("0.18.md").writeText("Shared notes.")
    changelogsDir.resolve("0.18_beta.md").writeText("Beta-specific notes.")

    val alphaResult = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")
    val betaResult = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "beta")

    assertThat(alphaResult).containsEntry("en-US", "Shared notes.")
    assertThat(betaResult).containsEntry("en-US", "Beta-specific notes.")
  }

  @Test
  fun testResolveNotesForTrack_noFileExists_returnsEmptyMap() {
    tempFolder.newFolder("config", "changelogs")

    val result = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")

    assertThat(result).isEmpty()
  }

  @Test
  fun testResolveNotesForTrack_emptySharedFile_returnsEmptyMap() {
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    changelogsDir.resolve("0.18.md").writeText("   ")

    val result = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")

    assertThat(result).isEmpty()
  }

  @Test
  fun testResolveNotesForTrack_notesThatExceedMaxLength_throwsIllegalStateException() {
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    changelogsDir.resolve("0.18.md").writeText("A".repeat(501))

    val exception = assertThrows<IllegalStateException> {
      resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")
    }

    assertThat(exception).hasMessageThat().contains("500")
  }

  @Test
  fun testResolveNotesForTrack_notesAtExactMaxLength_returnsNotes() {
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    val exactNotes = "A".repeat(500)
    changelogsDir.resolve("0.18.md").writeText(exactNotes)

    val result = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")

    assertThat(result).containsEntry("en-US", exactNotes)
  }

  @Test
  fun testResolveNotesForTrack_trailingWhitespaceInFile_isTrimmedBeforeReturn() {
    val changelogsDir = tempFolder.newFolder("config", "changelogs")
    changelogsDir.resolve("0.18.md").writeText("  Release notes.  \n")

    val result = resolveNotesForTrack(tempFolder.root.absolutePath, "0.18", "alpha")

    assertThat(result).containsEntry("en-US", "Release notes.")
  }

  // ---------------------------------------------------------------------------
  // main() — argument validation
  // ---------------------------------------------------------------------------

  @Test
  fun testMain_wrongArgumentCount_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(emptyArray())
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_tooManyArguments_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "0.17", "token", "extra"))
    }

    assertThat(exception).hasMessageThat().contains("Usage:")
  }

  @Test
  fun testMain_blankWorkspacePath_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("", "org.oppia.android", "0.17", "token"))
    }

    assertThat(exception).hasMessageThat().contains("workspace_path must not be blank")
  }

  @Test
  fun testMain_blankPackageName_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "", "0.17", "token"))
    }

    assertThat(exception).hasMessageThat().contains("package_name must not be blank")
  }

  @Test
  fun testMain_blankGcpAccessToken_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "0.17", ""))
    }

    assertThat(exception).hasMessageThat().contains("gcp_access_token must not be blank")
  }

  @Test
  fun testMain_invalidVersionFormat_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "invalid-version", "token"))
    }

    assertThat(exception).hasMessageThat().contains("version must be in major.minor format")
  }

  @Test
  fun testMain_versionWithoutMinor_throwsIllegalArgumentException() {
    val exception = assertThrows<IllegalArgumentException> {
      main(arrayOf("/workspace", "org.oppia.android", "17", "token"))
    }

    assertThat(exception).hasMessageThat().contains("version must be in major.minor format")
  }
}
