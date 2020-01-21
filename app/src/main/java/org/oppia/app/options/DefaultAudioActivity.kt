package org.oppia.app.options

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

private const val AUDIO_LANGUAGE_PREFERENCE_KEY = "AUDIO_LANGUAGE_PREFERENCE_KEY"
private const val AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE = "AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE"

/** The activity to change the Default Audio language of the app. */
class DefaultAudioActivity : InjectableAppCompatActivity() {

  @Inject lateinit var defaultAudioActivityPresenter: DefaultAudioActivityPresenter
  private lateinit var prefKey: String
  private lateinit var prefSummaryValue: String

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    prefKey = intent.getStringExtra(AUDIO_LANGUAGE_PREFERENCE_KEY)
    prefSummaryValue = intent.getStringExtra(AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE)
    defaultAudioActivityPresenter.handleOnCreate(prefKey, prefSummaryValue)
  }

  companion object {
    /** Returns a new [Intent] to route to [DefaultAudioActivity]. */
    fun createDefaultAudioActivityIntent(context: Context, prefKey: String, summaryValue: String): Intent {
      val intent = Intent(context, DefaultAudioActivity::class.java)
      intent.putExtra(AUDIO_LANGUAGE_PREFERENCE_KEY, prefKey)
      intent.putExtra(AUDIO_LANGUAGE_PREFERENCE_SUMMARY_VALUE, summaryValue)
      return intent
    }
  }
}
