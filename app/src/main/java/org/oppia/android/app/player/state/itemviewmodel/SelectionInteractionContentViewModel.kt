package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  val htmlContent: String,
  val hasConversationView: Boolean,
  private val itemIndex: Int,
  private val selectionInteractionViewModel: SelectionInteractionViewModel
) : ObservableViewModel() {
  var isAnswerSelected = ObservableBoolean()

  fun handleItemClicked() {
    val isCurrentlySelected = isAnswerSelected.get()
    val shouldNowBeSelected =
      selectionInteractionViewModel.updateSelection(itemIndex, isCurrentlySelected)
    if (isCurrentlySelected != shouldNowBeSelected) {
      isAnswerSelected.set(shouldNowBeSelected)
    }
  }
}
