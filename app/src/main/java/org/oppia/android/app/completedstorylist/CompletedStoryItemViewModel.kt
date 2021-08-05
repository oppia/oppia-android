package org.oppia.android.app.completedstorylist

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.home.RouteToTopicPlayStoryListener
import org.oppia.android.app.home.RouteToTopicPreviewListener
import org.oppia.android.app.model.CompletedStory
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.app.viewmodel.ObservableViewModel

/** Completed story view model for the recycler view in [CompletedStoryListFragment]. */
class CompletedStoryItemViewModel(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  val completedStory: CompletedStory,
  val entityType: String,
  private val intentFactoryShim: IntentFactoryShim,
  private val myDownloadsFeatureFlagValue: Boolean
) : ObservableViewModel(), RouteToTopicPlayStoryListener {

  private val routeToTopicPreviewListener = activity as RouteToTopicPreviewListener

  fun onCompletedStoryItemClicked() {
    routeToTopicPlayStory(internalProfileId, completedStory.topicId, completedStory.storyId)
  }

  override fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String) {
    if (myDownloadsFeatureFlagValue) {
      routeToTopicPreviewListener.routeToTopicPreview(internalProfileId, topicId)
    } else {
      val intent = intentFactoryShim.createTopicPlayStoryActivityIntent(
        activity.applicationContext,
        internalProfileId,
        topicId,
        storyId
      )
      activity.startActivity(intent)
    }
  }
}
