package org.oppia.android.domain.hintsandsolution

import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State

/**
 * Handler for showing hints to the learner after a period of time in the event they submit a
 * wrong answer.
 *
 * # Flow chart for when hints are shown
 *
 *            Submit 1st              Submit wrong
 *            wrong answer            answer
 *              +---+                   +---+
 *              |   |                   |   |
 *              |   v                   |   v
 *            +-+---+----+            +-+---+-----+           +----------+
 *     Initial| No       | Wait 60s   |           | View hint | Hint     |
 *     state  | hint     +----------->+ Hint      +---------->+ consumed |
 *     +----->+ released | or, submit | available | Wait 30s  |          |
 *            |          | 2nd wrong  |           +<----------+          |
 *            +----------+ answer     +----+------+           +----+-----+
 *                                         ^                       |
 *                                         |Wait 10s               |
 *                                         |                       |
 *                                    +----+------+                |
 *                               +--->+ No        | Submit wrong   |
 *                   Submit wrong|    | hint      | answer         |
 *                   answer      |    | available +<---------------+
 *                               +----+           |
 *                                    +-----------+
 *
 * # Logic for selecting a hint
 *
 * Hints are selected based on the availability of hints to show, and any previous hints that have
 * been shown. A new hint will only be made available if its previous hint has been viewed by the
 * learner. Hints are always shown in order. If all hints have been exhausted and viewed by the
 * user, then the 'hint available' state in the diagram above will trigger the solution to be
 * made available to view, if a solution is present. Once the solution is viewed by the learner,
 * they will reach a terminal state for hints and no additional hints or solutions will be made
 * available.
 *
 * Implementations of this class are safe to access across multiple threads, but care must be taken
 * when calling back into this class from [HintMonitor] since that could cause deadlocks. Note also
 * that this class may block the calling thread. While the operations this class performs
 * synchronously should be quick, care should be taken about heavy usage of this class on the main
 * thread as it may introduce janky behavior.
 */
interface HintHandler {

  /**
   * Starts watching for potential hints to be shown (e.g. if a user doesn't submit an answer after
   * a certain amount of time). This is meant to only be called once at the beginning of a state.
   */
  fun startWatchingForHintsInNewState(state: State)

  /**
   * Indicates that the current state has ended and a new one should start being monitored. This
   * will cancel any previously pending background operations and potentially starts new ones
   * corresponding to the new state.
   */
  fun finishState(newState: State)

  /**
   * Notifies the handler that a wrong answer was submitted.
   *
   * @param wrongAnswerCount the total number of wrong answers submitted to date
   */
  fun handleWrongAnswerSubmission(wrongAnswerCount: Int)

  /** Notifies the handler that the user revealed a hint corresponding to the specified index. */
  fun viewHint(hintIndex: Int)

  /** Notifies the handler that the user revealed the the solution for the current state. */
  fun viewSolution()

  /**
   * Notifies the handler that the user navigated to a previously completed state. This will
   * potentially cancel any ongoing operations to avoid the hint counter continuing when navigating
   * an earlier state.
   */
  fun navigateToPreviousState()

  /**
   * Notifies the handler that the user has navigated back to the latest (pending) state. Note that
   * this may resume background operations, but it does not guarantee that those start at the same
   * time that they left off at (counters may be reset upon returning to the latest state).
   */
  fun navigateBackToLatestPendingState()

  /** Returns the [HelpIndex] corresponding to the current pending state. */
  fun getCurrentHelpIndex(): HelpIndex

  /** A callback interface for monitoring changes to [HintHandler]. */
  interface HintMonitor {
    /**
     * Called when the [HelpIndex] managed by the [HintHandler] has changed. To get the latest
     * state, call [HintHandler.getCurrentHelpIndex]. Note that this method may be called on a
     * background thread.
     */
    fun onHelpIndexChanged()
  }

  /**
   * Factory for creating new [HintHandler]s.
   *
   * Note that instances of this class are injectable in the application component.
   */
  interface Factory {
    /**
     * Creates a new [HintHandler].
     *
     * @param hintMonitor a [HintMonitor] to observe async changes to hints/solution
     * @return a new [HintHandler] for managing hint/solution state for a specific play session
     */
    fun create(hintMonitor: HintMonitor): HintHandler
  }
}
