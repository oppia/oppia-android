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
  private val hintMonitor: HintHandler.HintMonitor
) : HintHandler {

  private val handlerLock = ReentrantLock()

  private lateinit var pendingState: State

  override fun startWatchingForHintsInNewState(state: State) {
    handlerLock.withLock {
      pendingState = state
      hintMonitor.onHelpIndexChanged()
    }
  }

  override fun finishState(newState: State) {
    handlerLock.withLock {
      startWatchingForHintsInNewState(newState)
    }
  }

  override fun handleWrongAnswerSubmission(wrongAnswerCount: Int) {}

  override fun viewHint(hintIndex: Int) {}

  override fun viewSolution() {}

  override fun navigateToPreviousState() {}

  override fun navigateBackToLatestPendingState() {}

  override fun getCurrentHelpIndex(): HelpIndex {
    return if (!pendingState.offersHelp()) {
      // If this state has no help to show, do nothing.
      HelpIndex.getDefaultInstance()
    } else {
      HelpIndex.newBuilder().apply {
        everythingRevealed = true
      }.build()
    }
  }

  /** Debug implementation of [HintHandler.Factory]. */
  class FactoryDebugImpl @Inject constructor(
    private val hintHandlerProdImplFactory: HintHandlerProdImpl.FactoryProdImpl,
    private val showAllHintsAndSolutionController: ShowAllHintsAndSolutionController
  ) : HintHandler.Factory {
    override fun create(hintMonitor: HintHandler.HintMonitor): HintHandler {
      return if (!showAllHintsAndSolutionController.getShowAllHintsAndSolution()) {
        hintHandlerProdImplFactory.create(hintMonitor) as HintHandlerProdImpl
      } else {
        HintHandlerDebugImpl(hintMonitor)
      }
    }
  }
}
