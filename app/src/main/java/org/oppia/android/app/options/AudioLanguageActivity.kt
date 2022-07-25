package org.oppia.android.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity to change the Default Audio language of the app. */
class AudioLanguageActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var audioLanguageActivityPresenter: AudioLanguageActivityPresenter
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    prefSummaryValue = if (savedInstanceState != null) {
      savedInstanceState.get(AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY) as String
    } else {
      checkNotNull(intent.getStringExtra(AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY)) {
        "Expected $AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY to be in intent extras."
      }
    }
    audioLanguageActivityPresenter.handleOnCreate(prefSummaryValue)
  }

  companion object {
    const val AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY =
      "AudioLanguageActivity.audio_language_preference_summary_value"

    /** Returns a new [Intent] to route to [AudioLanguageActivity]. */
    fun createAudioLanguageActivityIntent(
      context: Context,
      summaryValue: String?
    ): Intent {
      val intent = Intent(context, AudioLanguageActivity::class.java)
      intent.putExtra(AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY, summaryValue)
      return intent
    }

    fun getKeyAudioLanguagePreferenceSummaryValue(): String {
      return AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(
      AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE_EXTRA_KEY,
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
