package org.oppia.android.app.player.state.answerhandling

import org.oppia.android.app.model.RawUserAnswer
import org.oppia.android.app.model.UserAnswer

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

  /** Returns whether this handler automatically navigates the user to a later state, including answer submission. */
  fun isAutoNavigating(): Boolean = false

  /** Return the current answer's error messages  if not valid else return null. */
  fun checkPendingAnswerError(category: AnswerErrorCategory): String? {
    return null
  }

  /** Returns the pending answer awaiting submission by the user. */
  fun getPendingAnswer(): UserAnswer? {
    return null
  }

  /**
   * Returns a raw representation of the current answer entered by the user which is used to retain
   * state on configuration changes.
   */
  fun getRawUserAnswer(): RawUserAnswer {
    return RawUserAnswer.getDefaultInstance()
  }
}

/**
 * A callback that will be called by [InteractionAnswerHandler]s when a user submits an answer. To be implemented by
 * the parent fragment of the handler.
 */
interface InteractionAnswerReceiver {
  fun onAnswerReadyForSubmission(answer: UserAnswer)
}

/** Categories of errors that can be inferred from a pending answer.  */
enum class AnswerErrorCategory {
  /** Corresponds to errors that may be found while the user is trying to input an answer.  */
  REAL_TIME,
  /** Corresponds to errors that may be found only when a user tries to submit an answer.  */
  SUBMIT_TIME
}
