package org.oppia.android.app.topic.studyguide

import androidx.databinding.ObservableBoolean

/**
 * [StudyGuideItemViewModel] for the heading shown above a study guide section.
 *
 * @property sectionIndex the zero-based index of this section within the study guide
 * @property headingText the localized heading text
 * @property isExpanded the [ObservableBoolean] tracking whether this section is expanded
 */
class StudyGuideSectionHeadingViewModel(
  val sectionIndex: Int,
  val headingText: String,
  val isExpanded: ObservableBoolean
) : StudyGuideItemViewModel() {

  private var expansionToggleListener: ((Int) -> Unit)? = null

  /** Registers a callback to be notified when this heading is clicked and expanded state toggles. */
  fun setExpansionToggleListener(listener: (Int) -> Unit) {
    this.expansionToggleListener = listener
  }

  /** Toggles the expanded state of this section. */
  fun onHeadingClicked() {
    isExpanded.set(!isExpanded.get())
    expansionToggleListener?.invoke(sectionIndex)
  }
}
