package org.oppia.app.utility

import android.app.Activity
import android.util.DisplayMetrics
import org.oppia.app.R
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState
import org.oppia.app.player.state.StateFragment
import org.oppia.app.topic.questionplayer.QuestionPlayerFragment
import kotlin.math.pow
import kotlin.math.sqrt

private const val MINIMUM_DIAGONAL_WIDTH = 7.0

/**
 * A helper class that is used to detect whether to split the screen in [QuestionPlayerFragment]
 * and [StateFragment] or not based on multiple factors
 */
class SplitScreenManager(
  private val activity: Activity
) {
  private val splitScreenInteractionIdsPool = listOf("DragAndDropSortInput", "ImageClickInput")

  /**
   * The actual function that decides whether to split or not.
   *
   * @param[interactionId] the id of the interaction of the state inside the [EphemeralState] or
   * [EphemeralQuestion]
   * @return `true` if the screen should be split, `false` otherwise
   */
  fun isSplitPossible(interactionId: String): Boolean {
    return isDeviceSplittable() && isInteractionSplittable(interactionId)
  }

  /**
   * @return The physical diagonal size of the device in inches
   */
  // https://stackoverflow.com/a/19156339/6628335
  private fun deviceDiagonalSize(): Double {
    val displayMetrics = DisplayMetrics()
    activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
    val widthInPixel = displayMetrics.widthPixels
    val heightInPixel = displayMetrics.heightPixels
    val widthInInch = widthInPixel.toDouble() / displayMetrics.xdpi.toDouble()
    val heightInInch = heightInPixel.toDouble() / displayMetrics.ydpi.toDouble()
    val x = widthInInch.pow(2.0)
    val y = heightInInch.pow(2.0)
    return sqrt(x + y)
  }

  /**
   * Checks the device efficiency for splitting based on the **density** and **diagonal physical size**
   * @return `true` if the device is splittable, `false` otherwise
   */
  private fun isDeviceSplittable(): Boolean {
    val shouldSplit = activity.resources.getBoolean(R.bool.shouldSplit)
    return shouldSplit && deviceDiagonalSize() >= MINIMUM_DIAGONAL_WIDTH
  }

  /**
   * Checks whether the given interaction is suitable for splitting the screen or not
   * @param[interactionId] the id of the interaction of the state inside the [EphemeralState] or
   * [EphemeralQuestion]
   * @return `true` if the interaction is suitable for splitting, `false` otherwise
   */
  private fun isInteractionSplittable(interactionId: String): Boolean {
    return splitScreenInteractionIdsPool.find {
      it == interactionId
    } != null
  }
}
