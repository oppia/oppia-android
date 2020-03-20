package org.oppia.app.topic.revision

import org.oppia.app.model.Subtopic

/** Listener for when a subtopic is selected for review. */
interface RevisionSubtopicSelector {
  fun onTopicRevisionSummaryClicked(subtopic: Subtopic)
}
