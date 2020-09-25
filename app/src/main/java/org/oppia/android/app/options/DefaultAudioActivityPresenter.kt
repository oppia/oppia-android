package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [DefaultAudioActivity]. */
@ActivityScope
class DefaultAudioActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  private lateinit var prefSummaryValue: String

  fun handleOnCreate(prefKey: String, prefValue: String) {
    activity.setContentView(R.layout.default_audio_activity)
    setLanguageSelected(prefValue)
    if (getDefaultAudioFragment() == null) {
      val defaultAudioFragment = DefaultAudioFragment.newInstance(prefKey, prefValue)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.default_audio_fragment_container, defaultAudioFragment).commitNow()
    }
  }

  fun setLanguageSelected(audioLanguage: String) {
    prefSummaryValue = audioLanguage
  }

  fun getLanguageSelected(): String {
    return prefSummaryValue
  }

  private fun getDefaultAudioFragment(): DefaultAudioFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.default_audio_fragment_container) as DefaultAudioFragment?
  }
}
