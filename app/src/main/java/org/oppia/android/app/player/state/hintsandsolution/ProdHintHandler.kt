package org.oppia.android.app.player.state.hintsandsolution

import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.PendingState
import org.oppia.android.app.model.State
import org.oppia.android.app.player.state.listener.ShowHintAvailabilityListener
import org.oppia.android.app.utility.LifecycleSafeTimerFactory
import javax.inject.Inject

/**
 * [HintHandler] for showing hints to the learner after a period of time in the event they submit a
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
class ProdHintHandler @Inject constructor(
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val fragment: Fragment,
  @DelayShowInitialHintMillis private val delayShowInitialHintMs: Long,
  @DelayShowAdditionalHintsMillis private val delayShowAdditionalHintsMs: Long,
  @DelayShowAdditionalHintsFromWrongAnswerMillis
  private val delayShowAdditionalHintsFromWrongAnswerMs: Long
) : HintHandler {
  private var trackedWrongAnswerCount = 0
  private var previousHelpIndex: HelpIndex = HelpIndex.getDefaultInstance()
  private var hintSequenceNumber = 0
  private var isHintVisibleInLatestState = false

  /** Resets this handler to prepare it for a new state, cancelling any pending hints. */
  override fun reset() {
    trackedWrongAnswerCount = 0
    previousHelpIndex = HelpIndex.getDefaultInstance()
    // Cancel any potential pending hints by advancing the sequence number. Note that this isn't
    // reset to 0 to ensure that all previous hint tasks are cancelled, and new tasks can be
    // scheduled without overlapping with past sequence numbers.
    hintSequenceNumber++
    isHintVisibleInLatestState = false
  }

  /** Hide hint when moving to any previous state. */
  override fun hideHint() {
    (fragment as ShowHintAvailabilityListener).onHintAvailable(
      HelpIndex.getDefaultInstance()
    )
  }

  /**
   * Handles potentially new wrong answers that were submitted, and if so schedules a hint to be
   * shown to the user if hints are available.
   */
  override fun maybeScheduleShowHint(state: State, pendingState: PendingState) {
    if (state.interaction.hintList.isEmpty()) {
      // If this state has no hints to show, do nothing.
      return
    }

    // If hint was visible in the current state show all previous hints coming back to the current
    // state. If any hint was revealed and user move between current and completed states, then
    // show those revealed hints back by making icon visible else use the previous help index.
    if (isHintVisibleInLatestState) {
      if (state.interaction.hintList[previousHelpIndex.hintIndex].hintIsRevealed) {
        (fragment as ShowHintAvailabilityListener).onHintAvailable(
          HelpIndex.newBuilder().setEverythingRevealed(true).build()
        )
      } else {
        (fragment as ShowHintAvailabilityListener).onHintAvailable(
          previousHelpIndex
        )
      }
    }

    // Start showing hints after a wrong answer is submitted or if the user appears stuck (e.g.
    // doesn't answer after some duration). Note that if there's already a timer to show a hint,
    // it will be reset for each subsequent answer.
    val nextUnrevealedHintIndex = getNextHintIndexToReveal(state)
    val isFirstHint = previousHelpIndex.indexTypeCase == HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET
    val wrongAnswerCount = pendingState.wrongAnswerList.size
    if (wrongAnswerCount == trackedWrongAnswerCount) {
      // If no answers have been submitted, schedule a task to automatically help after a fixed
      // amount of time. This will automatically reset if something changes other than answers
      // (e.g. revealing a hint), which may trigger more help to become available.
      if (isFirstHint) {
        // The learner needs to wait longer for the initial hint to show since they need some time
        // to read through and consider the question.
        scheduleShowHint(delayShowInitialHintMs, nextUnrevealedHintIndex)
      } else {
        scheduleShowHint(delayShowAdditionalHintsMs, nextUnrevealedHintIndex)
      }
    } else {
      // See if the learner's new wrong answer justifies showing a hint.
      if (isFirstHint) {
        if (wrongAnswerCount > 1) {
          // If more than one answer has been submitted and no hint has yet been shown, show a
          // hint immediately since the learner is probably stuck.
          showHintImmediately(nextUnrevealedHintIndex)
        }
      } else {
        // Otherwise, always schedule to show a hint on a new wrong answer for subsequent hints.
        scheduleShowHint(
          delayShowAdditionalHintsFromWrongAnswerMs,
          nextUnrevealedHintIndex
        )
      }
      trackedWrongAnswerCount = wrongAnswerCount
    }
  }

  /**
   * Returns the [HelpIndex] of the next hint or solution that hasn't yet been revealed, or
   * default if there is none.
   */
  private fun getNextHintIndexToReveal(state: State): HelpIndex {
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
      HelpIndex.newBuilder().setHintIndex(lastUnrevealedHintIndex).build()
    } else if (solution.hasCorrectAnswer() && !solution.solutionIsRevealed) {
      HelpIndex.newBuilder().setShowSolution(true).build()
    } else {
      HelpIndex.newBuilder().setEverythingRevealed(true).build()
    }
  }

  /**
   * Schedules to allow the hint of the specified index to be shown after the specified delay,
   * cancelling any previously pending hints initiated by calls to this method.
   */
  private fun scheduleShowHint(delayMs: Long, helpIndexToShow: HelpIndex) {
    val targetSequenceNumber = ++hintSequenceNumber
    lifecycleSafeTimerFactory.createTimer(delayMs).observe(
      fragment,
      Observer {
        showHint(targetSequenceNumber, helpIndexToShow)
      }
    )
  }

  /**
   * Immediately indicates the specified hint is ready to be shown, cancelling any previously
   * pending hints initiated by calls to [scheduleShowHint].
   */
  private fun showHintImmediately(helpIndexToShow: HelpIndex) {
    showHint(++hintSequenceNumber, helpIndexToShow)
  }

  private fun showHint(targetSequenceNumber: Int, helpIndexToShow: HelpIndex) {
    // Only finish this timer if no other hints were scheduled and no cancellations occurred.
    if (targetSequenceNumber == hintSequenceNumber) {
      if (previousHelpIndex != helpIndexToShow) {
        // Only indicate the hint is available if its index is actually new (including if it
        // becomes null such as in the case of the solution becoming available).
        (fragment as ShowHintAvailabilityListener).onHintAvailable(helpIndexToShow)
        previousHelpIndex = helpIndexToShow
        isHintVisibleInLatestState = true
      }
    }
  }
}
