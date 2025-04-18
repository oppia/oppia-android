package org.oppia.android.app.recyclerview

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * This is used to keep the carousel item snap to start rather than  centre.
 * Reference: https://blog.mindorks.com/using-snaphelper-in-recyclerview-fc616b6833e8
 */
class StartSnapHelper : LinearSnapHelper() {

  private lateinit var mVerticalHelper: OrientationHelper
  private lateinit var mHorizontalHelper: OrientationHelper

  @Throws(IllegalStateException::class)
  override fun attachToRecyclerView(recyclerView: RecyclerView?) {
    super.attachToRecyclerView(recyclerView)
  }

  override fun calculateDistanceToFinalSnap(
    layoutManager: RecyclerView.LayoutManager,
    targetView: View
  ): IntArray? {
    val out = IntArray(2)
    if (layoutManager.canScrollHorizontally()) {
      out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager))
    } else {
      out[0] = 0
    }
    if (layoutManager.canScrollVertically()) {
      out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager))
    } else {
      out[1] = 0
    }
    return out
  }

  override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
    return if (layoutManager is LinearLayoutManager) {
      if (layoutManager.canScrollHorizontally()) {
        getStartView(layoutManager, getHorizontalHelper(layoutManager))
      } else {
        getStartView(layoutManager, getVerticalHelper(layoutManager))
      }
    } else super.findSnapView(layoutManager)
  }

  private fun distanceToStart(targetView: View, helper: OrientationHelper): Int {
    return helper.getDecoratedStart(targetView) - helper.startAfterPadding
  }

  private fun getStartView(
    layoutManager: RecyclerView.LayoutManager,
    helper: OrientationHelper
  ): View? {
    if (layoutManager is LinearLayoutManager) {
      val firstChild = layoutManager.findFirstVisibleItemPosition()
      val lastChild = layoutManager.findLastCompletelyVisibleItemPosition()
      val isLastItemFullyVisible = lastChild == layoutManager.itemCount - 1

      if (firstChild == RecyclerView.NO_POSITION) {
        return null
      }

      val child = layoutManager.findViewByPosition(firstChild)

      // If the last item is fully visible, but we're still in the middle of the list, allow
      // snapping to the start.
      if (isLastItemFullyVisible && firstChild > 0) {
        return child
      }

      return if (helper.getDecoratedEnd(child) >= helper.getDecoratedMeasurement(child) / 2 &&
        helper.getDecoratedEnd(child) > 0
      ) {
        child
      } else {
        layoutManager.findViewByPosition(firstChild + 1)
      }
    }
    return super.findSnapView(layoutManager)
  }

  private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
    if (!::mVerticalHelper.isInitialized) {
      mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
    }
    return mVerticalHelper
  }

  private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
    if (!::mHorizontalHelper.isInitialized) {
      mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
    }
    return mHorizontalHelper
  }
}
