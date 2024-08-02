package org.oppia.android.app.player.state.itemviewmodel

import androidx.fragment.app.Fragment
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
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
  val shouldAnimateContinueButton: Boolean,
  val continueButtonAnimationTimestampMs: Long
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

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(private val fragment: Fragment) : InteractionItemFactory {
    override fun create(
      entityId: String,
      hasConversationView: Boolean,
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext,
      timeToStartNoticeAnimationMs: Long?,
      userAnswerState: UserAnswerState
    ): StateItemViewModel {
      return ContinueInteractionViewModel(
        interactionAnswerReceiver,
        hasConversationView,
        hasPreviousButton,
        fragment as PreviousNavigationButtonListener,
        isSplitView,
        writtenTranslationContext,
        shouldAnimateContinueButton = timeToStartNoticeAnimationMs != null,
        continueButtonAnimationTimestampMs = timeToStartNoticeAnimationMs ?: 0L
      )
    }
  }
}
