package org.oppia.app.utility

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.DisplayMetrics
import android.view.WindowManager
import org.oppia.app.model.StoryTextSize
import javax.inject.Inject

class FontScaleConfigurationUtil @Inject constructor(
  private val context: Context,
  private val result: String
) {
  fun adjustFontScale() {
    val configuration = context.resources.configuration
    configuration.fontScale = getStoryTextSize(result)
    val metrics: DisplayMetrics = context.resources.displayMetrics
    val windowManager =
      context.getSystemService(WINDOW_SERVICE) as WindowManager?
    windowManager!!.defaultDisplay.getMetrics(metrics)
    metrics.scaledDensity = configuration.fontScale * metrics.density
    context.createConfigurationContext(configuration)
    context.resources.displayMetrics.setTo(metrics)
  }

  private fun getStoryTextSize(storyTextSize: String): Float {
    return when (storyTextSize) {
      StoryTextSize.SMALL_TEXT_SIZE.name -> .8f
      StoryTextSize.MEDIUM_TEXT_SIZE.name -> 1.0f
      StoryTextSize.LARGE_TEXT_SIZE.name -> 1.4f
      else -> 1.6f
    }
  }
}
