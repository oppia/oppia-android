package org.oppia.app.utility

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.DisplayMetrics
import android.view.WindowManager
import org.oppia.app.model.StoryTextSize
import javax.inject.Inject

/** Utility to change the scale of font for the entire app. */
class FontScaleConfigurationUtil @Inject constructor() {
  /** This method updates font scale. */
  fun adjustFontScale(
    context: Context, storyTextSize: String
  ) {
    val configuration = context.resources.configuration
    configuration.fontScale = getStoryTextSizeConfigurationUtil(storyTextSize)
    val metrics: DisplayMetrics = context.resources.displayMetrics
    val windowManager = context.getSystemService(WINDOW_SERVICE) as? WindowManager
    windowManager!!.defaultDisplay.getMetrics(metrics)
    metrics.scaledDensity = configuration.fontScale * metrics.density
    context.createConfigurationContext(configuration)
    context.resources.displayMetrics.setTo(metrics)
  }

  /** This method returns font scale by story text size. */
  private fun getStoryTextSizeConfigurationUtil(storyTextSize: String): Float {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE.name -> .8f
      StoryTextSize.MEDIUM_TEXT_SIZE.name -> 1.0f
      StoryTextSize.LARGE_TEXT_SIZE.name -> 1.2f
      StoryTextSize.EXTRA_LARGE_TEXT_SIZE.name -> 1.4f
      else -> 1.0f
    }
  }
}
