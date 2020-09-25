package org.oppia.android.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.model.Topic
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying topic item in [OngoingTopicListActivity]. */
class OngoingTopicItemViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val topic: Topic,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim
) :
  ObservableViewModel(),
  RouteToTopicListener {

  fun onTopicItemClicked() {
    routeToTopic(internalProfileId, topic.topicId)
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
