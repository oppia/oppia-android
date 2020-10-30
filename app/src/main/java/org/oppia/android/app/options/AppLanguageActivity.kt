package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(APP_LANGUAGE_PREFERENCE_TITLE_EXTRA_KEY)
    prefSummaryValue = if (savedInstanceState == null) {
      intent.getStringExtra(APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY)
    } else {
      savedInstanceState.get(KEY_SELECTED_LANGUAGE) as String
    }
    appLanguageActivityPresenter.handleOnCreate(prefKey, prefSummaryValue)
  }

  companion object {
    internal const val APP_LANGUAGE_PREFERENCE_TITLE_EXTRA_KEY =
      "AppLanguageActivity.app_language_preference_title"
    internal const val APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY =
      "AppLanguageActivity.app_language_preference_summary_value"
    internal const val KEY_SELECTED_LANGUAGE = "SELECTED_LANGUAGE"

    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, AppLanguageActivity::class.java)
      intent.putExtra(APP_LANGUAGE_PREFERENCE_TITLE_EXTRA_KEY, prefKey)
      intent.putExtra(APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY, summaryValue)
      return intent
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
    outState.putString(KEY_SELECTED_LANGUAGE, appLanguageActivityPresenter.getLanguageSelected())
  }
}
