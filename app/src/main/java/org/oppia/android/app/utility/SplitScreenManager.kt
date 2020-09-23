package org.oppia.android.app.utility

import android.content.Context
import android.content.res.Resources
import org.oppia.android.app.R
import org.oppia.android.app.model.EphemeralQuestion
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.player.state.StateFragment
import org.oppia.android.app.player.state.itemviewmodel.InteractionViewModelModule.Companion.splitScreenInteractionIdsPool
import org.oppia.android.app.topic.questionplayer.QuestionPlayerFragment
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.sqrt

private const val MINIMUM_DIAGONAL_WIDTH = 7.0

/**
 * A helper class that is used to detect whether to split the screen in [QuestionPlayerFragment]
 * and [StateFragment] or not based on multiple factors.
 */
@Singleton
class SplitScreenManager @Inject constructor(private val context: Context) {

  /**
   * The actual function that decides whether to split or not.
   *
   * @param[interactionId] the id of the interaction of the state inside the [EphemeralState] or
   * [EphemeralQuestion].
   * @return `true` if the screen should be split, `false` otherwise.
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
   * Checks the device efficiency for splitting based on the **density** and **diagonal physical size**.
   * @return `true` if the device is splittable, `false` otherwise.
   */
  private fun isDeviceLargeEnoughForSplitScreen(): Boolean {
    val shouldSplit = context.resources.getBoolean(R.bool.shouldCheckForSplittingInteraction)
    return shouldSplit && computeDeviceDiagonalSizeInches() >= MINIMUM_DIAGONAL_WIDTH
  }

  /**
   * Checks whether the given interaction is suitable for splitting the screen or not.
   * @param[interactionId] the id of the interaction of the state inside the [EphemeralState] or
   * [EphemeralQuestion].
   * @return `true` if the interaction is suitable for splitting, `false` otherwise.
   */
  private fun isInteractionSplittable(interactionId: String): Boolean {
    return splitScreenInteractionIdsPool.find {
      it == interactionId
    } != null
  }
}
