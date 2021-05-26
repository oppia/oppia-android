package org.oppia.android.app.mydownloads.downloads

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.util.parser.html.TopicHtmlParserEntityType

/** [ViewModel] for title in [DownloadsFragment]. */
class DownloadsTopicViewModel(
  fragment: Fragment,
  val topicSummary: TopicSummary,
  @TopicHtmlParserEntityType val topicEntityType: String,
  val topicSize: String,
  private val internalProfileId: Int
) : DownloadsItemViewModel() {

  private val routeToTopicListener = fragment as RouteToTopicListener

  fun onTopicItemClicked() {
    routeToTopicListener.routeToTopic(internalProfileId, topicSummary.topicId)
  }
}
