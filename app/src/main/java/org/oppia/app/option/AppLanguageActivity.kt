package org.oppia.app.option

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val APP_LANGUAGE_PREFERENCE_KEY = "APP_LANGUAGE_PREFERENCE_KEY"

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  @Inject lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(APP_LANGUAGE_PREFERENCE_KEY)
    prefSummaryValue = intent.getStringExtra(PREFERENCE_SUMMARY_VALUE)
    appLanguageActivityPresenter.handleOnCreate(prefKey, prefSummaryValue)
  }

  companion object {
    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String
    ): Intent {
      val intent = Intent(context, AppLanguageActivity::class.java)
      intent.putExtra(APP_LANGUAGE_PREFERENCE_KEY, prefKey)
      intent.putExtra(PREFERENCE_SUMMARY_VALUE, summaryValue)
      return intent
    }
  }
}
