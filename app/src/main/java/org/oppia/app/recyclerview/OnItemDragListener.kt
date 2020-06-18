package org.oppia.app.recyclerview

import androidx.recyclerview.widget.RecyclerView

/** Listener for handling action of [DragAndDropItemFacilitator]. */
interface OnItemDragListener {
  /**
   * This method notifies the class about the change when [DragAndDropItemFacilitator] performs the
   * drag action on any item of recyclerview.
   *
   * @param indexFrom a position integer on which item was originally on.
   * @param indexTo   a position integer to which item is been moved to.
   * @param adapter   a adapter reference of the current recyclerview.
   */
  fun onItemDragged(indexFrom: Int, indexTo: Int, adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>)
}
