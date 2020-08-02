package org.oppia.app.recyclerview

import androidx.recyclerview.widget.RecyclerView

/** Listener for items being dragged and dropped in [RecyclerView]s that have a [DragAndDropItemFacilitator]. */
interface OnItemDragListener {
  /**
   * Called when an item is dragged & dropped to a new position.
   *
   * @param indexFrom the original position of the item within the adapter item list
   * @param indexTo the new position of the item within the adapter item list
   * @param adapter the adapter of the [RecyclerView] whose item was recently dragged
   */
  fun onItemDragged(
    indexFrom: Int,
    indexTo: Int,
    adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
  )
}
