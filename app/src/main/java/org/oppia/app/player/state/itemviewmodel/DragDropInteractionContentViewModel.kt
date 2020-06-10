package org.oppia.app.player.state.itemviewmodel

import org.oppia.app.model.StringList
import org.oppia.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for MultipleChoiceInput values or ItemSelectionInput values. */
class DragDropInteractionContentViewModel(
  var htmlContent: StringList, var itemIndex: Int, var listSize:Int,
  private val dragAndDropSortInputViewModel: DragAndDropSortInputViewModel
) : ObservableViewModel() {

  fun handleGrouping() {
    dragAndDropSortInputViewModel.updateList(itemIndex)
  }
}