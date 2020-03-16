package org.oppia.app.player.state.hintsandsolution

import org.oppia.app.model.State

/** Manager for updating audio state within the state player. */
interface HintsAndSolutionUiManager {
  /**
   * Used to set the state and explorationId for the audio player
   *
   * @param newState: New State for the audio player to get recorded voiceovers from.
   * @param explorationId: Used to build voiceover uri.
   */
  fun setStateAndExplorationId(newState: State, explorationId: String)

}
