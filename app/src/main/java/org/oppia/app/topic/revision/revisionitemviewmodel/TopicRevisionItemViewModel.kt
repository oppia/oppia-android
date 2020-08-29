package org.oppia.app.topic.revision.revisionitemviewmodel

import org.oppia.app.model.Subtopic
import org.oppia.app.topic.revision.RevisionSubtopicSelector
import org.oppia.app.viewmodel.ObservableViewModel

/** [ViewModel] for child views of recycler view present in the [TopicRevisionFragment]. */
class TopicRevisionItemViewModel(
  val topicId: String,
  val subtopic: Subtopic,
  val entityType: String,
  val onRevisionItemPressed: RevisionSubtopicSelector
) : ObservableViewModel()
