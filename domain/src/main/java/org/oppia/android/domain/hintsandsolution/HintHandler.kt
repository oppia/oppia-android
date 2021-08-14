package org.oppia.android.domain.hintsandsolution

import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HintState
import org.oppia.android.app.model.State
import javax.inject.Inject
import javax.inject.Singleton

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
@Singleton
class HintHandler @Inject constructor(
  @DelayShowInitialHintMillis private val delayShowInitialHintMs: Long,
  @DelayShowAdditionalHintsMillis private val delayShowAdditionalHintsMs: Long,
  @DelayShowAdditionalHintsFromWrongAnswerMillis
  private val delayShowAdditionalHintsFromWrongAnswerMs: Long
) {
  private var trackedWrongAnswerCount = 0
  private var helpIndex: HelpIndex = HelpIndex.getDefaultInstance()
  private var hintSequenceNumber = 0

  // Negative 1 implies that a new task does not have to be scheduled and so no new help should be
  // revealed. This is so that new tasks are not scheduled in cases when the result of the task is
  // guaranteed to not show a new any new help to the learner, (e.g. when an unrevealed hint or
  // solution is visible).
  private var delayForNextHintAndSolution: Long = -1

  /**
   * Resets this handler to prepare it for a new state, cancelling any pending hints.
   *
   * @return [HintState] with the latest values of the [HintHandler]
   */
  fun reset(): HintState {
    trackedWrongAnswerCount = 0
    helpIndex = HelpIndex.getDefaultInstance()
    // Cancel any potential pending hints by advancing the sequence number. Note that this isn't
    // reset to 0 to ensure that all previous hint tasks are cancelled, and new tasks can be
    // scheduled without overlapping with past sequence numbers.
    ++hintSequenceNumber
    // Set the delay for a new hint and solution so that a new task is not started unintentionally.
    delayForNextHintAndSolution = -1
    return getLatestHintState()
  }

  /** Hide new hint and solution when moving to any previous state. */
  fun hideHintsAndSolution() {
    // Cancel any potential pending hints by advancing the sequence number.
    hintSequenceNumber++
  }

  /**
   * Handles potentially new wrong answers that were submitted and cases when the user is probably
   * stuck on a particular state, and if so schedules a hint or solution to be shown to the user if
   * hints and solution are available.
   *
   * @return [HintState] with the latest values of the [HintHandler]
   */
  fun maybeScheduleShowHint(state: State, wrongAnswerCount: Int): HintState {
    if (state.interaction.hintList.isEmpty()) {
      // If this state has no hints and solution to show, return a default instance of HintState.
      return HintState.getDefaultInstance()
    }

    delayForNextHintAndSolution = when (helpIndex.indexTypeCase) {
      HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET -> {
        trackedWrongAnswerCount = wrongAnswerCount
        // The learner needs to wait longer for the initial hint to show since they need some time
        // to read through and consider the question.
        if (wrongAnswerCount == 0 || wrongAnswerCount == 1) {
          // If less than two answers are submitted, schedule a task to automatically help after a
          // fixed amount of time. This will automatically reset if something changes
          // (e.g. submitting an answer).
          hintSequenceNumber++
          delayShowInitialHintMs
        } else {
          // Update the sequence number to cancel any pending tasks ans show new help immediately.
          hintSequenceNumber++
          // Update helpIndex with the next available help
          helpIndex = getNextHintAndSolutionToReveal(state)
          // Set delayForNextHintAndSolution to -1 so that no new tasks are scheduled.
          -1
        }
      }
      HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX -> {
        if (trackedWrongAnswerCount != wrongAnswerCount) {
          // If a new wrong answer is submitted, schedule a task to reveal a new hint.
          trackedWrongAnswerCount = wrongAnswerCount
          hintSequenceNumber++
          delayShowAdditionalHintsFromWrongAnswerMs
        } else {
          // Otherwise, always schedule to show a hint on a new wrong answer for subsequent hints.
          hintSequenceNumber++
          delayShowAdditionalHintsMs
        }
      }
      HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX -> {
        // Do not schedule a new task until the last help has been revealed.
        -1
      }
      else -> {
        // Update with the tracked answer count if there is a new answer.
        trackedWrongAnswerCount = wrongAnswerCount
        // The else branch implies that the helpIndex.indexTypeCase is either equal to SHOW_SOLUTION
        // or EVERYTHING_IS_REVEALED, in either case do not schedule a new task because no more help
        // is available.
        -1
      }
    }
    return getLatestHintState()
  }

  /**
   * Shows a new hint and solution when the scheduled task with the most recent trackedSequence
   * number completes.
   *
   * @return [HintState] with the latest values of the [HintHandler]
   */
  fun showNewHintAndSolution(state: State, trackedSequenceNumber: Int): HintState {
    if (trackedSequenceNumber == hintSequenceNumber) {
      // Only finish this timer if no other hints were scheduled and no cancellations occurred.
      helpIndex = getNextHintAndSolutionToReveal(state)
    }
    return getLatestHintState()
  }

  /** Notifies the HintHandler that the visible hint has been revealed by the learner. */
  fun notifyHintIsRevealed(index: Int) {
    if (
      helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX &&
      helpIndex.availableNextHintIndex == index
    ) {
      helpIndex = helpIndex.toBuilder().setLatestRevealedHintIndex(index).build()
    }
  }

  /** Notifies the HintHandler that the visible solution has been revealed by the learner. */
  fun notifySolutionIsRevealed() {
    if (helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.SHOW_SOLUTION) {
      helpIndex =
        helpIndex.toBuilder().setEverythingRevealed(true).build()
    }
  }

  /**
   * Returns the [HelpIndex] of the next hint or solution that hasn't yet been revealed, or
   * default if there is none.
   */
  private fun getNextHintAndSolutionToReveal(state: State): HelpIndex {
    // Return the index of the first unrevealed hint, or the length of the list if all have been
    // revealed.
    val hintList = state.interaction.hintList
    val solution = state.interaction.solution

    val hasHelp = hintList.isNotEmpty() || solution.hasCorrectAnswer()
    val lastUnrevealedHintIndex = hintList.indices.filterNot { idx ->
      hintList[idx].hintIsRevealed
    }.firstOrNull()

    return if (!hasHelp) {
      HelpIndex.getDefaultInstance()
    } else if (lastUnrevealedHintIndex != null) {
      HelpIndex.newBuilder().setAvailableNextHintIndex(lastUnrevealedHintIndex).build()
    } else if (solution.hasCorrectAnswer() && !solution.solutionIsRevealed) {
      HelpIndex.newBuilder().setShowSolution(true).build()
    } else {
      HelpIndex.newBuilder().setEverythingRevealed(true).build()
    }
  }

  private fun getLatestHintState(): HintState {
    return HintState.newBuilder().apply {
      hintSequenceNumber = this@HintHandler.hintSequenceNumber
      trackedAnswerCount = this@HintHandler.trackedWrongAnswerCount
      helpIndex = this@HintHandler.helpIndex
      delayToShowNextHintAndSolution = this@HintHandler.delayForNextHintAndSolution
    }.build()
  }
}
