package org.oppia.app.options

import androidx.databinding.ObservableField

/** StoryText size settings view model for the recycler view in [OptionsFragment]. */
class OptionsReadingTextSizeViewModel(
  private val routeToReadingTextSizeListener: RouteToReadingTextSizeListener
) : OptionsItemViewModel() {
  val readingTextSize = ObservableField<String>("")

  fun setReadingTextSize(readingTextSizeValue: String) {
    readingTextSize.set(readingTextSizeValue)
  }

  fun onReadingTextSizeClicked() {
    routeToReadingTextSizeListener.routeReadingTextSize(readingTextSize.get())
  }
}
