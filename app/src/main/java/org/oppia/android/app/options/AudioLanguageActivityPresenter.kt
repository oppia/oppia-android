package org.oppia.android.app.options

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityScope
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.AudioLanguageActivityResultBundle
import org.oppia.android.app.model.ProfileId
import org.oppia.android.databinding.AudioLanguageActivityBinding
import org.oppia.android.util.extensions.putProtoExtra
import javax.inject.Inject

/** The presenter for [AudioLanguageActivity]. */
@ActivityScope
class AudioLanguageActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  private lateinit var audioLanguage: AudioLanguage

  /** Handles when the activity is first created. */
  fun handleOnCreate(audioLanguage: AudioLanguage, profileId: ProfileId) {
    this.audioLanguage = audioLanguage

    val binding: AudioLanguageActivityBinding =
      DataBindingUtil.setContentView(activity, R.layout.audio_language_activity)
    binding.audioLanguageToolbar.setNavigationOnClickListener {
      finishWithResult()
    }
    if (getAudioLanguageFragment() == null) {
      val audioLanguageFragment = AudioLanguageFragment.newInstance(audioLanguage, profileId)
      activity.supportFragmentManager.beginTransaction()
        .add(R.id.audio_language_fragment_container, audioLanguageFragment).commitNow()
    }
  }

  /** Updates the currently selected [AudioLanguage] to the specified [audioLanguage]. */
  fun setLanguageSelected(audioLanguage: AudioLanguage) {
    this.audioLanguage = audioLanguage
  }

  /** Returns the current [AudioLanguage] selected in the activity. */
  fun getLanguageSelected(): AudioLanguage = audioLanguage

  /**
   * Finishes the current activity with a result (specifically, an intent result with
   * [AudioLanguageActivityResultBundle] populated with the [AudioLanguage] that was selected in the
   * activity).
   */
  fun finishWithResult() {
    val intent = Intent().apply {
      val result = AudioLanguageActivityResultBundle.newBuilder().apply {
        this.audioLanguage = this@AudioLanguageActivityPresenter.audioLanguage
      }.build()
      putProtoExtra(MESSAGE_AUDIO_LANGUAGE_RESULTS_KEY, result)
    }

    activity.setResult(RESULT_OK, intent)
    activity.finish()
  }

  private fun getAudioLanguageFragment(): AudioLanguageFragment? {
    return activity.supportFragmentManager
      .findFragmentById(R.id.audio_language_fragment_container) as? AudioLanguageFragment
  }
}
