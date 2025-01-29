package org.oppia.android.app.recyclerview

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

private const val ALPHA_FULL = 1.0f

/** A [ItemTouchHelper.SimpleCallback] that provides drag & drop functionality to [RecyclerView]s. */
class DragAndDropItemFacilitator(
  private val onItemDragListener: OnItemDragListener,
  private val onDragEndedListener: OnDragEndedListener
) : ItemTouchHelper.SimpleCallback(
  ItemTouchHelper.UP or ItemTouchHelper.DOWN,
  /* swipeDirs= */ 0
) {

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
  override fun isLongPressDragEnabled(): Boolean = false

  override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
    super.onSelectedChanged(viewHolder, actionState)
    when (actionState) {
      ItemTouchHelper.ACTION_STATE_DRAG -> {
        viewHolder?.itemView?.alpha = ALPHA_FULL / 2
        viewHolder?.itemView?.isPressed = true
      }
      ItemTouchHelper.ACTION_STATE_IDLE -> {
        viewHolder?.itemView?.alpha = ALPHA_FULL
        viewHolder?.itemView?.isPressed = false
      }
    }
  }

  override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
    super.clearView(recyclerView, viewHolder)
    viewHolder.itemView.alpha = ALPHA_FULL
    recyclerView.post {
      recyclerView.adapter?.let { adapter ->
        if (!recyclerView.isComputingLayout) {
          onDragEndedListener.onDragEnded(adapter)
        } else {
          recyclerView.post {
            onDragEndedListener.onDragEnded(adapter)
          }
        }
      }
    }}}
