package org.oppia.android.app.recyclerview

import android.view.ViewTreeObserver
import androidx.core.view.doOnNextLayout
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
  override fun isLongPressDragEnabled(): Boolean = true

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
    recyclerView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
      override fun onPreDraw(): Boolean {
        recyclerView.viewTreeObserver.removeOnPreDrawListener(this)

        if (!recyclerView.isComputingLayout) {
          recyclerView.adapter?.let { adapter ->
            onDragEndedListener.onDragEnded(adapter)
          }
        } else {
          recyclerView.doOnNextLayout {
            recyclerView.adapter?.let { adapter ->
              onDragEndedListener.onDragEnded(adapter)
            }
          }
        }
        return true
      }
    })
  }
}
