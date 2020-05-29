package org.oppia.app.recyclerview

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class RvItemTouchHelperCallback(var listener: RvItemTouchHelperListener) : ItemTouchHelper.Callback() {

  override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
    val swipeFlags = 0
    val dragFlags: Int = if (recyclerView.layoutManager is GridLayoutManager) {
      ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    } else {
      ItemTouchHelper.UP or ItemTouchHelper.DOWN
    }
    return makeMovementFlags(dragFlags, swipeFlags)
  }

  override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
    val fromPosition = viewHolder.adapterPosition
    val toPosition = target.adapterPosition
    listener.onMove(fromPosition, toPosition)
    return true
  }

  // when selected, show different background color
  override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
    // Translate View
    }
    super.onSelectedChanged(viewHolder, actionState)
  }

  override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
    super.clearView(recyclerView, viewHolder)
    // Revert View
  }

  override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
  }
}
