package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.AppLanguageActivityParams
import org.oppia.android.app.model.AppLanguageActivityResultBundle
import org.oppia.android.app.model.AppLanguageActivityStateBundle
import org.oppia.android.app.model.OppiaLanguage
import org.oppia.android.app.model.ScreenName.APP_LANGUAGE_ACTIVITY
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.getProtoExtra
import org.oppia.android.util.extensions.putProto
import org.oppia.android.util.extensions.putProtoExtra
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    appLanguageActivityPresenter.handleOnCreate(
      savedInstanceState?.retrieveLanguageFromSavedState() ?: intent.retrieveLanguageFromParams()
    )
  }

  companion object {
    private const val ACTIVITY_PARAMS_KEY = "AppLanguageActivity.params"
    private const val ACTIVITY_SAVED_STATE_KEY = "AppLanguageActivity.saved_state"
    internal const val APP_LANGUAGE_PREFERENCE_TITLE_EXTRA_KEY =
      "AppLanguageActivity.app_language_preference_title"
    const val APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY =
      "AppLanguageActivity.app_language_preference_summary_value"
    internal const val SELECTED_LANGUAGE_EXTRA_KEY = "AppLanguageActivity.selected_language"

    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      oppiaLanguage: OppiaLanguage
    ): Intent {
      return Intent(context, AppLanguageActivity::class.java).apply {
        val arguments = AppLanguageActivityParams.newBuilder().apply {
          this.oppiaLanguage = oppiaLanguage
        }.build()
        putProtoExtra(ACTIVITY_PARAMS_KEY, arguments)
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

    fun getAppLanguagePreferenceTitleExtraKey(): String {
      return APP_LANGUAGE_PREFERENCE_TITLE_EXTRA_KEY
    }

    fun getAppLanguagePreferenceSummaryValueExtraKey(): String {
      return APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY
    }
  }

  override fun onBackPressed() {
    val resultBundle = AppLanguageActivityResultBundle.newBuilder().apply {
      oppiaLanguage = appLanguageActivityPresenter.getLanguageSelected()
    }.build()
    val intent = Intent().apply {
      putProtoExtra(MESSAGE_APP_LANGUAGE_ARGUMENT_KEY, resultBundle)
    }
    setResult(REQUEST_CODE_APP_LANGUAGE, intent)
    finish()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    val state = AppLanguageActivityStateBundle.newBuilder().apply {
      oppiaLanguage = appLanguageActivityPresenter.getLanguageSelected()
    }.build()
    outState.putProto(ACTIVITY_SAVED_STATE_KEY, state)
  }
}
