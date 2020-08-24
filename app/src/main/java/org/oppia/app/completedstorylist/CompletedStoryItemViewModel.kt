package org.oppia.app.completedstorylist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import org.oppia.app.home.RouteToTopicPlayStoryListener
import org.oppia.app.model.CompletedStory
import org.oppia.app.shim.IntentFactoryShim

/** Completed story view model for the recycler view in [CompletedStoryListFragment]. */
class CompletedStoryItemViewModel(
  val activity: AppCompatActivity,
  val internalProfileId: Int,
  val completedStory: CompletedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim
) : ViewModel(), RouteToTopicPlayStoryListener {
  fun onCompletedStoryItemClicked() {
    routeToTopicPlayStory(internalProfileId, completedStory.topicId, completedStory.storyId)
  }

  override fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String) {
    val intent = intentFactoryShim.createTopicPlayStoryActivityIntent(
      activity.applicationContext,
      internalProfileId,
      topicId,
      storyId
    )
    activity.startActivity(intent)
  }
}
