package org.oppia.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.app.home.RouteToTopicListener
import org.oppia.app.model.Topic
import org.oppia.app.topic.TopicActivity

/** [ViewModel] for displaying topic item in [OngoingTopicListActivity]. */
class OngoingTopicItemViewModel(
  val activity: AppCompatActivity,
  val internalProfileId: Int,
  val topic: Topic,
  val entityType: String
) : ViewModel(), RouteToTopicListener {
  fun onTopicItemClicked() {
    routeToTopic(internalProfileId, topic.topicId)
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {
    val intent = TopicActivity.createTopicActivityIntent(
      activity.applicationContext,
      internalProfileId,
      topicId
    )
    activity.startActivity(intent)
  }
}
