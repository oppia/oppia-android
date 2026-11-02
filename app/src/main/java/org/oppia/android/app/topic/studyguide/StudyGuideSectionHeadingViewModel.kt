package org.oppia.android.app.topic.studyguide

import androidx.databinding.ObservableBoolean

/**
 * [StudyGuideItemViewModel] for the heading shown above a study guide section.
 *
 * @property headingText the localized heading text
 * @property isExpanded the [ObservableBoolean] tracking whether this section is expanded
 */
class StudyGuideSectionHeadingViewModel(
  val headingText: String,
  val isExpanded: ObservableBoolean
) : StudyGuideItemViewModel() {

  /** Toggles the expanded state of this section. */
  fun onHeadingClicked() {
    isExpanded.set(!isExpanded.get())
  }
}
