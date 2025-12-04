package org.oppia.android.domain.hintsandsolution

import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State

/**
 * Handler for showing hints to the learner after a period of time in the event they submit a
 * wrong answer.
 *
 * Note that the exact behavior of when a hint or solution is made available is up to the
 * implementation, but it's assumed that:
 * 1. This class is the sole decision maker for whether a hint is available or revealed (and ditto
 *    for solutions)
 * 2. Hints must be viewed in order
 * 3. Later hints are not available until all previous hints have been revealed
 * 4. The solution cannot be revealed until all previous hints have been revealed
 *
 * Implementations of this class are not safe to access across multiple threads, and will block the
 * calling thread.
 */
interface HintHandler {

  /**
   * Starts watching for potential hints to be shown (e.g. if a user doesn't submit an answer after
   * a certain amount of time). This is meant to only be called once at the beginning of a state.
   */
  suspend fun startWatchingForHintsInNewState(state: State)

  /**
   * Starts watching for potential hints to be shown when the exploration is resumed (e.g. if a
   * user doesn't submit an answer after a certain amount of time). This is meant to only be called
   * once when the the exploration is resumed.
   *
   * @param trackedWrongAnswerCount the count of wrong answers saved in the checkpoint
   * @param helpIndex the cached state of hints/solution from the checkpoint
   * @param state the restored pending state
   */
  suspend fun resumeHintsForSavedState(
    trackedWrongAnswerCount: Int,
    helpIndex: HelpIndex,
    state: State
  )

  /**
   * Indicates that the current state has ended and a new one should start being monitored. This
   * will cancel any previously pending background operations and potentially starts new ones
   * corresponding to the new state.
   */
  suspend fun finishState(newState: State)

  /**
   * Notifies the handler that a wrong answer was submitted.
   *
   * @param wrongAnswerCount the total number of wrong answers submitted to date
   */
  suspend fun handleWrongAnswerSubmission(wrongAnswerCount: Int)

  /** Notifies the handler that the user revealed a hint corresponding to the specified index. */
  suspend fun viewHint(hintIndex: Int)

  /** Notifies the handler that the user revealed the the solution for the current state. */
  suspend fun viewSolution()

  /**
   * Notifies the handler that the user navigated to a previously completed state. This will
   * potentially cancel any ongoing operations to avoid the hint counter continuing when navigating
   * an earlier state.
   */
  suspend fun navigateToPreviousState()

  /**
   * Notifies the handler that the user has navigated back to the latest (pending) state. Note that
   * this may resume background operations, but it does not guarantee that those start at the same
   * time that they left off at (counters may be reset upon returning to the latest state).
   */
  suspend fun navigateBackToLatestPendingState()

  /**
   * Returns a [StateFlow] of the [HelpIndex] corresponding to the current pending state which can
   * be used to actively monitor the index state, if desired.
   */
  fun getCurrentHelpIndex(): StateFlow<HelpIndex>

  /**
   * Factory for creating new [HintHandler]s.
   *
   * Note that instances of this class are injectable in the application component.
   */
  interface Factory {
    /**
     * Creates a new [HintHandler].
     *
     * @return a new [HintHandler] for managing hint/solution state for a specific play session
     */
    fun create(): HintHandler
  }
}
