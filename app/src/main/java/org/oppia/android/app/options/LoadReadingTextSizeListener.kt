package org.oppia.android.app.options

import org.oppia.android.app.model.ReadingTextSize

/** Listener for when an activity should load a [ReadingTextSizeFragment]. */
interface LoadReadingTextSizeListener {
  /**
   * Loads a tablet UI panel for changing the current UI reading text size (with the current text
   * size being passed in via [textSize]).
   */
  fun loadReadingTextSizeFragment(textSize: ReadingTextSize)
}
