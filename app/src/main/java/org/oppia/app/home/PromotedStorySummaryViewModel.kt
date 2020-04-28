package org.oppia.app.home

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import org.oppia.app.model.PromotedStory
import org.oppia.app.topic.TopicActivity

/** PromotedStory item [ViewModel] for the promoted story recycler view in [HomeFragment]. */
class PromotedStorySummaryViewModel(
  val promotedStory: PromotedStory,
  private val activity: AppCompatActivity,
  private val internalProfileId: Int) : HomeItemViewModel(),RouteToTopicPlayStoryListener {

  val promotedStoryObservable = ObservableField<PromotedStory>()

  /*fun setPromotedStory(promotedStory: PromotedStory) {
    promotedStoryObservable.set(promotedStory)
  }*/

  fun clickOnStoryTile() {
    routeToTopicPlayStory(
      internalProfileId,
      promotedStoryObservable.get()!!.topicId,
      promotedStoryObservable.get()!!.storyId
    )
  }
  override fun routeToTopicPlayStory(internalProfileId: Int, topicId: String, storyId: String) {
    activity.startActivity(
      TopicActivity.createTopicPlayStoryActivityIntent(
        activity.applicationContext,
        internalProfileId,
        topicId,
        storyId
      )
    )
  }
}