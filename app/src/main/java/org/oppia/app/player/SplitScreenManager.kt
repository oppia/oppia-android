package org.oppia.app.player

import android.app.Activity
import android.util.DisplayMetrics
import org.oppia.app.R
import kotlin.math.pow
import kotlin.math.sqrt

private const val MINIMUM_DIAGONAL_WIDTH = 7.0

class SplitScreenManager(
  private val activity: Activity
) {
  private val splitScreenInteractionIdsPool = listOf("DragAndDropSortInput", "ImageClickInput")

  fun shouldSplit(interactionId: String): Boolean {
    return isDeviceSplittable() && isInteractionSplittable(interactionId)
  }

  // https://stackoverflow.com/a/19156339/6628335
  private fun deviceDiagonalWidth(): Double {
    val dm = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(dm)
    val width = dm.widthPixels
    val height = dm.heightPixels
    val wi = width.toDouble() / dm.xdpi.toDouble()
    val hi = height.toDouble() / dm.ydpi.toDouble()
    val x = wi.pow(2.0)
    val y = hi.pow(2.0)
    return sqrt(x + y)
  }

  private fun isDeviceSplittable(): Boolean {
    val shouldSplit = activity.resources.getBoolean(R.bool.shouldSplit)
    return shouldSplit && deviceDiagonalWidth() >= MINIMUM_DIAGONAL_WIDTH
  }

  private fun isInteractionSplittable(interactionId: String): Boolean {
    val element = splitScreenInteractionIdsPool.find {
      it == interactionId
    }
    return element != null
  }
}
