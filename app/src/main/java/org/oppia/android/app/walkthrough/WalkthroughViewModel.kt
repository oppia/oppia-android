package org.oppia.android.app.walkthrough

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [WalkthroughActivity]. */
class WalkthroughViewModel @Inject constructor() : ObservableViewModel() {
  val currentProgress = ObservableField(0)
  val isProgressBarVisible = MutableLiveData<Boolean>(true)

  fun changeProgressBarVisibility(visibility: Boolean) {
    isProgressBarVisible.value = visibility
  }
}
