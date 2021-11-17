package org.oppia.android.app.options

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [AudioLanguageActivity]. */
@ActivityScope
class AudioLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  private lateinit var prefSummaryValue: String

  fun handleOnCreate(prefKey: String, prefValue: String) {
    activity.setContentView(R.layout.audio_language_activity)
    val toolbar: androidx.appcompat.widget.Toolbar = activity.findViewById(R.id.audio_language_toolbar)
    toolbar.setNavigationOnClickListener {
      val intent = Intent().apply {
        putExtra(MESSAGE_AUDIO_LANGUAGE_ARGUMENT_KEY, prefSummaryValue)
      }
      (activity as AudioLanguageActivity).setResult(REQUEST_CODE_AUDIO_LANGUAGE, intent)
      activity.finish()
    }
    setLanguageSelected(prefValue)
    if (getAudioLanguageFragment() == null) {
      val audioLanguageFragment = AudioLanguageFragment.newInstance(prefKey, prefValue)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.audio_language_fragment_container, audioLanguageFragment).commitNow()
    }
  }

  fun setLanguageSelected(audioLanguage: String) {
    prefSummaryValue = audioLanguage
  }

  fun getLanguageSelected(): String {
    return prefSummaryValue
  }

  private fun getAudioLanguageFragment(): AudioLanguageFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.audio_language_fragment_container) as AudioLanguageFragment?
  }
}
