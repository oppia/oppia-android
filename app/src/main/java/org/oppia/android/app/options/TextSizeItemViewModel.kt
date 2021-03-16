package org.oppia.android.app.options

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.viewmodel.ObservableViewModel

private const val SMALL_TEXT_SIZE_SCALE = 0.8f
private const val MEDIUM_TEXT_SIZE_SCALE = 1.0f
private const val LARGE_TEXT_SIZE_SCALE = 1.2f
private const val EXTRA_LARGE_TEXT_SIZE_SCALE = 1.4f

/**
 * Text Size item view model for the recycler view in [ReadingTextSizeFragment].
 */
class TextSizeItemViewModel constructor(
  val defaultReadingTextSizeInFloat: Float,
  val readingTextSize: ReadingTextSize,
  private val selectedTextSize: LiveData<String>,
  val textSizeRadioButtonListener: TextSizeRadioButtonListener
) : ObservableViewModel() {
  val textSizeName: String by lazy {
    when (readingTextSize) {
      ReadingTextSize.SMALL_TEXT_SIZE -> "Small"
      ReadingTextSize.MEDIUM_TEXT_SIZE -> "Medium"
      ReadingTextSize.LARGE_TEXT_SIZE -> "Large"
      else -> "Extra Large"
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
    Transformations.map(selectedTextSize) { it == textSizeName }
  }
}
