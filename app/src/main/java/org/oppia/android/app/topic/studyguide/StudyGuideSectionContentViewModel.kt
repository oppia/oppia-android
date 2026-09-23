package org.oppia.android.app.topic.studyguide

/**
 * [StudyGuideItemViewModel] for the content shown within a study guide section.
 *
 * @property contentHtml the section's content as HTML, parsed and displayed by the presenter
 */
class StudyGuideSectionContentViewModel(
  val contentHtml: String
) : StudyGuideItemViewModel()
