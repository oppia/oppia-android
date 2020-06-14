package org.oppia.app.recyclerview

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * This is used to enable functionality like  drag up and down to the RecyclerView.
 */
class DragItemTouchHelperCallback private constructor(dragDirs: Int, swipeDirs: Int) :
  ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
  private var dragEnabled = false
  private lateinit var onItemDragListener: OnItemDragListener

  private constructor(builder: Builder) : this(builder.dragDirs, builder.swipeDirs) {
    dragEnabled = builder.dragEnabled
    onItemDragListener = builder.onItemDragListener
  }

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

  class Builder(
    internal val dragDirs: Int,
    internal val swipeDirs: Int,
    internal val onItemDragListener: OnItemDragListener
  ) {
    internal var dragEnabled = true

    fun build(): DragItemTouchHelperCallback {
      return DragItemTouchHelperCallback(this)
    }
  }

  companion object {
    const val ALPHA_FULL = 1.0f
  }
}
