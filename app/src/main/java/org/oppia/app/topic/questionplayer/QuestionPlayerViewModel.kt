package org.oppia.app.topic.questionplayer

import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.StatePlayerRecyclerViewAssembler
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ObservableViewModel] for the question player. */
class QuestionPlayerViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList<StateItemViewModel>()
  val questionCount = ObservableField(0)
  val currentQuestion = ObservableField(0)
  val progressPercentage = ObservableField(0)
  val isAtEndOfSession = ObservableBoolean(false)

  // TODO(#40): Add a hasPendingAnswer() that binds to the enabled state of the Submit button.
  fun getPendingAnswer(recyclerViewAssembler: StatePlayerRecyclerViewAssembler): UserAnswer {
    return recyclerViewAssembler.getPendingAnswerHandler()?.getPendingAnswer() ?: UserAnswer.getDefaultInstance()
  }
}