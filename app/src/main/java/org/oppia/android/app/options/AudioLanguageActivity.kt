package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the Default Audio language of the app. */
class AudioLanguageActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var audioLanguageActivityPresenter: AudioLanguageActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE)
    prefSummaryValue = if (savedInstanceState != null) {
      savedInstanceState.get(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE) as String
    } else {
      intent.getStringExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    }
    audioLanguageActivityPresenter.handleOnCreate(prefKey, prefSummaryValue)
  }

  companion object {
    internal const val KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE = "AUDIO_LANGUAGE_PREFERENCE"
    internal const val KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE =
      "AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE"
    /** Returns a new [Intent] to route to [AudioLanguageActivity]. */
    fun createAudioLanguageActivityIntent(
      context: Context,
      prefKey: String,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, AudioLanguageActivity::class.java)
      intent.putExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_TITLE, prefKey)
      intent.putExtra(KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE, summaryValue)
      return intent
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      KEY_AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE,
      audioLanguageActivityPresenter.getLanguageSelected()
    )
  }

  override fun onBackPressed() {
    val message = audioLanguageActivityPresenter.getLanguageSelected()
    val intent = Intent()
    intent.putExtra(MESSAGE_AUDIO_LANGUAGE_ARGUMENT_KEY, message)
    setResult(REQUEST_CODE_AUDIO_LANGUAGE, intent)
    finish()
  }
}
