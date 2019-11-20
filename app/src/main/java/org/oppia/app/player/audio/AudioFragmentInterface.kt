package org.oppia.app.player.audio

import androidx.lifecycle.LiveData

/** Interface to interact with AudioFragment from [StateFragment]. */
interface AudioFragmentInterface {
  /**
   * Used to set AudioFragment's VoiceoverMappings with given stateId and contentId.
   * If no contentId is provided, AudioFragment will use state.content.contentId as default.
   */
  fun setVoiceoverMappingsByState(stateId: String, contentId: String? = null)

  /** Allows [StateFragment] to get the current play status of the audio player. */
  fun getCurrentPlayStatus(): LiveData<AudioViewModel.UiAudioPlayStatus>

  /** Used to automatically start playing audio when switching states. */
  fun playAudio()
}