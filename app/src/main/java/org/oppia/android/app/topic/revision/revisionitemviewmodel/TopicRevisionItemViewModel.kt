package org.oppia.android.app.topic.revision.revisionitemviewmodel

import org.oppia.android.app.model.Subtopic
import org.oppia.android.app.topic.revision.RevisionSubtopicSelector
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ObservableViewModel] for child views of recycler view present in the [TopicRevisionFragment]. */
class TopicRevisionItemViewModel(
  val topicId: String,
  val subtopic: Subtopic,
  val entityType: String,
  val onRevisionItemPressed: RevisionSubtopicSelector
) : ObservableViewModel()
