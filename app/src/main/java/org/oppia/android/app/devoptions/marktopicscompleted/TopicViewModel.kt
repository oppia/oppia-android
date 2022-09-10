package org.oppia.android.app.devoptions.marktopicscompleted

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController

/** [ViewModel] for displaying a topic for the recyclerView in [MarkTopicsCompletedFragment]. */
class TopicViewModel(
  ephemeralTopic: EphemeralTopic,
  val isCompleted: Boolean,
  translationController: TranslationController
) : ObservableViewModel() {
  /** The summary of the topic being displayed. */
  val topic = ephemeralTopic.topic

  /** The localized title of the topic being displayed. */
  val topicTitle by lazy {
    translationController.extractString(topic.title, ephemeralTopic.writtenTranslationContext)
  }
}
