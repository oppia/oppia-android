package org.oppia.android.app.player.state.itemviewmodel

import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.model.SetOfTranslatableHtmlContentIds
import org.oppia.android.app.model.StringList
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for DragDropSortInput values. */
class DragDropInteractionContentViewModel(
  private val contentIdHtmlMap: Map<String, String>,
  var htmlContent: SetOfTranslatableHtmlContentIds,
  var itemIndex: Int,
  var listSize: Int,
  val dragAndDropSortInteractionViewModel: DragAndDropSortInteractionViewModel,
  private val resourceHandler: AppLanguageResourceHandler
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

  fun computeDragDropMoveUpItemContentDescription(): String {
    return if (itemIndex != 0) {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.move_item_up_content_description, itemIndex.toString()
      )
    } else resourceHandler.getStringInLocale(R.string.up_button_disabled)
  }

  fun computeDragDropMoveDownItemContentDescription(): String {
    return if (itemIndex != listSize - 1) {
      resourceHandler.getStringInLocaleWithWrapping(
        R.string.move_item_down_content_description, (itemIndex + 2).toString()
      )
    } else resourceHandler.getStringInLocale(R.string.down_button_disabled)
  }

  fun computeDragDropGroupItemContentDescription(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.link_to_item_below, (itemIndex + 2).toString()
    )
  }

  fun computeDragDropUnlinkItemContentDescription(): String {
    return resourceHandler.getStringInLocaleWithWrapping(
      R.string.unlink_items, (itemIndex + 1).toString()
    )
  }
}
