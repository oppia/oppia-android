package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.ADMIN_INTRO_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for displaying the admin onboarding screen. */
class AdminIntroActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var adminIntroActivityPresenter: AdminIntroActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent.extractCurrentUserProfileId()

    adminIntroActivityPresenter.handleOnCreate(profileId)
  }

  /** Returns a new [Intent] to open an [AdminIntroActivity] with the specified params. */
  companion object {
    fun createAdminIntroActivityIntent(context: Context): Intent {
      return Intent(context, AdminIntroActivity::class.java).apply {
        decorateWithScreenName(ADMIN_INTRO_ACTIVITY)
      }
    }
  }
}
