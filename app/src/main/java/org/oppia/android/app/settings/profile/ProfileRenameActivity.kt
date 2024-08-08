package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.PROFILE_RENAME_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity that allows user to rename a profile. */
class ProfileRenameActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var profileRenameActivityPresenter: ProfileRenameActivityPresenter

  companion object {

    /** Returns an [Intent] for opening [ProfileRenameActivity]. */
    fun createProfileRenameActivity(context: Context, internalProfileId: Int): Intent {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      return Intent(context, ProfileRenameActivity::class.java).apply {
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(PROFILE_RENAME_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    (activityComponent as ActivityComponentImpl).inject(this)
    profileRenameActivityPresenter.handleOnCreate(
      intent?.extractCurrentUserProfileId()?.loggedInInternalProfileId ?: 0
    )
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
