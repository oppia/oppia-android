package org.oppia.app.recyclerview

import android.content.Context
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * This is used to compute the number of columns based on the predicted size of the recycler view (which should be the
 * width of the parent given the match_parents) divided by the expected column size.
 * For reference https://github.com/pushpalroy/talkie/blob/master/app/src/main/java/com/pushpal/talkie/model/util/GridAutoFitLayoutManager.java.
 */
class GridAutoFitLayoutManager : GridLayoutManager {
  private var columnWidth: Int = 500 // assume cell width of 500px
  private var columnWidthChanged = true

  constructor(context: Context, columnWidth: Int) : super(context, 1) {
    setColumnWidth(checkedColumnWidth(context, columnWidth))
  }/* Initially set spanCount to 1, will be changed automatically later. */

  private fun checkedColumnWidth(context: Context, columnWidth: Int): Int {
    var columnWidth = columnWidth
    if (columnWidth <= 0) { /* Set default columnWidth value (48dp here). It is better to move this constant to static constant on top, but we need context to convert it to dp, so can't really do so. */
      columnWidth =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics).toInt()
    }
    return columnWidth
  }

  private fun setColumnWidth(newColumnWidth: Int) {
    if (newColumnWidth > 0 && newColumnWidth != columnWidth) {
      columnWidth = newColumnWidth
      columnWidthChanged = true
    }
  }

  fun getColumnWidth(): Int {
    return columnWidth
  }

  override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
    if (columnWidthChanged && columnWidth > 0) {
      val totalSpace: Int
      if (orientation == RecyclerView.VERTICAL) {
        totalSpace = width - paddingRight - paddingLeft
      } else {
        totalSpace = height - paddingTop - paddingBottom
      }
      val spanCount = Math.max(1, totalSpace / columnWidth)
      setSpanCount(spanCount)
      columnWidthChanged = false
    }
    super.onLayoutChildren(recycler, state)
  }
}
