package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    prefSummaryValue = if (savedInstanceState == null) {
      checkNotNull(intent.getStringExtra(APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY)) {
        "Expected $APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY to be in intent extras."
      }
    } else {
      savedInstanceState.get(SELECTED_LANGUAGE_EXTRA_KEY) as String
    }
    appLanguageActivityPresenter.handleOnCreate(prefSummaryValue)
  }

  companion object {
    const val APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY =
      "AppLanguageActivity.app_language_preference_summary_value"
    internal const val SELECTED_LANGUAGE_EXTRA_KEY = "AppLanguageActivity.selected_language"

    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, AppLanguageActivity::class.java)
      intent.putExtra(APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY, summaryValue)
      return intent
    }

    fun getAppLanguagePreferenceSummaryValueExtraKey(): String {
      return APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY
    }
  }

  override fun onBackPressed() {
    val message = appLanguageActivityPresenter.getLanguageSelected()
    val intent = Intent()
    intent.putExtra(MESSAGE_APP_LANGUAGE_ARGUMENT_KEY, message)
    setResult(REQUEST_CODE_APP_LANGUAGE, intent)
    finish()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      SELECTED_LANGUAGE_EXTRA_KEY,
      appLanguageActivityPresenter.getLanguageSelected()
    )
  }
}
