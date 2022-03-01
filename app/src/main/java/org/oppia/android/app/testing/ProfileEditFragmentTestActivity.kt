package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.settings.profile.PROFILE_EDIT_PROFILE_ID_EXTRA_KEY
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.testing.activity.TestActivity
import javax.inject.Inject

/** Test Activity for testing [ProfileEditFragment] and it's view models. */
class ProfileEditFragmentTestActivity : TestActivity() {
  @Inject
  lateinit var profileEditFragmentTestActivityPresenter: ProfileEditFragmentTestActivityPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileEditFragmentTestActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns an [Intent] for opening the [ProfileEditFragmentTestActivitytivity]. */
    fun createProfileEditFragmentTestActivity(context: Context, profileId: Int): Intent {
      val intent = Intent(context, ProfileEditFragmentTestActivity::class.java)
      intent.putExtra(PROFILE_EDIT_PROFILE_ID_EXTRA_KEY, profileId)
      return intent
    }
  }
}
