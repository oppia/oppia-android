package org.oppia.android.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.PROFILE_PICTURE_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity to display profile picture. */
class ProfilePictureActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var profilePictureActivityPresenter: ProfilePictureActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val profileId = intent.extractCurrentUserProfileId()
    profilePictureActivityPresenter.handleOnCreate(profileId.internalId)
  }

  companion object {

    fun createProfilePictureActivityIntent(context: Context, profileId: ProfileId): Intent {
      return Intent(context, ProfilePictureActivity::class.java).apply {
        decorateWithScreenName(PROFILE_PICTURE_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }
}
