package org.oppia.android.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ScreenName.PROFILE_PICTURE_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** Activity to display profile picture. */
class ProfilePictureActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var profilePictureActivityPresenter: ProfilePictureActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    val internalProfileId = intent.getIntExtra(
      PROFILE_PICTURE_ACTIVITY_PROFILE_ID_KEY, -1
    )
    profilePictureActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    internal const val PROFILE_PICTURE_ACTIVITY_PROFILE_ID_KEY =
      "ProfilePictureActivity.internal_profile_id"

    fun createProfilePictureActivityIntent(context: Context, internalProfileId: Int): Intent {
      return Intent(context, ProfilePictureActivity::class.java).apply {
        decorateWithScreenName(PROFILE_PICTURE_ACTIVITY)
        putExtra(
          PROFILE_PICTURE_ACTIVITY_PROFILE_ID_KEY, internalProfileId
        )
      }
    }
  }
}
