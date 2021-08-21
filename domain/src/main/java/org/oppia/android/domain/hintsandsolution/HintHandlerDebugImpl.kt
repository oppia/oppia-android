package org.oppia.android.domain.hintsandsolution

import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionController
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

/**
 * Debug implementation of [HintHandler] that conditionally always shows hints & solutions if
 * 'Show all hints and solution' functionality is enabled in the developer options menu.
 * If this functionality is disabled then it will fall back to [HintHandlerProdImpl].
 */
class HintHandlerDebugImpl private constructor(
  private val hintHandlerProdImpl: HintHandlerProdImpl,
  private val showAllHintsAndSolutionController: ShowAllHintsAndSolutionController,
  private val hintMonitor: HintHandler.HintMonitor
) : HintHandler {

  private val handlerLock = ReentrantLock()

  override fun startWatchingForHintsInNewState(state: State) {
    if (!showAllHintsAndSolutionController.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.startWatchingForHintsInNewState(state)
    } else {
      handlerLock.withLock {
        hintHandlerProdImpl.pendingState = state
        hintMonitor.onHelpIndexChanged()
        showAllHintsAndSolution()
      }
    }
  }

  override fun finishState(newState: State) {
    if (!showAllHintsAndSolutionController.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.finishState(newState)
    } else {
      handlerLock.withLock {
        hintHandlerProdImpl.reset()
        startWatchingForHintsInNewState(newState)
      }
    }
  }

  override fun handleWrongAnswerSubmission(wrongAnswerCount: Int) {
    if (!showAllHintsAndSolutionController.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.handleWrongAnswerSubmission(wrongAnswerCount)
    }
  }

  override fun viewHint(hintIndex: Int) {
    if (!showAllHintsAndSolutionController.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.viewHint(hintIndex)
    } else {
      handlerLock.withLock {
        val helpIndex = hintHandlerProdImpl.computeCurrentHelpIndex()
        check(
          helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX &&
            helpIndex.nextAvailableHintIndex == hintIndex
        ) {
          "Cannot reveal hint for current index: ${helpIndex.indexTypeCase} + " +
            "(trying to reveal hint: $hintIndex)"
        }

        hintHandlerProdImpl.cancelPendingTasks()
        hintHandlerProdImpl.lastRevealedHintIndex =
          hintHandlerProdImpl.lastRevealedHintIndex.coerceAtLeast(hintIndex)
        hintMonitor.onHelpIndexChanged()
      }
    }
  }

  override fun viewSolution() {
    hintHandlerProdImpl.viewSolution()
  }

  override fun navigateToPreviousState() {
    hintHandlerProdImpl.navigateToPreviousState()
  }

  override fun navigateBackToLatestPendingState() {
    if (!showAllHintsAndSolutionController.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.navigateBackToLatestPendingState()
    }
  }

  override fun getCurrentHelpIndex(): HelpIndex {
    return hintHandlerProdImpl.getCurrentHelpIndex()
  }

  private fun showAllHintsAndSolution() {
    if (!hintHandlerProdImpl.pendingState.offersHelp()) {
      // If this state has no help to show, do nothing.
      return
    }

    hintHandlerProdImpl.pendingState.interaction.hintList.forEach { _ ->
      val helpIndex = hintHandlerProdImpl.getNextHelpIndexToReveal()
      hintHandlerProdImpl.showHintImmediately(helpIndex)
      viewHint(helpIndex.nextAvailableHintIndex)
    }
    if (hintHandlerProdImpl.pendingState.hasSolution()) {
      hintHandlerProdImpl.showHintImmediately(hintHandlerProdImpl.getNextHelpIndexToReveal())
      viewSolution()
    }
  }

  /** Debug implementation of [HintHandler.Factory]. */
  class FactoryDebugImpl @Inject constructor(
    private val hintHandlerProdImplFactory: HintHandlerProdImpl.FactoryProdImpl,
    private val showAllHintsAndSolutionController: ShowAllHintsAndSolutionController
  ) : HintHandler.Factory {
    override fun create(hintMonitor: HintHandler.HintMonitor): HintHandler {
      val hintHandlerProdImpl: HintHandlerProdImpl =
        hintHandlerProdImplFactory.create(hintMonitor) as HintHandlerProdImpl
      return HintHandlerDebugImpl(
        hintHandlerProdImpl,
        showAllHintsAndSolutionController,
        hintMonitor
      )
    }
  }
}
