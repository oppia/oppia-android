package org.oppia.app.recyclerview

import androidx.recyclerview.widget.RecyclerView

/** Listener for handling action of  [DragItemTouchHelperCallback]. */
interface OnItemDragListener {
  fun onItemDragged(indexFrom: Int, indexTo: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>)
}
