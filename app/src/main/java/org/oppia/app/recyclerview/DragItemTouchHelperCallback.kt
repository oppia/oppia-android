package org.oppia.app.recyclerview

import android.graphics.Color
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class DragItemTouchHelperCallback private constructor(dragDirs: Int, swipeDirs: Int) :
  ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
  private var dragEnabled = false
  private var onItemDragListener: OnItemDragListener? = null

  private constructor(builder: Builder) : this(
    builder.dragDirs,
    builder.swipeDirs
  ) {
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
    if (source.itemViewType != target.itemViewType) {
      return false
    }
    // Notify the adapter of the move
    onItemDragListener!!.onItemDragged(source.adapterPosition, target.adapterPosition)
    return true
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
  override fun onSelectedChanged(
    viewHolder: RecyclerView.ViewHolder?,
    actionState: Int
  ) { // We only want the active item to change
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
      viewHolder!!.itemView.alpha = ALPHA_FULL / 2
      viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
    }
    super.onSelectedChanged(viewHolder, actionState)
  }

  override fun clearView(
    recyclerView: RecyclerView,
    viewHolder: RecyclerView.ViewHolder
  ) {
    viewHolder.itemView.alpha = ALPHA_FULL
    viewHolder.itemView.setBackgroundColor(0)
    super.clearView(recyclerView, viewHolder)
  }

  interface OnItemDragListener {
    fun onItemDragged(indexFrom: Int, indexTo: Int)
  }

  class Builder(internal val dragDirs: Int, internal val swipeDirs: Int) {
    internal var onItemDragListener: OnItemDragListener? = null
    internal var dragEnabled = false
    fun onItemDragListener(value: OnItemDragListener?): Builder {
      onItemDragListener = value
      return this
    }


    fun build(): DragItemTouchHelperCallback {
      return DragItemTouchHelperCallback(this)
    }

  }
  companion object {
    const val ALPHA_FULL = 1.0f
  }
}
