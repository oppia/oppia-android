package org.oppia.android.app.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AdminIntroActivityParams
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ProfileType
import org.oppia.android.app.model.ScreenName.ADMIN_INTRO_ACTIVITY
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** Argument key for [AdminIntroActivity] intent parameters. */
const val ADMIN_INTRO_PARAMS_KEY = "AdminIntroActivityParams.params"

/** Activity for displaying the admin onboarding screen. */
class AdminIntroActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var adminIntroActivityPresenter: AdminIntroActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    (activityComponent as ActivityComponentImpl).inject(this)

    val profileId = intent.extractCurrentUserProfileId()
    val adminIntroActivityParams = intent.getProtoExtra(
      ADMIN_INTRO_PARAMS_KEY, AdminIntroActivityParams.getDefaultInstance()
    )
    val profileType = adminIntroActivityParams.profileType
    val profileNickname = adminIntroActivityParams.profileNickname

    adminIntroActivityPresenter.handleOnCreate(profileId, profileType, profileNickname)
  }

  companion object {
    /** Returns a new [Intent] to open an [AdminIntroActivity] with the specified params. */
    fun createAdminIntroActivityIntent(
      context: Context,
      profileId: ProfileId,
      profileType: ProfileType,
      profileNickname: String
    ): Intent {
      val introActivityParams = AdminIntroActivityParams.newBuilder()
        .setProfileType(profileType)
        .setProfileNickname(profileNickname)
        .build()

      return Intent(context, AdminIntroActivity::class.java).apply {
        decorateWithScreenName(ADMIN_INTRO_ACTIVITY)
        putProtoExtra(ADMIN_INTRO_PARAMS_KEY, introActivityParams)
        decorateWithUserProfileId(profileId)
      }
    }
  }
}
