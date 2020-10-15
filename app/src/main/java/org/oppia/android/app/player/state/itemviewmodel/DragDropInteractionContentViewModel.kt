package org.oppia.android.app.player.state.itemviewmodel

import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.model.StringList
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for DragDropSortInput values. */
class DragDropInteractionContentViewModel(
  var htmlContent: StringList,
  val gcsResourceName: String,
  val gcsEntityType: String,
  val gcsEntityId: String,
  var itemIndex: Int,
  var listSize: Int,
  var dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel
) : ObservableViewModel() {

  // TODO: doc
  val nestedItemViewModelList: List<DragDropSingleItemViewModel> by lazy {
    htmlContent.htmlList.map { string ->
      DragDropSingleItemViewModel(string, gcsResourceName, gcsEntityType, gcsEntityId)
    }
  }

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
