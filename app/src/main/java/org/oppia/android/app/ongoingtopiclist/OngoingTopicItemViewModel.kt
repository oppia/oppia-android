package org.oppia.android.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.model.Topic
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/** [ViewModel] for displaying topic item in [OngoingTopicListActivity]. */
class OngoingTopicItemViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val topic: Topic,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel(), RouteToTopicListener {

  fun onTopicItemClicked() {
    routeToTopic(internalProfileId, topic.topicId)
  }

  fun computeStoryCountText(): String {
    return resourceHandler.getQuantityStringInLocaleWithWrapping(
      R.plurals.lesson_count, topic.storyCount, topic.storyCount.toString()
    )
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
