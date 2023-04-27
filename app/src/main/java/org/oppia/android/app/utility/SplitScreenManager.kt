package org.oppia.android.app.utility

import android.content.Context
import android.content.res.Resources
import org.oppia.android.R
import org.oppia.android.app.player.state.itemviewmodel.SplitScreenInteractionIds
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

private const val MINIMUM_DIAGONAL_WIDTH = 7.0

/**
 * A helper class that is used to detect whether to split the screen in
 * [org.oppia.android.app.topic.questionplayer.QuestionPlayerFragment] and
 * [org.oppia.android.app.player.state.StateFragment] or not based on multiple factors.
 */
class SplitScreenManager @Inject constructor(
  private val context: Context,
  @SplitScreenInteractionIds private val splitInteractionIds: Set<@JvmSuppressWildcards String>
) {

  /**
   * The actual function that decides whether to split or not.
   *
   * @param interactionId the id of the interaction of the state inside the
   *     [org.oppia.android.app.model.EphemeralState] or
   *     [org.oppia.android.app.model.EphemeralQuestion]
   * @return whether the screen should be split
   */
  fun shouldSplitScreen(interactionId: String): Boolean {
    return isDeviceLargeEnoughForSplitScreen() && isInteractionSplittable(interactionId)
  }

  /**
   * @return The physical diagonal size of the device in inches.
   */
  // https://stackoverflow.com/a/19156339/6628335
  private fun computeDeviceDiagonalSizeInches(): Double {
    val displayMetrics = Resources.getSystem().displayMetrics
    val widthInPixel = displayMetrics.widthPixels
    val heightInPixel = displayMetrics.heightPixels
    val widthInInch = widthInPixel.toDouble() / displayMetrics.xdpi.toDouble()
    val heightInInch = heightInPixel.toDouble() / displayMetrics.ydpi.toDouble()
    val x = widthInInch.pow(2.0)
    val y = heightInInch.pow(2.0)
    return sqrt(x + y)
  }

  /**
   * Checks the device efficiency for splitting based on the **density** and **diagonal physical
   * size**.
   *
   * @return whether the device is splittable
   */
  private fun isDeviceLargeEnoughForSplitScreen(): Boolean {
    val shouldSplit = context.resources.getBoolean(R.bool.shouldCheckForSplittingInteraction)
    return shouldSplit && computeDeviceDiagonalSizeInches() >= MINIMUM_DIAGONAL_WIDTH
  }

  /**
   * Checks whether the given interaction is suitable for splitting the screen or not.
   *
   * @param interactionId the id of the interaction of the state inside the
   *     [org.oppia.android.app.model.EphemeralState] or
   *     [org.oppia.android.app.model.EphemeralQuestion]
   * @return whether if the interaction is suitable for splitting
   */
  private fun isInteractionSplittable(interactionId: String): Boolean {
    return splitInteractionIds.find {
      it == interactionId
    } != null
  }
}
