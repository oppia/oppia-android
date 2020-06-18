package org.oppia.app.recyclerview

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

private const val ALPHA_FULL = 1.0f

/**
 * This is used to enable functionality like drag up and down to the RecyclerView.
 */
class DragAndDropItemFacilitator(
  dragDirs: Int,
  swipeDirs: Int,
  private val onItemDragListener: OnItemDragListener
) :
  ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
  private var dragEnabled = true

  override fun isLongPressDragEnabled(): Boolean {
    return dragEnabled
  }

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
    super.clearView(recyclerView, viewHolder)
  }
}
