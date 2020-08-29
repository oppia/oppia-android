package org.oppia.app.options

import androidx.databinding.ObservableField

/** ReadingTextSize settings view model for the recycler view in [OptionsFragment]. */
class OptionsReadingTextSizeViewModel(
  private val routeToReadingTextSizeListener: RouteToReadingTextSizeListener,
  private val loadReadingTextSizeListener: LoadReadingTextSizeListener
) : OptionsItemViewModel() {
  val readingTextSize = ObservableField<String>("")

  fun setReadingTextSize(readingTextSizeValue: String) {
    readingTextSize.set(readingTextSizeValue)
  }

  fun loadReadingTextSizeFragment() {
    loadReadingTextSizeListener.loadReadingTextSizeFragment(readingTextSize.get()!!)
  }

  fun onReadingTextSizeClicked() {
    if (isMultipane.get()!!) {
      loadReadingTextSizeListener.loadReadingTextSizeFragment(readingTextSize.get()!!)
    } else {
      routeToReadingTextSizeListener.routeReadingTextSize(readingTextSize.get())
    }
  }
}
