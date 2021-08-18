package org.oppia.android.domain.hintsandsolution

import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionHandler
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

/** Debug implementation of [HintHandler]. */
class HintHandlerDebugImpl private constructor(
  private val hintHandlerProdImpl: HintHandlerProdImpl,
  private val showAllHintsAndSolutionHandler: ShowAllHintsAndSolutionHandler,
  private val hintMonitor: HintHandler.HintMonitor
) : HintHandler {

  private val handlerLock = ReentrantLock()

  override fun startWatchingForHintsInNewState(state: State) {
    if (!showAllHintsAndSolutionHandler.getShowAllHintsAndSolution()) {
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
    if (!showAllHintsAndSolutionHandler.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.finishState(newState)
    } else {
      handlerLock.withLock {
        hintHandlerProdImpl.reset()
        startWatchingForHintsInNewState(newState)
      }
    }
  }

  override fun handleWrongAnswerSubmission(wrongAnswerCount: Int) {
    if (!showAllHintsAndSolutionHandler.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.handleWrongAnswerSubmission(wrongAnswerCount)
    }
  }

  override fun viewHint(hintIndex: Int) {
    if (!showAllHintsAndSolutionHandler.getShowAllHintsAndSolution()) {
      hintHandlerProdImpl.viewHint(hintIndex)
    } else {
      handlerLock.withLock {
        val helpIndex = hintHandlerProdImpl.computeCurrentHelpIndex()
        check(
          helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.AVAILABLE_NEXT_HINT_INDEX &&
            helpIndex.availableNextHintIndex == hintIndex
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
    if (!showAllHintsAndSolutionHandler.getShowAllHintsAndSolution()) {
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
      viewHint(helpIndex.availableNextHintIndex)
    }
    val solutionExists = hintHandlerProdImpl.pendingState.interaction.solution.hasCorrectAnswer()
    if (solutionExists) {
      hintHandlerProdImpl.showHintImmediately(hintHandlerProdImpl.getNextHelpIndexToReveal())
      viewSolution()
    }
  }

  /** Debug implementation of [HintHandler.Factory]. */
  class FactoryImpl @Inject constructor(
    private val hintHandlerProdImplFactory: HintHandlerProdImpl.FactoryImpl,
    private val showAllHintsAndSolutionHandler: ShowAllHintsAndSolutionHandler
  ) : HintHandler.Factory {
    override fun create(hintMonitor: HintHandler.HintMonitor): HintHandler {
      val hintHandlerProdImpl: HintHandlerProdImpl =
        hintHandlerProdImplFactory.create(hintMonitor) as HintHandlerProdImpl
      return HintHandlerDebugImpl(
        hintHandlerProdImpl,
        showAllHintsAndSolutionHandler,
        hintMonitor
      )
    }
  }
}
