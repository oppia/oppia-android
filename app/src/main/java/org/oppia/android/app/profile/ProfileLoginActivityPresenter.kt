package org.oppia.android.app.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.ui.R
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

private const val TAG_PROFILE_LOGIN_FRAGMENT = "TAG_PROFILE_LOGIN_FRAGMENT"

/** The presenter for [ProfileLoginActivity]. */
@ActivityScope
class ProfileLoginActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  /** Creates the view for [ProfileLoginActivity]. */
  fun handleOnCreate(profileId: ProfileId) {
    activity.setContentView(R.layout.profile_login_activity)

    if (getProfileLoginFragment() == null) {
      val profileLoginFragment = ProfileLoginFragment().apply {
        arguments = Bundle().also { it.decorateWithUserProfileId(profileId) }
      }

      activity.supportFragmentManager.beginTransaction()
        .add(
          R.id.profile_login_fragment_placeholder, profileLoginFragment, TAG_PROFILE_LOGIN_FRAGMENT
        )
        .commitNow()
    }
  }

  private fun getProfileLoginFragment(): ProfileLoginFragment? {
    return activity.supportFragmentManager.findFragmentByTag(
      TAG_PROFILE_LOGIN_FRAGMENT
    ) as? ProfileLoginFragment
  }
}
