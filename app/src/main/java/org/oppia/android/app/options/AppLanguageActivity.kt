package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.model.AppLanguageActivityParams
import org.oppia.android.app.model.AppLanguageActivityStateBundle
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.ScreenName.APP_LANGUAGE_ACTIVITY
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId
import javax.inject.Inject

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter
  private var profileId: Int? = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    profileId = intent?.extractCurrentUserProfileId()?.loggedInInternalProfileId ?: -1
    appLanguageActivityPresenter.handleOnCreate(
      savedInstanceState?.retrieveLanguageFromSavedState() ?: intent.retrieveLanguageFromParams(),
      profileId!!
    )
  }

  companion object {
    private const val ACTIVITY_PARAMS_KEY = "AppLanguageActivity.params"
    private const val ACTIVITY_SAVED_STATE_KEY = "AppLanguageActivity.saved_state"

    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      oppiaLanguage: OppiaLanguage,
      internalProfileId: Int?
    ): Intent {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId!!).build()
      return Intent(context, AppLanguageActivity::class.java).apply {
        val arguments = AppLanguageActivityParams.newBuilder().apply {
          this.oppiaLanguage = oppiaLanguage
        }.build()
        putProtoExtra(ACTIVITY_PARAMS_KEY, arguments)
        decorateWithUserProfileId(profileId)
        decorateWithScreenName(APP_LANGUAGE_ACTIVITY)
      }
    }

    private fun Intent.retrieveLanguageFromParams(): OppiaLanguage {
      return getProtoExtra(
        ACTIVITY_PARAMS_KEY, AppLanguageActivityParams.getDefaultInstance()
      ).oppiaLanguage
    }

    private fun Bundle.retrieveLanguageFromSavedState(): OppiaLanguage {
      return getProto(
        ACTIVITY_SAVED_STATE_KEY, AppLanguageActivityStateBundle.getDefaultInstance()
      ).oppiaLanguage
    }
  }

  override fun onBackPressed() = finish()

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val state = AppLanguageActivityStateBundle.newBuilder().apply {
      oppiaLanguage = appLanguageActivityPresenter.getLanguageSelected()
    }.build()
    outState.putProto(ACTIVITY_SAVED_STATE_KEY, state)
  }
}
