package org.oppia.android.domain.hintsandsolution

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.PendingState
import org.oppia.android.app.model.State
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.SHOW_SOLUTION
import org.oppia.android.util.data.AsyncDataSubscriptionManager
import org.oppia.android.util.threading.BackgroundDispatcher

/** Implementation of [HintHandler]. */
class HintHandlerImpl private constructor(
  private val delayShowInitialHintMs: Long,
  private val delayShowAdditionalHintsMs: Long,
  private val delayShowAdditionalHintsFromWrongAnswerMs: Long,
  backgroundCoroutineDispatcher: CoroutineDispatcher,
  private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager,
  private val hintMonitor: HintHandler.HintMonitor
) : HintHandler {
  private val backgroundCoroutineScope = CoroutineScope(backgroundCoroutineDispatcher)
  private var trackedWrongAnswerCount = 0
  private var pendingHelpIndex: HelpIndex = HelpIndex.getDefaultInstance()
  private var hintSequenceNumber = 0
  private var isHintVisibleInLatestState = false
  private var lastRevealedHintIndex = -1
  private var latestAvailableHintIndex = -1
  private var solutionIsAvailable = false
  private var solutionIsRevealed = false

  init {
    reset()
  }

  override fun reset() {
    trackedWrongAnswerCount = 0
    pendingHelpIndex = HelpIndex.getDefaultInstance()
    // Cancel tasks rather than resetting to avoid potential cases where previous tasks can carry to
    // the next state.
    cancelPendingTasks()
    isHintVisibleInLatestState = false
    lastRevealedHintIndex = -1
    latestAvailableHintIndex = -1
    solutionIsAvailable = false
    solutionIsRevealed = false
  }

  override fun updateHintStateMachine(state: State, pendingState: PendingState) {
    maybeScheduleShowHint(state, pendingState.wrongAnswerCount)
  }

  override fun revealHint(state: State, hintIndex: Int) {
    val helpIndex = computeCurrentHelpIndex(state)
    check(
      helpIndex.indexTypeCase == AVAILABLE_NEXT_HINT_INDEX
        && helpIndex.availableNextHintIndex == hintIndex
    ) {
      "Cannot reveal hint for current index: ${helpIndex.indexTypeCase} (trying to reveal hint:" +
        " $hintIndex)"
    }

    cancelPendingTasks()
    lastRevealedHintIndex = lastRevealedHintIndex.coerceAtLeast(hintIndex)
  }

  override fun revealSolution(state: State) {
    val helpIndex = computeCurrentHelpIndex(state)
    check(helpIndex.indexTypeCase == SHOW_SOLUTION) {
      "Cannot reveal solution for current index: ${helpIndex.indexTypeCase}"
    }

    cancelPendingTasks()
    solutionIsRevealed = true
  }

  override fun checkHintsOnAnswerSubmission(state: State, wrongAnswerCount: Int) {
    maybeScheduleShowHint(state, wrongAnswerCount)
  }

  override fun getCurrentHelpIndex(state: State): HelpIndex = computeCurrentHelpIndex(state)

  private fun cancelPendingTasks() {
    // Cancel any potential pending hints by advancing the sequence number. Note that this isn't
    // reset to 0 to ensure that all previous hint tasks are cancelled, and new tasks can be
    // scheduled without overlapping with past sequence numbers.
    hintSequenceNumber++
  }

  private fun maybeScheduleShowHint(state: State, wrongAnswerCount: Int) {
    if (state.interaction.hintList.isEmpty()) {
      // If this state has no hints to show, do nothing.
      return
    }

    // If hint was visible in the current state show all previous hints coming back to the current
    // state. If any hint was revealed and user move between current and completed states, then
    // show those revealed hints back by making icon visible else use the previous help index.
    if (isHintVisibleInLatestState) {
      // TODO: replace with internal mechanism
//      if (state.interaction.hintList[previousHelpIndex.hintIndex].hintIsRevealed) {
//        (fragment as ShowHintAvailabilityListener).onHintAvailable(
//          HelpIndex.newBuilder().setEverythingRevealed(true).build()
//        )
//      } else {
//        (fragment as ShowHintAvailabilityListener).onHintAvailable(
//          previousHelpIndex
//        )
//      }
    }

    // Start showing hints after a wrong answer is submitted or if the user appears stuck (e.g.
    // doesn't answer after some duration). Note that if there's already a timer to show a hint,
    // it will be reset for each subsequent answer.
    val nextUnrevealedHintIndex = getNextHintIndexToReveal(state)
    val isFirstHint = pendingHelpIndex.indexTypeCase == HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET
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

  // TODO: consolidate the following 2 methods.

  /**
   * Returns the [HelpIndex] of the next hint or solution that hasn't yet been revealed, or
   * default if there is none.
   */
  private fun getNextHintIndexToReveal(state: State): HelpIndex {
    // Return the index of the first unrevealed hint, or the length of the list if all have been
    // revealed.
    val hintList = state.interaction.hintList
    val solution = state.interaction.solution

    val hasHints = hintList.isNotEmpty()
    val hasHelp = hasHints || solution.hasCorrectAnswer()
    val lastUnrevealedHintIndex = lastRevealedHintIndex + 1

    return if (!hasHelp) {
      HelpIndex.getDefaultInstance()
    } else if (hasHints && lastUnrevealedHintIndex < hintList.size) {
      HelpIndex.newBuilder().setAvailableNextHintIndex(lastUnrevealedHintIndex).build()
    } else if (solution.hasCorrectAnswer() && !solutionIsRevealed) {
      HelpIndex.newBuilder().setShowSolution(true).build()
    } else {
      HelpIndex.newBuilder().setEverythingRevealed(true).build()
    }
  }

  private fun computeCurrentHelpIndex(state: State): HelpIndex {
    // Return the index of the first unrevealed hint, or the length of the list if all have been
    // revealed.
    val hintList = state.interaction.hintList
    val solution = state.interaction.solution

    val hasSolution = solution.hasCorrectAnswer()
    val hasHelp = hintList.isNotEmpty() || hasSolution
    val hasAtLeastOneHintAvailable = latestAvailableHintIndex != -1
    val hasSeenAllAvailableHints = latestAvailableHintIndex == lastRevealedHintIndex
    val hasSeenAllHints = lastRevealedHintIndex == hintList.lastIndex
    val hasViewableSolution = hasSolution && solutionIsAvailable

    return when {
      // No hints or solution are available to be shown.
      !hasHelp -> HelpIndex.getDefaultInstance()

      // The solution has been revealed.
      solutionIsRevealed -> HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()

      // All hints have been shown and a solution can be shown.
      hasSeenAllHints && hasViewableSolution -> HelpIndex.newBuilder().apply {
        showSolution = true
      }.build()

      // All hints have been shown & there is no solution.
      hasSeenAllHints && !hasSolution -> HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()

      // Hints are available (though they may have already been seen).
      hasAtLeastOneHintAvailable -> if (hasSeenAllAvailableHints) {
        HelpIndex.newBuilder().apply {
          latestRevealedHintIndex = lastRevealedHintIndex
        }.build()
      } else {
        HelpIndex.newBuilder().apply {
          availableNextHintIndex = latestAvailableHintIndex
        }.build()
      }

      // No hints are available to be shown yet.
      else -> HelpIndex.getDefaultInstance()
    }
  }

  /**
   * Schedules to allow the hint of the specified index to be shown after the specified delay,
   * cancelling any previously pending hints initiated by calls to this method.
   */
  private fun scheduleShowHint(delayMs: Long, helpIndexToShow: HelpIndex) {
    val targetSequenceNumber = ++hintSequenceNumber
    backgroundCoroutineScope.launch {
      delay(delayMs)
      showHint(targetSequenceNumber, helpIndexToShow)
    }
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
      if (pendingHelpIndex != helpIndexToShow) {
        // TODO: make this better
        if (helpIndexToShow.indexTypeCase == AVAILABLE_NEXT_HINT_INDEX) {
          latestAvailableHintIndex = helpIndexToShow.availableNextHintIndex
        } else if (helpIndexToShow.indexTypeCase == SHOW_SOLUTION) {
          solutionIsAvailable = true
        }

        // Only indicate the hint is available if its index is actually new (including if it
        // becomes null such as in the case of the solution becoming available).
        hintMonitor.onHelpIndexChanged()
        pendingHelpIndex = helpIndexToShow
        isHintVisibleInLatestState = true
      }
    }
  }

  /** Implementation of [HintHandler.Factory]. */
  class FactoryImpl @Inject constructor(
    @DelayShowInitialHintMillis private val delayShowInitialHintMs: Long,
    @DelayShowAdditionalHintsMillis private val delayShowAdditionalHintsMs: Long,
    @DelayShowAdditionalHintsFromWrongAnswerMillis
    private val delayShowAdditionalHintsFromWrongAnswerMs: Long,
    @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
    private val asyncDataSubscriptionManager: AsyncDataSubscriptionManager
  ): HintHandler.Factory {
    override fun create(hintMonitor: HintHandler.HintMonitor): HintHandler {
      return HintHandlerImpl(
        delayShowInitialHintMs,
        delayShowAdditionalHintsMs,
        delayShowAdditionalHintsFromWrongAnswerMs,
        backgroundCoroutineDispatcher,
        asyncDataSubscriptionManager,
        hintMonitor
      )
    }
  }

/*  private var trackedWrongAnswerCount = 0
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
  fun maybeScheduleShowHint(state: State, wrongAnswerCount: Int) {
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
  }

  /**
   * Shows a new hint and solution when the scheduled task with the most recent trackedSequence
   * number completes.
   */
  fun showNewHintAndSolution(state: State, trackedSequenceNumber: Int) {
    if (trackedSequenceNumber == hintSequenceNumber) {
      // Only finish this timer if no other hints were scheduled and no cancellations occurred.
      helpIndex = getNextHintAndSolutionToReveal(state)
    }
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
   * Schedules a future task to show help to the user that will be triggered exactly once after the
   * specified timeout in milliseconds.
   */

  private fun scheduleTaskToShowHelp(
    isCurrentStatePendingState: Boolean
  ) {
//    if (hintState.delayToShowNextHintAndSolution == -1L || !isCurrentStatePendingState) {
      // Do not start timer to show new hints and solutions if the delay is set to -1 or if the
      // current state is not the pending top state.
//      return
//    }
//    backgroundCoroutineScope.launch {
//      delay(hintState.delayToShowNextHintAndSolution)
//      hintAndSolutionTimerCompleted(hintState.hintSequenceNumber)
//    }
  }

  /**
   * Notifies the [HintHandler] that a scheduled task to show new help has finished.
   *
   * @param trackedSequenceNumber the ID used to identify each task scheduled to show help
   * @param state the state on which the hint is shown
   */
  private fun hintAndSolutionTimerCompleted(trackedSequenceNumber: Int) {
//    explorationProgressLock.withLock {
//      explorationProgress.hintState =
//        hintHandler.showNewHintAndSolution(
//          explorationProgress.stateDeck.getCurrentEphemeralState().state,
//          trackedSequenceNumber
//        )
//      if (
//        explorationProgress.hintState.helpIndex.indexTypeCase ==
//        HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX ||
//        explorationProgress.hintState.helpIndex.indexTypeCase ==
//        HelpIndex.IndexTypeCase.SHOW_SOLUTION
//      ) {
        // Only notify the currentState dataProvider the hint or solution is available is actually
        // new.
//        asyncDataSubscriptionManager.notifyChangeAsync(CURRENT_STATE_DATA_PROVIDER_ID)
//      }
//    }
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
  }*/
}
