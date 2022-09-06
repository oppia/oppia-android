package org.oppia.android.app.topic.practice.practiceitemviewmodel

import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.Subtopic
import org.oppia.android.domain.translation.TranslationController

/** Subtopic view model for the recycler view in [TopicPracticeFragment]. */
class TopicPracticeSubtopicViewModel(
  ephemeralSubtopic: EphemeralSubtopic,
  translationController: TranslationController
) : TopicPracticeItemViewModel() {
  /** The subtopic being displayed. */
  val subtopic: Subtopic = ephemeralSubtopic.subtopic

  /** The localized title of the subtopic being displayed. */
  val subtopicTitle by lazy {
    translationController.extractString(subtopic.title, ephemeralSubtopic.writtenTranslationContext)
  }
}
