package org.oppia.android.app.mydownloads.downloads

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.model.TopicSummary
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.util.parser.TopicHtmlParserEntityType

/** [ViewModel] for title in [DownloadsFragment]. */
class DownloadsTopicViewModel(
  private val activity: AppCompatActivity,
  val topicSummary: TopicSummary,
  @TopicHtmlParserEntityType val topicEntityType: String,
  val topicSize: String,
  private val intentFactoryShim: IntentFactoryShim,
  private val internalProfileId: Int
) : DownloadsItemViewModel(),
  RouteToTopicListener {

  fun onTopicItemClicked() {
    routeToTopic(internalProfileId, topicSummary.topicId)
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {
    val intent = intentFactoryShim.createTopicActivityIntent(
      activity.applicationContext,
      internalProfileId,
      topicId
    )
    activity.startActivity(intent)
  }
}
