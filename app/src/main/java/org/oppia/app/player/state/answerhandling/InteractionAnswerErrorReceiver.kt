package org.oppia.app.player.state.answerhandling

/**
 * A handler for interaction answers error receiving to update submit button.
 * Handlers can either require an additional user action before the submit button UI can be updated.
 */
interface InteractionAnswerErrorReceiver {

  /** Will be called to enable and disable submit button on pending answer error updated on real-time and sumbmit time answer change. */
  fun onPendingAnswerError(pendingAnswerError: String?) {}
}
