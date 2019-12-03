package org.oppia.app.player.audio

import androidx.lifecycle.LiveData

/** Manager for updating audio state within the state player. */
interface AudioUiManager {
  /**
   * Used to set AudioFragment's VoiceoverMappings with given stateId and contentId.
   * If no contentId is provided, AudioFragment will use state.content.contentId as default.
   */
  fun setVoiceoverMappings(explorationId: String, stateId: String, contentId: String? = null)

  /** Allows [StateFragment] to get the current play status of the audio player. */
  fun getCurrentPlayStatus(): LiveData<AudioViewModel.UiAudioPlayStatus>

  /** Used to automatically start playing audio when switching states. */
  fun playAudio()

  /** Used to pause audio when hiding [AudioFragment]. */
  fun pauseAudio()
}
