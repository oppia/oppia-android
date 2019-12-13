package org.oppia.app.player.state.answerhandling

import org.oppia.app.model.UserAnswer

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
   * Returns whether this handler requires an explicit error check. Note that this is expected to be invariant for the
   * lifetime of this handler instance. Note also that handlers that override this value to be true also need to override
   * [setPendingAnswerError] in order to return errors at the right time.
   */
  fun isExplicitErrorCheckRequired(): Boolean = false
  fun onAnswerRealTimeError(category: AnswerErrorCategory){
  }

  /**
   * Returns the current answer's error message if the current answer is invalid, otherwise null. Note that this method
   * is only assumed to be used if [isExplicitErrorCheckRequired] returns true.
   */
  fun hasPendingAnswerError(): Boolean = false

  /** Set the current answer's error messages  if not valid else return null. */
  fun setPendingAnswerError(category: AnswerErrorCategory): String? {
    return null
  }

  /** Return the current answer's error messages  if not valid else return null. */
  fun getPendingAnswerError(): String? {
    return null
  }

  /** Return the current answer that is ready for handling. */
  fun getPendingAnswer(): UserAnswer?{
    return null
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