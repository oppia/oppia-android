package org.oppia.app.home.topiclist

import org.oppia.app.model.TopicSummary

/** Listener interface for when topic summaries are clicked in the UI. */
interface TopicSummaryClickListener {
  fun onTopicSummaryClicked(topicSummary: TopicSummary)
}
