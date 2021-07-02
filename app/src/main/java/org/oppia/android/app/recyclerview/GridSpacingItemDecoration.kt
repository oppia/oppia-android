package org.oppia.android.app.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import org.oppia.android.R
import org.oppia.android.app.home.HomeFragmentPresenter

class GridSpacingItemDecoration(context: Context) : ItemDecoration() {
  private var orientation = -1
  private var spanCount = -1

  private val spacing by lazy {
    context.resources.getDimensionPixelSize(R.dimen.home_outer_margin)
  }

  private val halfSpacing by lazy {
    context.resources.getDimensionPixelSize(R.dimen.home_inner_margin)
  }

  override fun getItemOffsets(
    outRect: Rect,
    view: View,
    parent: RecyclerView,
    state: RecyclerView.State
  ) {
    super.getItemOffsets(outRect, view, parent, state)
    if (orientation == -1) {
      orientation = getOrientation(parent)
    }
    if (spanCount == -1) {
      spanCount = getTotalSpan(parent)
    }
    val childIndex = parent.getChildAdapterPosition(view)
    val itemSpanSize = getItemSpanSize(parent, childIndex)
    val spanIndex = getItemSpanIndex(parent, childIndex)

    val position = parent.getChildAdapterPosition(view)
    val viewType = parent.adapter!!.getItemViewType(position)

    if (viewType == HomeFragmentPresenter.ViewType.TOPIC_LIST.ordinal) {
//
      /* INVALID SPAN */if (spanCount < 1) return

      if (spanCount == 2) {
        outRect.right = halfSpacing
        outRect.left = halfSpacing
      }

      if (spanCount == 4) {
        outRect.left =
          (spacing ) - spanIndex * (spacing ) / spanCount // spacing - column * ((1f / spanCount) * spacing)
        outRect.right =
          (spanIndex + 1) * (spacing) / spanCount // (column + 1) * ((1f / spanCount) * spacing)
      } else {
        // If it is in column 0 you apply the full offset on the start side, else only half
        // If spanIndex + spanSize equals spanCount (it occupies the last column) you apply the full offset on the end, else only half.
        when {
          spanIndex == 0 -> {
            outRect.left = spacing
          }
          itemSpanSize + spanIndex == spanCount -> {
            outRect.right = spacing
          }
          else -> {
            outRect.left = spacing / 2
            outRect.right = spacing / 2
          }
        }
      }

      if (isLayoutRTL(parent)) {
        val tmp = outRect.left
        outRect.left = outRect.right
        outRect.right = tmp
      }

      if (spanIndex < spanCount) { // top edge
        outRect.top = halfSpacing
      }
      outRect.bottom = halfSpacing
    }
  }

  private fun getTotalSpan(parent: RecyclerView): Int {
    val mgr = parent.layoutManager
    if (mgr is GridLayoutManager) {
      return mgr.spanCount
    }
    return -1
  }

  private fun getItemSpanSize(parent: RecyclerView, childIndex: Int): Int {
    val mgr = parent.layoutManager
    if (mgr is GridLayoutManager) {
      return mgr.spanSizeLookup.getSpanSize(childIndex)
    }
    return -1
  }

  private fun getItemSpanIndex(parent: RecyclerView, childIndex: Int): Int {
    val mgr = parent.layoutManager
    if (mgr is GridLayoutManager) {
      return mgr.spanSizeLookup.getSpanIndex(childIndex, spanCount)
    }
    return -1
  }

  private fun getOrientation(parent: RecyclerView): Int {
    val mgr = parent.layoutManager
    if (mgr is GridLayoutManager) {
      return mgr.orientation
    }
    return VERTICAL
  }

  companion object {
    private const val VERTICAL = OrientationHelper.VERTICAL

    @SuppressLint("NewApi", "WrongConstant")
    private fun isLayoutRTL(parent: RecyclerView): Boolean {
      return parent.layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL
    }
  }
}
