package org.oppia.app.recyclerview

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

private const val ALPHA_FULL = 1.0f
/**
 * The index value when drag drop action has been completed.
 */
const val NO_ITEM = -1

/** A [ItemTouchHelper.SimpleCallback] that provides drag & drop functionality to [RecyclerView]s. */
class DragAndDropItemFacilitator(
  private val onItemDragListener: OnItemDragListener
) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

  override fun onMove(
    recyclerView: RecyclerView,
    source: RecyclerView.ViewHolder,
    target: RecyclerView.ViewHolder
  ): Boolean {
    onItemDragListener.onItemDragged(
      source.adapterPosition,
      target.adapterPosition,
      recyclerView.adapter!!
    )
    return true
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

  override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      viewHolder!!.itemView.alpha = ALPHA_FULL / 2
    }
    super.onSelectedChanged(viewHolder, actionState)
  }

  override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
    viewHolder.itemView.alpha = ALPHA_FULL
    onItemDragListener.onItemDragged(
      NO_ITEM,
      NO_ITEM,
      recyclerView.adapter!!
    )
    super.clearView(recyclerView, viewHolder)
  }
}
