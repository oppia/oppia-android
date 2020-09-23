package org.oppia.app.completedstorylist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.home.RouteToTopicPlayStoryListener
import org.oppia.app.model.CompletedStory
import org.oppia.app.shim.IntentFactoryShim
import org.oppia.app.viewmodel.ObservableViewModel

/** Completed story view model for the recycler view in [CompletedStoryListFragment]. */
class CompletedStoryItemViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val completedStory: CompletedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim
) : ObservableViewModel(), RouteToTopicPlayStoryListener {

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
