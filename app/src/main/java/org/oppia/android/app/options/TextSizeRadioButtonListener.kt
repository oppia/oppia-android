package org.oppia.android.app.options

import org.oppia.android.app.model.ReadingTextSize

/** Listener for when the reading text size is selected from the [ReadingTextSizeFragment]. */
interface TextSizeRadioButtonListener {
  /** Called when the user selects a new [ReadingTextSize]. */
  fun onTextSizeSelected(selectedTextSize: ReadingTextSize)
}
