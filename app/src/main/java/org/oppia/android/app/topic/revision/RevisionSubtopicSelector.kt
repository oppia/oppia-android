package org.oppia.android.app.topic.revision

import org.oppia.android.app.model.Subtopic

/** Listener for when a subtopic is selected for revision. */
interface RevisionSubtopicSelector {
  fun onTopicRevisionSummaryClicked(subtopic: Subtopic)
}
