package org.oppia.android.app.profile

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.app.R

/**
 * A decorator that assigns a dynamic margin to a recyclerview based on the number of items in the
 * adapter.
 */
class DynamicProfileItemMarginsDecorator(
  private val context: Context,
  private val adapter: RecyclerView.Adapter<*>
) : RecyclerView.ItemDecoration() {

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    val itemCount = adapter.itemCount

    val margin = when {
      itemCount == 1 -> context.resources.getDimensionPixelSize(
        R.dimen.profile_selection_fragment_recyclerview_margin_single_item
      )

      itemCount <= 3 -> context.resources.getDimensionPixelSize(
        R.dimen.profile_selection_fragment_recyclerview_margin_few_items
      )

      else -> context.resources.getDimensionPixelSize(
        R.dimen.profile_selection_fragment_recyclerview_margin_many_items
      )
    }

    outRect.left = margin
    outRect.right = margin
  }
}
