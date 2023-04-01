package org.oppia.android.app.options

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** Text Size list view model for the recycler view in [ReadingTextSizeFragment]. */
@FragmentScope
class ReadingTextSizeSelectionViewModel @Inject constructor(
  fragment: Fragment,
  resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  private val resourceBundle = fragment.requireContext().resources
  private val changeableSelectedTextSize = MutableLiveData<ReadingTextSize>()
  private val textSizeRadioButtonListener = fragment as TextSizeRadioButtonListener
  var selectedTextSize: ReadingTextSize?
    get() = changeableSelectedTextSize.value
    set(value) { changeableSelectedTextSize.value = value }

  private val textSizeList = listOf(
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.SMALL_TEXT_SIZE,
      changeableSelectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.MEDIUM_TEXT_SIZE,
      changeableSelectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.LARGE_TEXT_SIZE,
      changeableSelectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.EXTRA_LARGE_TEXT_SIZE,
      changeableSelectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
  )

  val recyclerViewTextSizeList: List<TextSizeItemViewModel> by lazy { textSizeList }
}
