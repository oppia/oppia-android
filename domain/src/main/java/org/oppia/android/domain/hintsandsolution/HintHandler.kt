package org.oppia.android.domain.hintsandsolution

import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.PendingState
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
 */
interface HintHandler {
  /** Resets this handler to prepare it for a new state, cancelling any pending hints. */
  fun reset()

  // Reset the hint state if pending top state has changed. Note that this handles
  // several different cases of ensuring hints/solution are reset when moving to a new
  // state.
  fun finishState() = reset()

  fun updateHintStateMachine(state: State, pendingState: PendingState)

  fun revealHint(state: State, hintIndex: Int)

  fun revealSolution(state: State)

  /**
   * Starts watching for potential hints to be shown (e.g. if a user doesn't submit an answer after
   * a certain amount of time). This is meant to only be called once at the beginning of a state.
   */
//  fun startWatchingForHintsInNewState(state: State)

  /**
   * Handles potentially new wrong answers that were submitted, and if so schedules a hint to be
   * shown to the user if hints are available.
   */
  fun checkHintsOnAnswerSubmission(state: State, wrongAnswerCount: Int)

  // TODO: replace with notification mechanism (maybe a DataProvider?)
  fun getCurrentHelpIndex(state: State): HelpIndex

  interface HintMonitor {
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
