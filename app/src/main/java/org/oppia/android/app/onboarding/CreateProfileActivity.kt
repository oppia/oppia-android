package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ScreenName.CREATE_PROFILE_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Activity for displaying a new learner profile creation flow. */
class CreateProfileActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var learnerProfileActivityPresenter: CreateProfileActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent.extractCurrentUserProfileId()
    val profileType = intent.getProtoExtra(
      CREATE_PROFILE_PARAMS_KEY,
      CreateProfileActivityParams.getDefaultInstance()
    ).profileType

    learnerProfileActivityPresenter.handleOnCreate(profileId, profileType)
  }

  companion object {
    /** Returns a new [Intent] open a [CreateProfileActivity] with the specified params. */
    fun createProfileActivityIntent(context: Context): Intent {
      return Intent(context, CreateProfileActivity::class.java).apply {
        decorateWithScreenName(CREATE_PROFILE_ACTIVITY)
      }
    }
  }
}
