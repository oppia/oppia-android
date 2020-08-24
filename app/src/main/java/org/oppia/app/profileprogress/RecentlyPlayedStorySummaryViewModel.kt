package org.oppia.app.profileprogress

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.home.RouteToTopicPlayStoryListener
import org.oppia.app.model.PromotedStory
import org.oppia.app.shim.IntentFactoryShim

/** Recently played item [ViewModel] for the recycler view in [ProfileProgressFragment]. */
class RecentlyPlayedStorySummaryViewModel(
  val activity: AppCompatActivity,
  val internalProfileId: Int,
  val promotedStory: PromotedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim
) : ProfileProgressItemViewModel(), RouteToTopicPlayStoryListener {
  fun onStoryItemClicked() {
    routeToTopicPlayStory(internalProfileId, promotedStory.topicId, promotedStory.storyId)
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
