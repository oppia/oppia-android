package org.oppia.android.domain.hintsandsolution

import javax.inject.Inject
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.State
import org.oppia.android.domain.devoptions.ShowAllHintsAndSolutionController
import org.oppia.android.util.data.AsyncResult

/**
 * Debug implementation of [HintHandler] that conditionally always shows hints & solutions if
 * 'Show all hints and solution' functionality is enabled in the developer options menu.
 * If this functionality is disabled then it will fall back to [HintHandlerProdImpl].
 */
class HintHandlerDebugImpl private constructor() : HintHandler {
  private val helpIndexFlow by lazy { MutableStateFlow(HelpIndex.getDefaultInstance()) }
  /**
   * Represents an [AsyncResult] that always passes for operations that don't change the handler.
   */
  private val alwaysSucceedingResult by lazy { MutableStateFlow(AsyncResult.success(null)) }

  override suspend fun startWatchingForHintsInNewState(state: State): StateFlow<AsyncResult<Nothing?>> {
    recomputeHelpIndex(state)
    return alwaysSucceedingResult
  }

  override suspend fun resumeHintsForSavedState(
    trackedWrongAnswerCount: Int,
    helpIndex: HelpIndex,
    state: State
  ): StateFlow<AsyncResult<Nothing?>> = alwaysSucceedingResult

  override suspend fun finishState(newState: State): StateFlow<AsyncResult<Nothing?>> {
    startWatchingForHintsInNewState(newState)
    return alwaysSucceedingResult
  }

  override suspend fun handleWrongAnswerSubmission(
    wrongAnswerCount: Int
  ): StateFlow<AsyncResult<Nothing?>> = alwaysSucceedingResult

  // This is never called as everything is already revealed when the state is loaded.
  override suspend fun viewHint(hintIndex: Int): StateFlow<AsyncResult<Nothing?>> =
    alwaysSucceedingResult

  // This is never called as everything is already revealed when the state is loaded.
  override suspend fun viewSolution(): StateFlow<AsyncResult<Nothing?>> = alwaysSucceedingResult

  override suspend fun navigateToPreviousState(): StateFlow<AsyncResult<Nothing?>> =
    alwaysSucceedingResult

  override suspend fun navigateBackToLatestPendingState(): StateFlow<AsyncResult<Nothing?>> =
    alwaysSucceedingResult

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
  @ObsoleteCoroutinesApi
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
