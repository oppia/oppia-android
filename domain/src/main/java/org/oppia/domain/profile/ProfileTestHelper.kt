package org.oppia.domain.profile

import androidx.lifecycle.LiveData
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryTextSize
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** This helper allows tests to easily create new profiles and switch between them. */
class ProfileTestHelper @Inject constructor(
  private val profileManagementController: ProfileManagementController
) {
  /** Creates one admin profile and one user profile. Logs in to admin profile. */
  fun initializeProfiles(): LiveData<AsyncResult<Any?>> {
    profileManagementController.addProfile(
      name = "Sean",
      pin = "12345",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true,
      storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
      appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
      audioLanguage = AudioLanguage.HINDI_AUDIO_LANGUAGE
    )
    profileManagementController.addProfile(
      name = "Ben",
      pin = "123",
      avatarImagePath = null,
      allowDownloadAccess = false,
      colorRgb = -10710042,
      isAdmin = false,
      storyTextSize = StoryTextSize.MEDIUM_TEXT_SIZE,
      appLanguage = AppLanguage.HINDI_APP_LANGUAGE,
      audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
    )
    return profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(0).build())
  }

  /** Create [numProfiles] number of user profiles. */
  fun addMoreProfiles(numProfiles: Int) {
    for (x in 0 until numProfiles) {
      profileManagementController.addProfile(
        name = (x + 65).toChar().toString(),
        pin = "123",
        avatarImagePath = null,
        allowDownloadAccess = false,
        colorRgb = -10710042,
        isAdmin = false,
        storyTextSize = StoryTextSize.LARGE_TEXT_SIZE,
        appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
        audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
      )
    }
  }

  /** Login to admin profile. */
  fun loginToAdmin() =
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(0).build())

  /** Login to user profile. */
  fun loginToUser() =
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(1).build())
  /** Login to user profile. */
  fun loginToUser2() =
    profileManagementController.loginToProfile(ProfileId.newBuilder().setInternalId(2).build())

  fun getStoryTextSize(storyTextSize: StoryTextSize): Any {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE -> 16f
      StoryTextSize.MEDIUM_TEXT_SIZE -> 18f
      StoryTextSize.LARGE_TEXT_SIZE -> 20f
      else -> 22f
    }
  }

  fun getAppLanguage(appLanguage: AppLanguage): Any {
    return when (appLanguage) {
      AppLanguage.ENGLISH_APP_LANGUAGE -> "English"
      AppLanguage.HINDI_APP_LANGUAGE -> "Hindi"
      AppLanguage.FRENCH_APP_LANGUAGE -> "French"
      AppLanguage.CHINESE_APP_LANGUAGE -> "Chinese"
      else -> "English"
    }
  }

  fun getAudioLanguage(audioLanguage: AudioLanguage): Any {
    return when (audioLanguage) {
      AudioLanguage.NO_AUDIO-> "No Audio"
      AudioLanguage.ENGLISH_AUDIO_LANGUAGE-> "English"
      AudioLanguage.HINDI_AUDIO_LANGUAGE -> "Hindi"
      AudioLanguage.FRENCH_AUDIO_LANGUAGE -> "French"
      AudioLanguage.CHINESE_AUDIO_LANGUAGE -> "Chinese"
      else -> "No Audio"
    }
  }
}
