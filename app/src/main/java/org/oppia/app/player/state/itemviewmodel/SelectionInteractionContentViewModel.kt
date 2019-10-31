package org.oppia.app.player.state.itemviewmodel

import androidx.databinding.ObservableBoolean
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class SelectionInteractionContentViewModel(
  val htmlContent: String, private val itemIndex: Int, isAnswerInitiallySelected: Boolean, val isReadOnly: Boolean,
  private val selectionInteractionViewModel: SelectionInteractionViewModel
): ObservableViewModel() {
  var isAnswerSelected = ObservableBoolean(isAnswerInitiallySelected)

  fun handleItemClicked() {
    // TODO(BenHenning): Clean up this data flow.
    selectionInteractionViewModel.handleItemClicked(this, itemIndex, isAnswerSelected.get())
  }
}
