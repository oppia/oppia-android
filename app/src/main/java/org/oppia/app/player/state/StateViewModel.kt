package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.viewmodel.ObservableArrayList
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor() : ObservableViewModel() {
  val itemList: ObservableList<StateItemViewModel> = ObservableArrayList()

  val isAudioBarVisible = ObservableField<Boolean>(false)

  fun setAudioBarVisibility(audioBarVisible: Boolean) {
    isAudioBarVisible.set(audioBarVisible)
  }

  // TODO(#164): Add a hasPendingAnswer() that binds to the enabled state of the Submit button.
  fun getPendingAnswer(statePlayerRecyclerViewAssembler: StatePlayerRecyclerViewAssembler): UserAnswer {
    return statePlayerRecyclerViewAssembler.getPendingAnswerHandler(itemList)?.getPendingAnswer()
      ?: UserAnswer.getDefaultInstance()
  }
}
