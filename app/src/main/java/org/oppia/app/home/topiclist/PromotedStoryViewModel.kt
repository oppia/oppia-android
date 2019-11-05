package org.oppia.app.home.topiclist

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.oppia.app.home.ContinuePlayingActivity
import org.oppia.app.home.HomeItemViewModel
import org.oppia.app.home.RouteToContinuePlayingListener
import org.oppia.app.home.RouteToTopicPlayStoryListener
import org.oppia.app.model.PromotedStory
import org.oppia.app.topic.TopicActivity

// TODO(#283): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
class PromotedStoryViewModel(private val activity: AppCompatActivity) : HomeItemViewModel(),
  RouteToContinuePlayingListener, RouteToTopicPlayStoryListener {

  /**
   * The retrieved [LiveData] for retrieving topic summaries. This model should ensure only one
   * [LiveData] is used for all subsequent processed data to ensure the transformed [LiveData]s are
   * always in sync.
   */
  val promotedStoryObservable = ObservableField<PromotedStory>()

  fun setPromotedStory(promotedStory: PromotedStory) {
    promotedStoryObservable.set(promotedStory)
  }

  fun clickOnStoryTile(@Suppress("UNUSED_PARAMETER") v: View) {
    routeToTopicPlayStory(promotedStoryObservable.get()!!.topicId, promotedStoryObservable.get()!!.storyId)
  }

  fun clickOnViewAll(@Suppress("UNUSED_PARAMETER") v: View) {
    routeToContinuePlaying()
  }

  override fun routeToTopicPlayStory(topicId: String, storyId: String) {
    activity.startActivity(
      TopicActivity.createTopicPlayStoryActivityIntent(
        activity.applicationContext,
        topicId,
        storyId
      )
    )
  }

  override fun routeToContinuePlaying() {
    activity.startActivity(ContinuePlayingActivity.createContinuePlayingActivityIntent(activity.applicationContext))
  }
}
