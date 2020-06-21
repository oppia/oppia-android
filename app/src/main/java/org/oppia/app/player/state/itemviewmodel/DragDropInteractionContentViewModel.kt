package org.oppia.app.player.state.itemviewmodel

import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.model.StringList
import org.oppia.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for DragDropSortInput values. */
class DragDropInteractionContentViewModel(
  var htmlContent: StringList, var itemIndex: Int, var listSize: Int,
  private val dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel
) : ObservableViewModel() {

  fun handleGrouping(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragAndDropSortInteractionViewModel.updateList(itemIndex,adapter)
  }
}