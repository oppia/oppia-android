package org.oppia.android.app.player.state.itemviewmodel

import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.viewmodel.ObservableViewModel

/**
 * The root [ObservableViewModel] for all individual items that may be displayed in the state
 * fragment recycler view.
 */
abstract class StateItemViewModel(val viewType: ViewType) : ObservableViewModel() {

  /** Corresponds to the type of the view model. */
  enum class ViewType {
    CONTENT,
    FEEDBACK,
    PREVIOUS_NAVIGATION_BUTTON,
    NEXT_NAVIGATION_BUTTON,
    SUBMIT_ANSWER_BUTTON,
    CONTINUE_NAVIGATION_BUTTON,
    REPLAY_NAVIGATION_BUTTON,
    RETURN_TO_TOPIC_NAVIGATION_BUTTON,
    CONTINUE_INTERACTION,
    SELECTION_INTERACTION,
    FRACTION_INPUT_INTERACTION,
    NUMERIC_INPUT_INTERACTION,
    TEXT_INPUT_INTERACTION,
    SUBMITTED_ANSWER,
    PREVIOUS_RESPONSES_HEADER,
    DRAG_DROP_SORT_INTERACTION,
    IMAGE_REGION_SELECTION_INTERACTION,
    RATIO_EXPRESSION_INPUT_INTERACTION,
    NUMERIC_EXPRESSION_INPUT_INTERACTION,
    ALGEBRAIC_EXPRESSION_INPUT_INTERACTION,
    MATH_EQUATION_INPUT_INTERACTION
  }

  /** Factory for creating new [StateItemViewModel]s for interactions. */
  interface InteractionItemFactory {
    /**
     * Returns a new [StateItemViewModel] corresponding to this interaction with the GCS entity ID,
     * the [Interaction] object corresponding to the interaction view, a receiver for answers if
     * this interaction pushes answers, and whether there's a previous button enabled (only relevant
     * for navigation-based interactions).
     *
     * @param timeToStartNoticeAnimationMs the milliseconds at which the implementation should start
     *     its "take notice" animation for the user, if it has one. When null, the animation should
     *     never be shown.
     */
    fun create(
      entityId: String,
      hasConversationView: Boolean,
      interaction: Interaction,
      interactionAnswerReceiver: InteractionAnswerReceiver,
      answerErrorReceiver: InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton: Boolean,
      isSplitView: Boolean,
      writtenTranslationContext: WrittenTranslationContext,
      timeToStartNoticeAnimationMs: Long?,
      userAnswerState: UserAnswerState = UserAnswerState.getDefaultInstance()
    ): StateItemViewModel
  }
}
