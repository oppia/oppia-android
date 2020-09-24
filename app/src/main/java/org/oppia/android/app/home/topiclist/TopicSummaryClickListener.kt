package org.oppia.android.app.home.topiclist

import org.oppia.android.app.model.TopicSummary

/** Listener interface for when topic summaries are clicked in the UI. */
interface TopicSummaryClickListener {
  fun onTopicSummaryClicked(topicSummary: TopicSummary)
}
