package org.oppia.android.app.recyclerview

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

private const val ALPHA_FULL = 1.0f

/** A [ItemTouchHelper.SimpleCallback] that provides drag & drop functionality to [RecyclerView]s. */
class DragAndDropItemFacilitator(
  private val onItemDragListener: OnItemDragListener,
  private val onDragEndedListenr: OnDragEndedListener
) : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, /* swipeDirs= */ 0) { // ktlint-disable max-line-length

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
    onDragEndedListenr.onDragEnded(recyclerView.adapter!!)
    super.clearView(recyclerView, viewHolder)
  }
}
