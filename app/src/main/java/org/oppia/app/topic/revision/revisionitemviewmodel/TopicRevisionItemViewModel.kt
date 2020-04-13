package org.oppia.app.topic.revision.revisionitemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.topic.revision.RevisionSubtopicSelector
import org.oppia.app.topic.revision.TopicRevisionFragment
import org.oppia.app.model.Subtopic

/** [ViewModel] for child views of recycler view present in the [TopicRevisionFragment]. */
class TopicRevisionItemViewModel(
  val subtopic: Subtopic,
  val onRevisionItemPressed: RevisionSubtopicSelector
) : ViewModel()
