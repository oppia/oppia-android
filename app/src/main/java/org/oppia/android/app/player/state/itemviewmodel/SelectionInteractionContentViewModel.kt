package org.oppia.android.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  val htmlContent: SubtitledHtml,
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

  fun enableItem(): Boolean {
    return if (
      selectionInteractionViewModel.selectedItems.size ==
      selectionInteractionViewModel.maxAllowableSelectionCount
    ) {
      selectionInteractionViewModel.enabledItemsList.forEach {
        it.set(isAnswerSelected.get())
      }
      selectionInteractionViewModel.enabledItemsList[itemIndex].get()
    } else {
      selectionInteractionViewModel.enabledItemsList.forEach {
        it.set(true)
      }
      true
    }
  }
}
