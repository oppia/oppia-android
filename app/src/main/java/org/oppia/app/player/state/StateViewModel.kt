package org.oppia.app.player.state

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import javax.inject.Inject

/** [ViewModel] for state-fragment. */
@FragmentScope
class StateViewModel @Inject constructor() : ViewModel() {
  var isAudioFragmentVisible = ObservableField<Boolean>(false)
  var isContinueButtonVisible = ObservableField<Boolean>(false)
  var isEndExplorationButtonVisible = ObservableField<Boolean>(false)
  var isLearnAgainButtonVisible = ObservableField<Boolean>(false)
  var isNextButtonVisible = ObservableField<Boolean>(false)
  var isPreviousButtonVisible = ObservableField<Boolean>(false)
  var isSubmitButtonVisible = ObservableField<Boolean>(false)
  var isSubmitButtonActive = ObservableField<Boolean>(false)

  fun hideAllButtons() {
    isContinueButtonVisible.set(false)
    isEndExplorationButtonVisible.set(false)
    isLearnAgainButtonVisible.set(false)
    isNextButtonVisible.set(false)
    isPreviousButtonVisible.set(false)
    isSubmitButtonVisible.set(false)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    isAudioFragmentVisible.set(isVisible)
  }

  fun setContinueButtonVisible(isVisible: Boolean) {
    isContinueButtonVisible.set(isVisible)
  }

  fun setEndExplorationButtonVisible(isVisible: Boolean) {
    isEndExplorationButtonVisible.set(isVisible)
  }

  fun setLearnAgainButtonVisible(isVisible: Boolean) {
    isLearnAgainButtonVisible.set(isVisible)
  }

  fun setNextButtonVisible(isVisible: Boolean) {
    isNextButtonVisible.set(isVisible)
  }

  fun setPreviousButtonVisible(isVisible: Boolean) {
    isPreviousButtonVisible.set(isVisible)
  }

  fun setSubmitButtonVisible(isVisible: Boolean) {
    isSubmitButtonVisible.set(isVisible)
  }

  fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    if (s.isNotEmpty()) {
      isSubmitButtonActive.set(true)
    } else {
      isSubmitButtonActive.set(false)
    }
  }
}
