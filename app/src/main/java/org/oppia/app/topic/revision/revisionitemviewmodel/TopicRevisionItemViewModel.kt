package org.oppia.app.topic.revision.revisionitemviewmodel

import androidx.lifecycle.ViewModel
import org.oppia.app.model.Subtopic
import org.oppia.app.topic.revision.RevisionSubtopicSelector

/** [ViewModel] for view present in the recycler view in [TopicReviewFragment]. */
class TopicRevisionItemViewModel(
  val subtopic: Subtopic,
  val onReviewItemPressed: RevisionSubtopicSelector
) : ViewModel()
