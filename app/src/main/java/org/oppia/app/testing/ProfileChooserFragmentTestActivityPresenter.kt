package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.AppLanguage
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.StoryTextSize
import org.oppia.app.profile.ProfileChooserFragment
import org.oppia.domain.profile.ProfileManagementController
import javax.inject.Inject

/** The presenter for [ProfileChooserFragmentTestActivity]. */
@ActivityScope
class ProfileChooserFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val profileManagementController: ProfileManagementController
) {
  /** Adds [ProfileChooserFragment] to view. */
  fun handleOnCreate() {
    // TODO(#482): Ensures that an admin profile is present. Remove when there is proper admin account creation.
    profileManagementController.addProfile(
      name = "Sean",
      pin = "",
      avatarImagePath = null,
      allowDownloadAccess = true,
      colorRgb = -10710042,
      isAdmin = true,
      storyTextSize = StoryTextSize.SMALL_TEXT_SIZE,
      appLanguage = AppLanguage.ENGLISH_APP_LANGUAGE,
      audioLanguage = AudioLanguage.ENGLISH_AUDIO_LANGUAGE
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
    activity.setContentView(R.layout.profile_test_activity)
    if (getProfileChooserFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.profile_chooser_fragment_placeholder,
        ProfileChooserFragment(),
        ProfileChooserFragmentTestActivity.TAG_PROFILE_CHOOSER_FRAGMENT
      ).commitNow()
    }
  }

  private fun getProfileChooserFragment(): ProfileChooserFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.profile_chooser_fragment_placeholder) as ProfileChooserFragment?
  }
}
