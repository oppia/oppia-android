package org.oppia.android.app.player.state.itemviewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.android.app.utility.LifecycleSafeTimerFactory
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/**
 * [StateItemViewModel] for the 'Continue' button. Note that this supports previous state navigation, and differs from
 * [NextButtonViewModel] in that the latter is for navigating to existing states rather than a new state. This differs
 * from [ContinueNavigationButtonViewModel] in that the latter is for an already completed state, whereas this
 * represents an actual interaction.
 */
class ContinueInteractionViewModel private constructor(
  private val interactionAnswerReceiver: InteractionAnswerReceiver,
  val hasConversationView: Boolean,
  val hasPreviousButton: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val explorationProgressController: ExplorationProgressController,
  private val fragment: Fragment,
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val oppiaClock: OppiaClock
) : StateItemViewModel(ViewType.CONTINUE_INTERACTION), InteractionAnswerHandler {

  override fun isExplicitAnswerSubmissionRequired(): Boolean = false

  override fun isAutoNavigating(): Boolean = true

  override fun getPendingAnswer(): UserAnswer = UserAnswer.newBuilder().apply {
    answer = InteractionObject.newBuilder().apply {
      normalizedString = DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER
    }.build()
    this.writtenTranslationContext = this@ContinueInteractionViewModel.writtenTranslationContext
  }.build()

  fun handleButtonClicked() {
    interactionAnswerReceiver.onAnswerReadyForSubmission(getPendingAnswer())
  }

  val animateContinueButton = MutableLiveData(false)

  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState().toLiveData()
  }

  fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment) { result ->
      processEphemeralStateResult(result)
    }
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralState>) {
    when (result) {
      is AsyncResult.Failure -> {
      }
//        oppiaLogger.e("StateFragment", "Failed to retrieve ephemeral state", result.error)
      is AsyncResult.Pending -> {
      } // Display nothing until a valid result is available.
      is AsyncResult.Success -> processEphemeralState(result.value)
    }
  }

  private fun processEphemeralState(ephemeralState: EphemeralState) {
    if (!ephemeralState.hasPreviousState) {
      val timeLeftToAnimate =
        ephemeralState.continueButtonAnimationTimestamp - oppiaClock.getCurrentTimeMs()
      if (timeLeftToAnimate < 0) {
        animateContinueButton.value = true
      } else {
        lifecycleSafeTimerFactory.createTimer(timeLeftToAnimate).observe(fragment) {
          animateContinueButton.value = true
        }
      }
    }
  }

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val fragment: Fragment,
    private val explorationProgressController: ExplorationProgressController,
    private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
    private val oppiaClock: OppiaClock
  ) : InteractionItemFactory {
    override fun create(
      entityId: String,
      hasConversationView: Boolean,
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext
    ): StateItemViewModel {
      return ContinueInteractionViewModel(
        interactionAnswerReceiver,
        hasConversationView,
        hasPreviousButton,
        fragment as PreviousNavigationButtonListener,
        isSplitView,
        writtenTranslationContext,
        explorationProgressController,
        fragment,
        lifecycleSafeTimerFactory,
        oppiaClock
      )
    }
  }
}
