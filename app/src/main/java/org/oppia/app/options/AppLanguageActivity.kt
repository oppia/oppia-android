package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val KEY_APP_LANGUAGE_PREFERENCE_TITLE = "APP_LANGUAGE_PREFERENCE"
private const val KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE = "APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE"
private const val KEY_SELECTED_LANGUAGE = "SELECTED_LANGUAGE"

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  @Inject lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(KEY_APP_LANGUAGE_PREFERENCE_TITLE)
    prefSummaryValue = if (savedInstanceState == null) {
      intent.getStringExtra(KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    } else {
      savedInstanceState.get(KEY_SELECTED_LANGUAGE) as String
    }
    appLanguageActivityPresenter.handleOnCreate(prefKey, prefSummaryValue)
    title = getString(R.string.title_app_language)
  }

  companion object {
    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, AppLanguageActivity::class.java)
      intent.putExtra(KEY_APP_LANGUAGE_PREFERENCE_TITLE, prefKey)
      intent.putExtra(KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE, summaryValue)
      return intent
    }
  }

  override fun onBackPressed() {
    val message = appLanguageActivityPresenter.getLanguageSelected()
    val intent = Intent()
    intent.putExtra(KEY_MESSAGE_APP_LANGUAGE, message)
    setResult(REQUEST_CODE_APP_LANGUAGE, intent)
    finish()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(KEY_SELECTED_LANGUAGE, appLanguageActivityPresenter.getLanguageSelected())
  }
}
