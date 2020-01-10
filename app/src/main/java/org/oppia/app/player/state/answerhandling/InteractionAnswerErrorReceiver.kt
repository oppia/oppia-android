package org.oppia.app.player.state.answerhandling

/**
 * A handler for interaction answers. Handlers can either require an additional user action before the answer can be
 * processed, or they can push the answer directly to a [InteractionAnswerReceiver]. Implementations must indicate
 * whether they require an explicit submit button.
 */
interface InteractionAnswerErrorReceiver {

  /** Will be called to enable and disable submit button on pending answer error updated on real-time and sumbmit time answer change. */
  fun onPendingAnswerError(pendingAnswerError: String?) {}
}
