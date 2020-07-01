package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  val htmlContent: String,
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
