package org.oppia.android.domain.hintsandsolution

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.EVERYTHING_REVEALED
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX
import org.oppia.android.app.model.HelpIndex.IndexTypeCase.SHOW_SOLUTION
import org.oppia.android.app.model.State
import org.oppia.android.util.threading.BackgroundDispatcher
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Production implementation of [HintHandler] that implements hints & solutions in parity with the
 * Oppia web platform.
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
class HintHandlerProdImpl private constructor(
  private val delayShowInitialHintMs: Long,
  private val delayShowAdditionalHintsMs: Long,
  private val delayShowAdditionalHintsFromWrongAnswerMs: Long,
  private val backgroundCoroutineDispatcher: CoroutineDispatcher
) : HintHandler {
  private val helpIndexFlow by lazy { MutableStateFlow(HelpIndex.getDefaultInstance()) }

  private var trackedWrongAnswerCount = 0
  private lateinit var pendingState: State
  private var hintSequenceNumber = 0
  private var lastRevealedHintIndex = -1

  private var latestAvailableHintIndex = -1
  private var solutionIsAvailable = false
  private var solutionIsRevealed = false

  override suspend fun startWatchingForHintsInNewState(state: State) {
    pendingState = state
    updateHelpIndex()
    maybeScheduleShowHint(wrongAnswerCount = 0)
  }

  override suspend fun resumeHintsForSavedState(
    trackedWrongAnswerCount: Int,
    helpIndex: HelpIndex,
    state: State
  ) {
    when (helpIndex.indexTypeCase) {
      NEXT_AVAILABLE_HINT_INDEX -> {
        lastRevealedHintIndex = helpIndex.nextAvailableHintIndex - 1
        latestAvailableHintIndex = helpIndex.nextAvailableHintIndex
        solutionIsAvailable = false
        solutionIsRevealed = false
      }
      LATEST_REVEALED_HINT_INDEX -> {
        lastRevealedHintIndex = helpIndex.latestRevealedHintIndex
        latestAvailableHintIndex = helpIndex.latestRevealedHintIndex
        solutionIsAvailable = false
        solutionIsRevealed = false
      }
      SHOW_SOLUTION, EVERYTHING_REVEALED -> {
        // 1 is subtracted from the hint count because hints are indexed from 0.
        lastRevealedHintIndex = state.interaction.hintCount - 1
        latestAvailableHintIndex = state.interaction.hintCount - 1
        solutionIsAvailable = true
        solutionIsRevealed = helpIndex.indexTypeCase == EVERYTHING_REVEALED
      }
      else -> {
        lastRevealedHintIndex = -1
        latestAvailableHintIndex = -1
        solutionIsAvailable = false
        solutionIsRevealed = false
      }
    }
    pendingState = state
    this.trackedWrongAnswerCount = trackedWrongAnswerCount
    updateHelpIndex()
    maybeScheduleShowHint(wrongAnswerCount = trackedWrongAnswerCount)
  }

  override suspend fun finishState(newState: State) {
    reset()
    startWatchingForHintsInNewState(newState)
  }

  override suspend fun handleWrongAnswerSubmission(wrongAnswerCount: Int) {
    maybeScheduleShowHint(wrongAnswerCount)
  }

  override suspend fun viewHint(hintIndex: Int) {
    val helpIndex = computeCurrentHelpIndex()
    check(
      helpIndex.indexTypeCase == NEXT_AVAILABLE_HINT_INDEX &&
        helpIndex.nextAvailableHintIndex == hintIndex
    ) {
      "Cannot reveal hint for current index: ${helpIndex.indexTypeCase} (trying to reveal hint:" +
        " $hintIndex)"
    }

    cancelPendingTasks()
    lastRevealedHintIndex = lastRevealedHintIndex.coerceAtLeast(hintIndex)
    updateHelpIndex()
    maybeScheduleShowHint()
  }

  override suspend fun viewSolution() {
    val helpIndex = computeCurrentHelpIndex()
    check(helpIndex.indexTypeCase == SHOW_SOLUTION) {
      "Cannot reveal solution for current index: ${helpIndex.indexTypeCase}"
    }

    cancelPendingTasks()
    solutionIsRevealed = true
    updateHelpIndex()
  }

  override suspend fun navigateToPreviousState() {
    // Cancel tasks from the top pending state to avoid hint counters continuing after navigating
    // away.
    cancelPendingTasks()
  }

  override suspend fun navigateBackToLatestPendingState() {
    maybeScheduleShowHint()
  }

  override fun getCurrentHelpIndex(): StateFlow<HelpIndex> = helpIndexFlow

  private fun cancelPendingTasks() {
    // Cancel any potential pending hints by advancing the sequence number. Note that this isn't
    // reset to 0 to ensure that all previous hint tasks are cancelled, and new tasks can be
    // scheduled without overlapping with past sequence numbers.
    hintSequenceNumber++
  }

  private suspend fun maybeScheduleShowHint(wrongAnswerCount: Int = trackedWrongAnswerCount) {
    if (!pendingState.offersHelp()) {
      // If this state has no help to show, do nothing.
      return
    }

    // Start showing hints after a wrong answer is submitted or if the user appears stuck (e.g.
    // doesn't answer after some duration). Note that if there's already a timer to show a hint,
    // it will be reset for each subsequent answer.
    val currentHelpIndex = computeCurrentHelpIndex()
    val nextUnrevealedHelpIndex = getNextHelpIndexToReveal()
    val isFirstHint = currentHelpIndex.indexTypeCase == INDEXTYPE_NOT_SET
    if (wrongAnswerCount == trackedWrongAnswerCount) {
      // If no answers have been submitted, schedule a task to automatically help after a fixed
      // amount of time. This will automatically reset if something changes other than answers
      // (e.g. revealing a hint), which may trigger more help to become available.
      if (isFirstHint) {
        // The learner needs to wait longer for the initial hint to show since they need some time
        // to read through and consider the question.
        scheduleShowHint(delayShowInitialHintMs, nextUnrevealedHelpIndex)
      } else {
        scheduleShowHint(delayShowAdditionalHintsMs, nextUnrevealedHelpIndex)
      }
    } else {
      // See if the learner's new wrong answer justifies showing a hint.
      if (isFirstHint) {
        if (wrongAnswerCount > 1) {
          // If more than one answer has been submitted and no hint has yet been shown, show a
          // hint immediately since the learner is probably stuck.
          showHintImmediately(nextUnrevealedHelpIndex)
        }
      } else {
        // Otherwise, always schedule to show a hint on a new wrong answer for subsequent hints.
        scheduleShowHint(
          delayShowAdditionalHintsFromWrongAnswerMs,
          nextUnrevealedHelpIndex
        )
      }
      trackedWrongAnswerCount = wrongAnswerCount
    }
  }

  /** Resets this handler to prepare it for a new state, cancelling any pending hints. */
  private fun reset() {
    trackedWrongAnswerCount = 0
    // Cancel tasks rather than resetting to avoid potential cases where previous tasks can carry to
    // the next state.
    cancelPendingTasks()
    lastRevealedHintIndex = -1
    latestAvailableHintIndex = -1
    solutionIsAvailable = false
    solutionIsRevealed = false
  }

  private fun computeCurrentHelpIndex(): HelpIndex {
    val hintList = pendingState.interaction.hintList
    val hasSolution = pendingState.hasSolution()
    val hasAtLeastOneHintAvailable = latestAvailableHintIndex != -1
    val hasSeenAllAvailableHints = latestAvailableHintIndex == lastRevealedHintIndex
    val hasSeenAllHints = lastRevealedHintIndex == hintList.lastIndex
    val hasViewableSolution = hasSolution && solutionIsAvailable

    return when {
      // No hints or solution are available to be shown.
      !pendingState.offersHelp() -> HelpIndex.getDefaultInstance()

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
      hasAtLeastOneHintAvailable ->
        if (hasSeenAllAvailableHints) {
          HelpIndex.newBuilder().apply {
            latestRevealedHintIndex = lastRevealedHintIndex
          }.build()
        } else {
          HelpIndex.newBuilder().apply {
            nextAvailableHintIndex = latestAvailableHintIndex
          }.build()
        }

      // No hints are available to be shown yet.
      else -> HelpIndex.getDefaultInstance()
    }
  }

  /**
   * Returns the [HelpIndex] of the next hint or solution that hasn't yet been revealed, or
   * default if there is none.
   */
  private fun getNextHelpIndexToReveal(): HelpIndex {
    // Return the index of the first unrevealed hint, or the length of the list if all have been
    // revealed.
    val hintList = pendingState.interaction.hintList

    val hasHints = hintList.isNotEmpty()
    val hasSolution = pendingState.hasSolution()
    val hasHelp = hasHints || hasSolution
    val lastUnrevealedHintIndex = lastRevealedHintIndex + 1

    return if (!hasHelp) {
      HelpIndex.getDefaultInstance()
    } else if (hasHints && lastUnrevealedHintIndex < hintList.size) {
      HelpIndex.newBuilder().setNextAvailableHintIndex(lastUnrevealedHintIndex).build()
    } else if (hasSolution && !solutionIsRevealed) {
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
    CoroutineScope(backgroundCoroutineDispatcher).launch {
      delay(delayMs)
      showHint(targetSequenceNumber, helpIndexToShow)
    }
  }

  /**
   * Immediately indicates the specified hint is ready to be shown, cancelling any previously
   * pending hints initiated by calls to [scheduleShowHint].
   */
  private suspend fun showHintImmediately(helpIndexToShow: HelpIndex) {
    showHint(++hintSequenceNumber, helpIndexToShow)
  }

  private suspend fun showHint(targetSequenceNumber: Int, nextHelpIndexToShow: HelpIndex) {
    // Only finish this timer if no other hints were scheduled and no cancellations occurred.
    if (targetSequenceNumber == hintSequenceNumber) {
      val previousHelpIndex = computeCurrentHelpIndex()

      when (nextHelpIndexToShow.indexTypeCase) {
        NEXT_AVAILABLE_HINT_INDEX -> {
          latestAvailableHintIndex = nextHelpIndexToShow.nextAvailableHintIndex
        }
        SHOW_SOLUTION -> solutionIsAvailable = true
        else -> {} // Nothing else to do.
      }

      // Only indicate the hint is available if its index is actually new (including if it
      // becomes null such as in the case of the solution becoming available).
      if (nextHelpIndexToShow != previousHelpIndex) {
        updateHelpIndex()
      }
    }
  }

  private suspend fun updateHelpIndex() = helpIndexFlow.emit(computeCurrentHelpIndex())

  /** Production implementation of [HintHandler.Factory]. */
  class FactoryProdImpl @Inject constructor(
    @DelayShowInitialHintMillis private val delayShowInitialHintMs: Long,
    @DelayShowAdditionalHintsMillis private val delayShowAdditionalHintsMs: Long,
    @DelayShowAdditionalHintsFromWrongAnswerMillis
    private val delayShowAdditionalHintsFromWrongAnswerMs: Long,
    @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
  ) : HintHandler.Factory {
    override fun create(): HintHandler {
      return HintHandlerProdImpl(
        delayShowInitialHintMs,
        delayShowAdditionalHintsMs,
        delayShowAdditionalHintsFromWrongAnswerMs,
        backgroundCoroutineDispatcher
      )
    }
  }
}

/** Returns whether this state has a solution to show. */
private fun State.hasSolution(): Boolean = interaction.hasSolution()

/** Returns whether this state has help that the user can see. */
internal fun State.offersHelp(): Boolean = interaction.hintList.isNotEmpty() || hasSolution()
