package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val KEY_APP_LANGUAGE_PREFERENCE_TITLE = "APP_LANGUAGE_PREFERENCE"
private const val KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE = "APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE"
private const val KEY_INTERNAL_PROFILE_ID = "INTERNAL_PROFILE_ID"

/** The activity to change the language of the app. */
class AppLanguageActivity : InjectableAppCompatActivity() {
  @Inject lateinit var appLanguageActivityPresenter: AppLanguageActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String
  private var internalProfileId: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(KEY_APP_LANGUAGE_PREFERENCE_TITLE)
    prefSummaryValue = intent.getStringExtra(KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    internalProfileId = intent.getIntExtra(KEY_INTERNAL_PROFILE_ID, -1)
    appLanguageActivityPresenter.handleOnCreate(prefKey, prefSummaryValue,internalProfileId)
  }

  companion object {
    /** Returns a new [Intent] to route to [AppLanguageActivity]. */
    fun createAppLanguageActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String?,
      internalProfileId: Int
    ): Intent {
      val intent = Intent(context, AppLanguageActivity::class.java)
      intent.putExtra(KEY_APP_LANGUAGE_PREFERENCE_TITLE, prefKey)
      intent.putExtra(KEY_APP_LANGUAGE_PREFERENCE_SUMMARY_VALUE, summaryValue)
      intent.putExtra(KEY_INTERNAL_PROFILE_ID, internalProfileId)
      return intent
    }
  }

  override fun onBackPressed() {
    val message = prefSummaryValue
    val intent = Intent()
    intent.putExtra(KEY_MESSAGE_APP_LANGUAGE, message)
    setResult(REQUEST_CODE_APP_LANGUAGE, intent)
    finish()
  }
}
