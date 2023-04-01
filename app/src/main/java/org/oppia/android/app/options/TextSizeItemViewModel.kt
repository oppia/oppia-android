package org.oppia.android.app.options

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

private const val SMALL_TEXT_SIZE_SCALE = 0.8f
private const val MEDIUM_TEXT_SIZE_SCALE = 1.0f
private const val LARGE_TEXT_SIZE_SCALE = 1.2f
private const val EXTRA_LARGE_TEXT_SIZE_SCALE = 1.4f

/** Text Size item view model for the recycler view in [ReadingTextSizeFragment]. */
class TextSizeItemViewModel(
  private val resources: Resources,
  val readingTextSize: ReadingTextSize,
  private val selectedTextSize: LiveData<ReadingTextSize>,
  val textSizeRadioButtonListener: TextSizeRadioButtonListener,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  private val defaultReadingTextSizeInFloat by lazy {
    resources.getDimension(R.dimen.default_reading_text_size)
  }
  val textSizeName: String by lazy {
    when (readingTextSize) {
      ReadingTextSize.SMALL_TEXT_SIZE ->
        resourceHandler.getStringInLocale(R.string.reading_text_size_small)
      ReadingTextSize.MEDIUM_TEXT_SIZE ->
        resourceHandler.getStringInLocale(R.string.reading_text_size_medium)
      ReadingTextSize.LARGE_TEXT_SIZE ->
        resourceHandler.getStringInLocale(R.string.reading_text_size_large)
      else -> resourceHandler.getStringInLocale(R.string.reading_text_size_extra_large)
    }
  }
  val textSize: Float by lazy {
    when (readingTextSize) {
      ReadingTextSize.SMALL_TEXT_SIZE -> defaultReadingTextSizeInFloat * SMALL_TEXT_SIZE_SCALE
      ReadingTextSize.MEDIUM_TEXT_SIZE -> defaultReadingTextSizeInFloat * MEDIUM_TEXT_SIZE_SCALE
      ReadingTextSize.LARGE_TEXT_SIZE -> defaultReadingTextSizeInFloat * LARGE_TEXT_SIZE_SCALE
      else -> defaultReadingTextSizeInFloat * EXTRA_LARGE_TEXT_SIZE_SCALE
    }
  }
  val isTextSizeSelected: LiveData<Boolean> by lazy {
    Transformations.map(selectedTextSize) { it == readingTextSize }
  }
}
