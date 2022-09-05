package org.oppia.android.app.ongoingtopiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.model.EphemeralTopic
import org.oppia.android.app.model.Topic
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.translation.TranslationController

/** [ViewModel] for displaying topic item in [OngoingTopicListActivity]. */
class OngoingTopicItemViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  ephemeralTopic: EphemeralTopic,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim,
  private val resourceHandler: AppLanguageResourceHandler,
  translationController: TranslationController
) : ObservableViewModel(), RouteToTopicListener {
  val topic = ephemeralTopic.topic

  val topicTitle: String by lazy {
    translationController.extractString(topic.title, ephemeralTopic.writtenTranslationContext)
  }

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
