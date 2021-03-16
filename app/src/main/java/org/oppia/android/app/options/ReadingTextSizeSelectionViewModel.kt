package org.oppia.android.app.options

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

@FragmentScope
class ReadingTextSizeSelectionViewModel @Inject constructor(
  val activity: AppCompatActivity,
  val fragment: Fragment
) : ObservableViewModel() {

  private val defaultReadingTextSizeInFloat = fragment.requireContext().resources.getDimension(
    R.dimen.default_reading_text_size
  )
  val selectedTextSize = MutableLiveData<String>()
  private val textSizeRadioButtonListener = fragment as TextSizeRadioButtonListener

  private val textSizeList = listOf<TextSizeItemViewModel>(
    TextSizeItemViewModel(
      defaultReadingTextSizeInFloat,
      ReadingTextSize.SMALL_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener
    ),
    TextSizeItemViewModel(
      defaultReadingTextSizeInFloat,
      ReadingTextSize.MEDIUM_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener
    ),
    TextSizeItemViewModel(
      defaultReadingTextSizeInFloat,
      ReadingTextSize.LARGE_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener
    ),
    TextSizeItemViewModel(
      defaultReadingTextSizeInFloat,
      ReadingTextSize.EXTRA_LARGE_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener
    ),
  )

  val recyclerViewTextSizeList: List<TextSizeItemViewModel> by lazy {
    textSizeList
  }
}
