package org.oppia.android.app.topic.practice.practiceitemviewmodel

import org.oppia.android.app.model.EphemeralSubtopic
import org.oppia.android.app.model.Subtopic
import org.oppia.android.domain.translation.TranslationController

/** Subtopic view model for the recycler view in [TopicPracticeFragment]. */
class TopicPracticeSubtopicViewModel(
  ephemeralSubtopic: EphemeralSubtopic,
  translationController: TranslationController
) : TopicPracticeItemViewModel() {
  val subtopic: Subtopic = ephemeralSubtopic.subtopic
  val subtopicTitle by lazy {
    translationController.extractString(subtopic.title, ephemeralSubtopic.writtenTranslationContext)
  }
}
