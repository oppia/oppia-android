package org.oppia.app.player.state.answerhandling

/**
 * A handler for interaction answer's error receiving to update submit button.
 * Handlers can either require an additional user action before the submit button UI can be updated.
 */
interface InteractionAnswerErrorReceiver {

  /**
   * Called when an error was detected upon answer submission. Implementations are recommended to prevent further answer
   * submission until the pending answer itself changes. The interaction is responsible for displaying the error provided
   * here, not the implementation.
   */
  fun onPendingAnswerError(pendingAnswerError: String?) {}
}
