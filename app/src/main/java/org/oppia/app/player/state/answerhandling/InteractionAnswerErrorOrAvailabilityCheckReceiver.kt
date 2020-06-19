package org.oppia.app.player.state.answerhandling

/**
 * A handler for interaction answer's error receiving and change in answer availability status to update submit button.
 * Handlers can either require an additional user action before the submit button UI can be updated.
 */
interface InteractionAnswerErrorOrAvailabilityCheckReceiver {

  /**
   * Called when an error was detected upon answer submission or if the input answer availability changes.
   * Implementations are recommended to prevent further answer submission until the pending answer itself changes.
   * The interaction is responsible for displaying the error provided here, not the implementation.
   */
  fun onPendingAnswerErrorOrAvailabilityCheck(pendingAnswerError: String?, inputAnswerAvailable: Boolean) {}
}
