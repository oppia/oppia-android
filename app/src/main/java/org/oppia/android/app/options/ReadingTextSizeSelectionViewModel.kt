package org.oppia.android.app.options

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject
import org.oppia.android.app.translation.AppLanguageResourceHandler

/** Text Size list view model for the recycler view in [ReadingTextSizeFragment]. */
@FragmentScope
class ReadingTextSizeSelectionViewModel @Inject constructor(
  fragment: Fragment,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {

  private val resourceBundle = fragment.requireContext().resources
  val selectedTextSize = MutableLiveData<String>()
  private val textSizeRadioButtonListener = fragment as TextSizeRadioButtonListener

  private val textSizeList = listOf<TextSizeItemViewModel>(
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.SMALL_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.MEDIUM_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.LARGE_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
    TextSizeItemViewModel(
      resourceBundle,
      ReadingTextSize.EXTRA_LARGE_TEXT_SIZE,
      selectedTextSize,
      textSizeRadioButtonListener,
      resourceHandler
    ),
  )

  val recyclerViewTextSizeList: List<TextSizeItemViewModel> by lazy {
    textSizeList
  }
}
