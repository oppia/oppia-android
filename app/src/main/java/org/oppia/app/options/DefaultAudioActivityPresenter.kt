package org.oppia.app.options

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [DefaultAudioActivity]. */
@ActivityScope
class DefaultAudioActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  private lateinit var prefSummaryValue: String

  fun handleOnCreate(prefKey: String, prefValue: String) {
    activity.setContentView(R.layout.default_audio_activity)
    setLanguageSelected(prefValue)
    val defaultAudioFragment = DefaultAudioFragment.newInstance(prefKey, prefValue)
    activity.supportFragmentManager.beginTransaction()
      .add(R.id.default_audio_fragment_container, defaultAudioFragment).commitNow()
  }

  fun setLanguageSelected(audioLanguage: String) {
    prefSummaryValue = audioLanguage
  }

  fun getLanguageSelected(): String {
    return prefSummaryValue
  }
}
