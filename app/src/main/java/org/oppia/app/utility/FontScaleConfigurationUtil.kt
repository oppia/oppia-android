package org.oppia.app.utility

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.DisplayMetrics
import android.view.WindowManager
import org.oppia.app.model.ReadingTextSize
import javax.inject.Inject

/** Utility to change the scale of font for the entire app. */
class FontScaleConfigurationUtil @Inject constructor() {
  /** This method updates font scale. */
  fun adjustFontScale(
    context: Context,
    readingTextSize: String
  ) {
    val configuration = context.resources.configuration
    configuration.fontScale = getReadingTextSizeConfigurationUtil(readingTextSize)
    val metrics: DisplayMetrics = context.resources.displayMetrics
    val windowManager = context.getSystemService(WINDOW_SERVICE) as? WindowManager
    windowManager!!.defaultDisplay.getMetrics(metrics)
    metrics.scaledDensity = configuration.fontScale * metrics.density
    context.createConfigurationContext(configuration)
    context.resources.displayMetrics.setTo(metrics)
  }

  /** This method returns font scale by story text size. */
  private fun getReadingTextSizeConfigurationUtil(readingTextSize: String): Float {
    return when (readingTextSize) {
      ReadingTextSize.SMALL_TEXT_SIZE.name -> .8f
      ReadingTextSize.MEDIUM_TEXT_SIZE.name -> 1.0f
      ReadingTextSize.LARGE_TEXT_SIZE.name -> 1.2f
      ReadingTextSize.EXTRA_LARGE_TEXT_SIZE.name -> 1.4f
      else -> 1.0f
    }
  }
}
