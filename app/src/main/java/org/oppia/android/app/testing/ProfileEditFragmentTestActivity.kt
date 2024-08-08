package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.settings.profile.ProfileEditFragment
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import javax.inject.Inject

/** Test Activity for testing [ProfileEditFragment] and its view models. */
class ProfileEditFragmentTestActivity : TestActivity() {
  @Inject
  lateinit var profileEditFragmentTestActivityPresenter: ProfileEditFragmentTestActivityPresenter
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileEditFragmentTestActivityPresenter.handleOnCreate()
  }

  companion object {
    /** Returns an [Intent] for opening [ProfileEditFragmentTestActivity]. */
    fun createProfileEditFragmentTestActivity(context: Context, internalProfileId: Int): Intent {
      val profileId = internalProfileId.let { ProfileId.newBuilder().setLoggedInInternalProfileId(it).build() }

      val intent = Intent(context, ProfileEditFragmentTestActivity::class.java)
      intent.decorateWithUserProfileId(profileId)
      return intent
    }
  }
}
