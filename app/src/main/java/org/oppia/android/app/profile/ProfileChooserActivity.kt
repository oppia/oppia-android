package org.oppia.android.app.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.model.ScreenName.PROFILE_CHOOSER_ACTIVITY
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ProfileChooserActivityParams
import org.oppia.android.app.onboarding.CREATE_PROFILE_PARAMS_KEY
import org.oppia.android.app.onboarding.PROFILE_CHOOSER_PARAMS_KEY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** Activity that controls profile creation and selection. */
class ProfileChooserActivity : InjectableSystemLocalizedAppCompatActivity() {
  @Inject
  lateinit var profileChooserActivityPresenter: ProfileChooserActivityPresenter

  companion object {
    fun createProfileChooserActivity(context: Context): Intent {
      return Intent(context, ProfileChooserActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        decorateWithScreenName(PROFILE_CHOOSER_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileType = intent.getProtoExtra(
      PROFILE_CHOOSER_PARAMS_KEY,
      ProfileChooserActivityParams.getDefaultInstance()
    ).profileType

    val profileId = intent.extractCurrentUserProfileId()

    profileChooserActivityPresenter.handleOnCreate(profileId, profileType)
  }
}
