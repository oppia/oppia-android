package org.oppia.app.topic.revision

import org.oppia.app.model.Subtopic

/** Listener for when a subtopic is selected for revision. */
interface RevisionSubtopicSelector {
  fun onTopicRevisionSummaryClicked(subtopic: Subtopic)
}
