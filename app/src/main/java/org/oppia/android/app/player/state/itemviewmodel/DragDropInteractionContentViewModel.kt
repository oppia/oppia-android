package org.oppia.android.app.player.state.itemviewmodel

import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.StringList
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for DragDropSortInput values. */
class DragDropInteractionContentViewModel(
  private val contentIdHtmlMap: Map<String, String>,
  var htmlContent: SetOfTranslatableHtmlContentIds,
  var itemIndex: Int,
  var listSize: Int,
  val dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel
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

  /**
   * Returns a [StringList] corresponding to a list of HTML strings that can be displayed to the
   * user.
   */
  fun computeStringList(): StringList = StringList.newBuilder().apply {
    addAllHtml(htmlContent.contentIdsList.mapNotNull { contentIdHtmlMap[it.contentId] })
  }.build()
}
