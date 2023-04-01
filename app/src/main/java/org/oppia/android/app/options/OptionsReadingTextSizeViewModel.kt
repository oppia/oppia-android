package org.oppia.android.app.options

import androidx.databinding.ObservableField
import org.oppia.android.R
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** ReadingTextSize settings view model for the recycler view in [OptionsFragment]. */
class OptionsReadingTextSizeViewModel(
  private val routeToReadingTextSizeListener: RouteToReadingTextSizeListener,
  private val loadReadingTextSizeListener: LoadReadingTextSizeListener,
  private val resourceHandler: AppLanguageResourceHandler
) : OptionsItemViewModel() {
  val readingTextSize = ObservableField(ReadingTextSize.TEXT_SIZE_UNSPECIFIED)
  val textSizeName: String
    get() {
      return when (readingTextSize.get()!!) {
        ReadingTextSize.SMALL_TEXT_SIZE ->
          resourceHandler.getStringInLocale(R.string.reading_text_size_small)
        ReadingTextSize.MEDIUM_TEXT_SIZE ->
          resourceHandler.getStringInLocale(R.string.reading_text_size_medium)
        ReadingTextSize.LARGE_TEXT_SIZE ->
          resourceHandler.getStringInLocale(R.string.reading_text_size_large)
        else -> resourceHandler.getStringInLocale(R.string.reading_text_size_extra_large)
      }
    }

  fun loadReadingTextSizeFragment() {
    loadReadingTextSizeListener.loadReadingTextSizeFragment(readingTextSize.get()!!)
  }

  fun onReadingTextSizeClicked() {
    if (isMultipane.get()!!) {
      loadReadingTextSizeListener.loadReadingTextSizeFragment(readingTextSize.get()!!)
    } else {
      routeToReadingTextSizeListener.routeReadingTextSize(readingTextSize.get()!!)
    }
  }
}
