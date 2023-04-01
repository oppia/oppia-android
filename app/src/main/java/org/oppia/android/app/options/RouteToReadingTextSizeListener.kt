package org.oppia.android.app.options

import org.oppia.android.app.model.ReadingTextSize

/** Listener for when an activity should route to a [ReadingTextSizeActivity]. */
interface RouteToReadingTextSizeListener {
  /**
   * Loads a standalone UI for changing the current UI reading text size (with the current text size
   * being passed in via [readingTextSize]).
   */
  fun routeReadingTextSize(readingTextSize: ReadingTextSize)
}
