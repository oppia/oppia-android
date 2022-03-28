package org.oppia.android.domain.hintsandsolution

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionController
import javax.inject.Inject

/**
 * Debug implementation of [HintHandler] that conditionally always shows hints & solutions if
 * 'Show all hints and solution' functionality is enabled in the developer options menu.
 * If this functionality is disabled then it will fall back to [HintHandlerProdImpl].
 */
class HintHandlerDebugImpl private constructor() : HintHandler {
  private val helpIndexFlow by lazy { MutableStateFlow(HelpIndex.getDefaultInstance()) }

  override suspend fun startWatchingForHintsInNewState(state: State) {
    recomputeHelpIndex(state)
  }

  override suspend fun resumeHintsForSavedState(
    trackedWrongAnswerCount: Int,
    helpIndex: HelpIndex,
    state: State
  ) {}

  override suspend fun finishState(newState: State) {
    startWatchingForHintsInNewState(newState)
  }

  override suspend fun handleWrongAnswerSubmission(wrongAnswerCount: Int) {}

  // This is never called as everything is already revealed when the state is loaded.
  override suspend fun viewHint(hintIndex: Int) {}

  // This is never called as everything is already revealed when the state is loaded.
  override suspend fun viewSolution() {}

  override suspend fun navigateToPreviousState() {}

  override suspend fun navigateBackToLatestPendingState() {}

  override fun getCurrentHelpIndex(): StateFlow<HelpIndex> = helpIndexFlow

  private fun recomputeHelpIndex(pendingState: State) {
    helpIndexFlow.value = if (!pendingState.offersHelp()) {
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
    override fun create(): HintHandler {
      return if (!showAllHintsAndSolutionController.getShowAllHintsAndSolution()) {
        hintHandlerProdImplFactory.create()
      } else {
        HintHandlerDebugImpl()
      }
    }
  }
}
