package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import org.oppia.app.model.UserAnswer

/** [StateItemViewModel] for previously submitted answers. */
class SubmittedAnswerViewModel(
  val submittedUserAnswer: UserAnswer,
  val gcsEntityId: String,
  val hasConversationView: Boolean,
  val isSplitView: Boolean
) : StateItemViewModel(ViewType.SUBMITTED_ANSWER) {
  val isCorrectAnswer = ObservableField<Boolean>(false)
  val isExtraInteractionAnswerCorrect = ObservableField<Boolean>(false)
}
