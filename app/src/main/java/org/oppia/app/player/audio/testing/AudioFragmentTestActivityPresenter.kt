package org.oppia.app.player.audio.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.audio.AudioFragment
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import javax.inject.Inject

/** The presenter for [AudioFragmentTestActivity] */
@ActivityScope
class AudioFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.audio_fragment_test_activity)
    if (getAudioFragment() == null) {
      val audioFragment = AudioFragment.newInstance(TEST_EXPLORATION_ID_5, "END")
      activity.supportFragmentManager.beginTransaction().add(
        R.id.audio_fragment_placeholder,
        audioFragment
      ).commitNow()
    }
  }

  private fun getAudioFragment(): AudioFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.audio_fragment_placeholder) as AudioFragment?
  }
}
