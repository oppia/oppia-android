package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableSystemLocalizedAppCompatActivity
import org.oppia.android.app.model.CreateProfileActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.ScreenName.CREATE_PROFILE_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Argument key for [CreateProfileActivity] intent parameters. */
const val CREATE_PROFILE_PARAMS_KEY = "CreateProfileActivity.params"

/** Activity for displaying a new profile creation screen. */
class CreateProfileActivity : InjectableSystemLocalizedAppCompatActivity() {
  @Inject
  lateinit var learnerProfileActivityPresenter: CreateProfileActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent.extractCurrentUserProfileId()
    val params = intent.getProtoExtra(
      CREATE_PROFILE_PARAMS_KEY,
      CreateProfileActivityParams.getDefaultInstance()
    )

    learnerProfileActivityPresenter
      .handleOnCreate(profileId, params.profileType, params.avatarColor)
  }

  companion object {
    /** Returns a new [Intent] open a [CreateProfileActivity] with the specified params. */
    fun createProfileActivityIntent(
      context: Context,
      profileId: ProfileId,
      profileType: ProfileType,
      avatarColor: Int = 0
    ): Intent {
      return Intent(context, CreateProfileActivity::class.java).apply {
        decorateWithScreenName(CREATE_PROFILE_ACTIVITY)
        decorateWithUserProfileId(profileId)
        putProtoExtra(
          CREATE_PROFILE_PARAMS_KEY,
          CreateProfileActivityParams.newBuilder()
            .setProfileType(profileType)
            .setAvatarColor(avatarColor)
            .build()
        )
      }
    }
  }
}
