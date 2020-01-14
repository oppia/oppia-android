package org.oppia.app.player.state.answerhandling

/**
 * A handler for interaction answer's error receiving to update submit button.
 * Handlers can either require an additional user action before the submit button UI can be updated.
 */
interface InteractionAnswerErrorReceiver {

  /** Will be called to enable and disable submit button while pending answer error is updated by real-time and submit-time answer change. */
  fun onPendingAnswerError(pendingAnswerError: String?) {}
}
