package org.oppia.app.recyclerview

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * This is used to compute the number of columns based on the predicted size of the recycler view (which should be the
 * width of the parent given the match_parents) divided by the expected column size.
 * For reference https://github.com/pushpalroy/talkie/blob/master/app/src/main/java/com/pushpal/talkie/model/util/GridAutoFitLayoutManager.java.
 */
class GridAutoFitLayoutManager : GridLayoutManager {
  private var columnWidth: Int = 0
  private var columnWidthChanged = true
  private val context: Context

  constructor(context: Context, columnWidth: Int) : super(context, 1) {
    this.context = context
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

  override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State) {
    if (columnWidthChanged && columnWidth > 0) {
      val totalSpace: Int
      if (orientation == RecyclerView.VERTICAL) {
        totalSpace = width - paddingRight - paddingLeft
      } else {
        totalSpace = height - paddingTop - paddingBottom
      }
      val autoFitspanCount = Math.max(1, totalSpace / columnWidth)
      if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        spanCount = 2
      } else {
        spanCount = if (autoFitspanCount < 3) 3 else autoFitspanCount
      }
      columnWidthChanged = false
    }
    super.onLayoutChildren(recycler, state)
  }
}
