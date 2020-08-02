package org.oppia.app.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.app.player.audio.AudioFragment
import javax.inject.Inject

/** The presenter for [AudioFragmentTestActivity] */
@ActivityScope
class AudioFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate(internalProfileId: Int) {
    activity.setContentView(R.layout.audio_fragment_test_activity)
    if (getAudioFragment() == null) {

      val audioFragment: AudioFragment = AudioFragment.newInstance(internalProfileId)

      activity
        .supportFragmentManager
        .beginTransaction()
        .add(
          R.id.audio_fragment_placeholder,
          audioFragment
        ).commitNow()
      val state = State.newBuilder()
        .setContent(SubtitledHtml.newBuilder().setContentId("content"))
        .putRecordedVoiceovers(
          "content",
          VoiceoverMapping.newBuilder()
            .putVoiceoverMapping(
              "en",
              Voiceover.newBuilder().setFileName("content-en-057j51i2es.mp3").build()
            )
            .putVoiceoverMapping(
              "es",
              Voiceover.newBuilder().setFileName("content-es-i0nhu49z0q.mp3").build()
            )
            .putVoiceoverMapping(
              "hi",
              Voiceover.newBuilder().setFileName("content-es-i0nhu49z0q.mp3").build()
            )
            .build()
        ).build()
      audioFragment.setStateAndExplorationId(state, "2mzzFVDLuAj8")
      audioFragment.loadMainContentAudio(false)
    }
  }

  private fun getAudioFragment(): AudioFragment? {
    return activity
      .supportFragmentManager
      .findFragmentById(R.id.audio_fragment_placeholder) as AudioFragment?
  }
}
