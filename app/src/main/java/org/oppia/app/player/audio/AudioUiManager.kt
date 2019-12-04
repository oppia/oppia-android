package org.oppia.app.player.audio

import org.oppia.app.model.State

/** Manager for updating audio state within the state player. */
interface AudioUiManager {
  /**
   * Used to set the state and explorationId for the audio player
   *
   * @param newState: New State for the audio player to get recorded voiceovers from.
   * @param explorationId: Used to build voiceover uri.
   */
  fun setStateAndExplorationId(newState: State, explorationId: String)

  /**
   * Loads audio for media player with content id.
   *
   * @param contentId: Used to get a specific Voiceover. If null, state.content.contentId is used as default.
   * @param allowAutoPlay: If false, audio is guaranteed not to be autoPlayed.
   */
  fun loadAudio(contentId: String?, allowAutoPlay: Boolean)

  /** Used to pause audio when hiding [AudioFragment]. */
  fun pauseAudio()
}
