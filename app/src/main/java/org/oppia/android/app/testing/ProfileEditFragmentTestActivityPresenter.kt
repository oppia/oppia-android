package org.oppia.android.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The presenter for [ProfileEditFragmentTestActivity]. */
@ActivityScope
class ProfileEditFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  /** Handles onCreate function of [ProfileEditActivity]. */
  fun handleOnCreate() {
    activity.setContentView(R.layout.profile_edit_activity)
    val profileId = activity.intent?.extractCurrentUserProfileId()?.loggedInInternalProfileId ?: 0
    if (getProfileEditFragment() == null) {
      activity.supportFragmentManager.beginTransaction().replace(
        R.id.profile_edit_fragment_placeholder,
        ProfileEditFragment.newInstance(profileId, isMultipane = false)
      ).commitNow()
    }
  }

  private fun getProfileEditFragment(): ProfileEditFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.profile_edit_fragment_placeholder) as? ProfileEditFragment
  }
}
