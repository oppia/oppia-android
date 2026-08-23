package org.oppia.android.app.player.state.itemviewmodel

import androidx.fragment.app.Fragment
import org.oppia.android.app.model.AnswerAndResponse
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.InteractionObject
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.translation.TranslationController
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
  interaction: Interaction,
  private val interactionAnswerReceiver: InteractionAnswerReceiver,
  val hasConversationView: Boolean,
  val hasPreviousButton: Boolean,
  val previousNavigationButtonListener: PreviousNavigationButtonListener,
  val isSplitView: Boolean,
  private val writtenTranslationContext: WrittenTranslationContext,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  val shouldAnimateContinueButton: Boolean,
  val continueButtonAnimationTimestampMs: Long
) : StateItemViewModel(ViewType.CONTINUE_INTERACTION), InteractionAnswerHandler {

  /**
   * The text to display on the continue interaction button. This uses the exploration's custom
   * button text (translated per [writtenTranslationContext]) if available, otherwise falls back to
   * the default localized 'Continue' string from app resources.
   */
  val buttonText: CharSequence = deriveButtonText(interaction)

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

  private fun deriveButtonText(interaction: Interaction): CharSequence {
    val buttonText =
      interaction.customizationArgsMap["buttonText"]?.subtitledUnicode?.let { unicode ->
        translationController.extractString(unicode, writtenTranslationContext)
      } ?: ""
    return buttonText.ifEmpty {
      resourceHandler.getStringInLocale(
        org.oppia.android.app.R.string.state_continue_button
      )
    }
  }

  /** Implementation of [StateItemViewModel.InteractionItemFactory] for this view model. */
  class FactoryImpl @Inject constructor(
    private val fragment: Fragment,
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController
  ) : InteractionItemFactory {
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
      userAnswerState: UserAnswerState,
      wrongAnswerList: List<AnswerAndResponse>
    ): StateItemViewModel {
      return ContinueInteractionViewModel(
        interaction,
        interactionAnswerReceiver,
        hasConversationView,
        hasPreviousButton,
        fragment as PreviousNavigationButtonListener,
        isSplitView,
        writtenTranslationContext,
        resourceHandler,
        translationController,
        shouldAnimateContinueButton = timeToStartNoticeAnimationMs != null,
        continueButtonAnimationTimestampMs = timeToStartNoticeAnimationMs ?: 0L
      )
    }
  }
}
