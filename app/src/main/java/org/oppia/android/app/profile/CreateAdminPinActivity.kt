package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.model.LegacyProfileId
import org.oppia.android.app.model.ScreenName.CREATE_ADMIN_PIN_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for displaying the Admin PIN setup screen. */
class CreateAdminPinActivity : InjectableSystemLocalizedAppCompatActivity() {

  @Inject
  lateinit var createAdminPinActivityPresenter: CreateAdminPinActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    (activityComponent as ActivityComponentImpl).inject(this)
    super.onCreate(savedInstanceState)

    val profileId = intent.extractCurrentUserProfileId()
    createAdminPinActivityPresenter.handleOnCreate(profileId)
  }

  companion object {
    /** Returns a new [Intent] to open a [CreateAdminPinActivity] with the specified params. */
    fun createAdminPinActivityIntent(context: Context, profileId: LegacyProfileId): Intent {
      return Intent(context, CreateAdminPinActivity::class.java).apply {
        decorateWithScreenName(CREATE_ADMIN_PIN_ACTIVITY)
        decorateWithUserProfileId(profileId)
      }
    }
  }
}
