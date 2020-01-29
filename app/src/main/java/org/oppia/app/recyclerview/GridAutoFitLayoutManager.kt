package org.oppia.app.recyclerview

import android.content.Context
import android.content.res.Configuration
import android.util.TypedValue
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * This is used to compute the number of columns based on the predicted size of the recycler view (which should be the
 * width of the parent given the match_parents) divided by the expected column size.
 * Initially set spanCount to 1, will be changed automatically later.
 * For reference https://github.com/pushpalroy/talkie/blob/master/app/src/main/java/com/pushpal/talkie/model/util/GridAutoFitLayoutManager.java.
 */
class GridAutoFitLayoutManager(
  private val context: Context,
  private var columnWidth: Int
) : GridLayoutManager(context, 1) {
  private var columnWidthChanged = true

  init {
    setColumnWidth(checkedColumnWidth(context, columnWidth))
  }

  private fun checkedColumnWidth(context: Context, columnWidth: Int): Int {
    /* Set default columnWidth value (48dp here). It is better to move this constant to static constant on top, but we need context to convert it to dp, so can't really do so. */
    if (columnWidth <= 0) {
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics).toInt()
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
      val totalSpace: Int = if (orientation == RecyclerView.VERTICAL) {
        width - paddingRight - paddingLeft
      } else {
        height - paddingTop - paddingBottom
      }
      val autoFitSpanCount = Math.max(1, totalSpace / columnWidth)
      spanCount = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        2
      } else {
        if (autoFitSpanCount < 3) 3 else autoFitSpanCount
      }
      columnWidthChanged = false
    }
    super.onLayoutChildren(recycler, state)
  }
}
