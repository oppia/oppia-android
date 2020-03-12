package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE = "AUDIO_LANGUAGE_PREFERENCE"
private const val KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE = "AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE"

/** The activity to change the Default Audio language of the app. */
class DefaultAudioActivity : InjectableAppCompatActivity() {

  @Inject lateinit var defaultAudioActivityPresenter: DefaultAudioActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE)
    prefSummaryValue = intent.getStringExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    defaultAudioActivityPresenter.handleOnCreate(prefKey, prefSummaryValue)
  }

  companion object {
    /** Returns a new [Intent] to route to [DefaultAudioActivity]. */
    fun createDefaultAudioActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, DefaultAudioActivity::class.java)
      intent.putExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE, prefKey)
      intent.putExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE, summaryValue)
      return intent
    }
  }

  override fun onBackPressed() {
    val message = defaultAudioActivityPresenter.getLanguageSelected()
    val intent = Intent()
    intent.putExtra(KEY_MESSAGE_AUDIO_LANGUAGE, message)
    setResult(REQUEST_CODE_AUDIO_LANGUAGE, intent)
    finish()
  }
}
