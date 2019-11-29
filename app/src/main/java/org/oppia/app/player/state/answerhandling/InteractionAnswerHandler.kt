package org.oppia.app.player.state.answerhandling

import org.oppia.app.model.UserAnswer
import org.oppia.app.parser.StringToFractionParser.AnswerErrorCategory

/**
 * A handler for interaction answers. Handlers can either require an additional user action before the answer can be
 * processed, or they can push the answer directly to a  [InteractionAnswerReceiver]. Implementations must indicate
 * whether they require an explicit submit button.
 */
interface InteractionAnswerHandler {
  /**
   * Returns whether this handler requires explicit answer submission. Note that this is expected to be an invariant for
   * the lifetime of this handler instance.
   */
  fun isExplicitAnswerSubmissionRequired(): Boolean = true

  /**
   * Returns whether this handler requires explicit error check required. Note that this is expected to be an invariant for
   * the lifetime of this handler instance.
   */
  fun isExplicitErrorCheckRequired(): Boolean = false

  /** Return the current answer's error messages  if not valid else return null. */
  fun getPendingAnswerError(category: AnswerErrorCategory): String? {
    return null
  }

  /** Return the current answer that is ready for handling. */
  fun getPendingAnswer(): UserAnswer
}

/**
 * A callback that will be called by [InteractionAnswerHandler]s when a user submits an answer. To be implemented by
 * the parent fragment of the handler.
 */
interface InteractionAnswerReceiver {
  fun onAnswerReadyForSubmission(answer: UserAnswer)
}
