package org.oppia.android.app.utility

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.util.DisplayMetrics
import android.view.WindowManager
import org.oppia.android.app.model.ReadingTextSize
import javax.inject.Inject

/** Utility to change the scale of font for the entire app. */
class FontScaleConfigurationUtil @Inject constructor() {
  /**
   * Updates the specified [context]'s current configuration to scale text size according to the
   * provided [readingTextSize].
   */
  fun adjustFontScale(context: Context, readingTextSize: ReadingTextSize) {
    val configuration = context.resources.configuration
    configuration.fontScale = getReadingTextSizeConfigurationUtil(readingTextSize)
    val metrics: DisplayMetrics = context.resources.displayMetrics
    val windowManager = context.getSystemService(WINDOW_SERVICE) as? WindowManager
    // TODO(#3616): Migrate to the proper SDK 30+ APIs.
    @Suppress("DEPRECATION") // The code is correct for targeted versions of Android.
    windowManager!!.defaultDisplay.getMetrics(metrics)
    // TODO(#5625): Migrate away from scaledDensity.
    @Suppress("DEPRECATION")
    metrics.scaledDensity = configuration.fontScale * metrics.density
    context.createConfigurationContext(configuration)
    context.resources.displayMetrics.setTo(metrics)
  }

  private fun getReadingTextSizeConfigurationUtil(readingTextSize: ReadingTextSize): Float {
    return when (readingTextSize) {
      ReadingTextSize.SMALL_TEXT_SIZE -> .8f
      ReadingTextSize.MEDIUM_TEXT_SIZE -> 1.0f
      ReadingTextSize.LARGE_TEXT_SIZE -> 1.2f
      ReadingTextSize.EXTRA_LARGE_TEXT_SIZE -> 1.4f
      else -> 1.0f
    }
  }
}
