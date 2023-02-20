package org.oppia.android.app.settings.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.PROFILE_RENAME_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity that allows user to rename a profile. */
class ProfileRenameActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileRenameActivityPresenter: ProfileRenameActivityPresenter

  companion object {

    /** Returns an [Intent] for opening [ProfileRenameActivity]. */
    fun createProfileRenameActivity(context: Context, profileId: ProfileId): Intent {
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
      intent.extractCurrentUserProfileId()
    )
  }

  override fun onSupportNavigateUp(): Boolean {
    finish()
    return false
  }
}
