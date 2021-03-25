package org.oppia.android.app.mydownloads.downloads

import androidx.lifecycle.ViewModel
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.util.parser.TopicHtmlParserEntityType

/** [ViewModel] for title in [DownloadsFragment]. */
class DownloadsTopicViewModel(
  val topicSummary: TopicSummary,
  @TopicHtmlParserEntityType val topicEntityType: String
) : DownloadsItemViewModel() {
  val name: String = topicSummary.name
  val totalChapterCount: Int = topicSummary.totalChapterCount
}
