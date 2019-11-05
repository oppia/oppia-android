package org.oppia.app.home.continueplaying

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel

/** [ViewModel] for section title in [ContinuePlayingFragment]. */
class SectionTitleViewModel : ContinuePlayingViewModel() {
  val sectionTitleTextObservable = ObservableField<String>("")

  fun setSectionTitleText(sectionTitleText: String) {
    sectionTitleTextObservable.set(sectionTitleText)
  }
}
