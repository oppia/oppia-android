package org.oppia.app.options


import androidx.databinding.ObservableList
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ObservableArrayList
import javax.inject.Inject

/** [ViewModel] for [OptionsFragment]. */
@FragmentScope
class OptionControlsViewModel @Inject constructor() : ViewModel() {
  private val itemViewModelList: ObservableList<OptionsItemViewModel> = ObservableArrayList()

  fun processOptionsList(): ObservableList<OptionsItemViewModel> {
    itemViewModelList.add(OptionsStoryTextViewViewModel())

    itemViewModelList.add(OptionsAppLanguageViewModel())

    itemViewModelList.add(OptionsAudioLanguageViewModel())

    return itemViewModelList
  }
}
