package org.oppia.android.app.recyclerview

import androidx.recyclerview.widget.RecyclerView

/** Listener when drag is end for an item of [RecyclerView] that have a [DragAndDropItemFacilitator]. */
interface OnDragEndedListener {
  /**
   * @param adapter the adapter of the [RecyclerView] whose item was recently dragged
   */
  fun onDragEnded(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>)
}
