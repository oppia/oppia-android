package org.oppia.app.onboarding

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import org.oppia.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/** [ViewModel] for [OnboardingFragment]. */
class OnboardingViewModel @Inject constructor() : ObservableViewModel() {
  val slideNumber = ObservableField<Int>(0)

  fun slideChanged(index: Int) {
    slideNumber.set(index)
  }
}
