package org.oppia.android.app.player.state.itemviewmodel

import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.model.StringList
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for DragDropSortInput values. */
class DragDropInteractionContentViewModel(
  var htmlContent: StringList,
  var itemIndex: Int,
  var listSize: Int,
  var dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel
) : ObservableViewModel() {

  fun handleGrouping(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragAndDropSortInteractionViewModel.updateList(itemIndex, adapter)
  }

  fun handleUnlinking(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragAndDropSortInteractionViewModel.unlinkElement(itemIndex, adapter)
  }

  fun handleUpMovement(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragAndDropSortInteractionViewModel.onItemMoved(itemIndex, itemIndex - 1, adapter)
  }

  fun handleDownMovement(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
    dragAndDropSortInteractionViewModel.onItemMoved(itemIndex, itemIndex + 1, adapter)
  }
}
