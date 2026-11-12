package org.oppia.android.app.topic.studyguide

import androidx.databinding.ObservableBoolean

/**
 * [StudyGuideItemViewModel] for the content shown within a study guide section.
 *
 * @property contentHtml the section's content as HTML, parsed and displayed by the presenter
 * @property isExpanded the [ObservableBoolean] tracking whether this section is expanded
 */
class StudyGuideSectionContentViewModel(
  val contentHtml: String,
  val isExpanded: ObservableBoolean
) : StudyGuideItemViewModel()
