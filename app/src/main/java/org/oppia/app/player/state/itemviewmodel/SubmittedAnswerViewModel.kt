package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableField
import org.oppia.app.model.UserAnswer

/** [StateItemViewModel] for previously submitted answers. */
class SubmittedAnswerViewModel(
  val submittedUserAnswer: UserAnswer,
  val gcsEntityId: String
) : StateItemViewModel(ViewType.SUBMITTED_ANSWER) {
  val isCorrectAnswer = ObservableField<Boolean>(false)
}
