package org.oppia.app.profileprogress

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity to display profile picture. */
class ProfilePictureActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var profilePictureActivityPresenter: ProfilePictureActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val internalProfileId = intent.getIntExtra(
      PROFILE_PICTURE_ACTIVITY_PROFILE_ID_KEY, -1
    )
    profilePictureActivityPresenter.handleOnCreate(internalProfileId)
  }

  companion object {
    internal const val PROFILE_PICTURE_ACTIVITY_PROFILE_ID_KEY =
      "ProfilePictureActivity.internal_profile_id"

    fun createProfilePictureActivityIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, ProfilePictureActivity::class.java)
      intent.putExtra(PROFILE_PICTURE_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
