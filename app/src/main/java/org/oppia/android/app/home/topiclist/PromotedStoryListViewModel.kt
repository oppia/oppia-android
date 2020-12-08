package org.oppia.android.app.home.topiclist

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.android.R
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.OngoingStoryList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicList
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.StoryHtmlParserEntityType

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel(
  private var activity: AppCompatActivity,
  private var internalProfileId: Int,
  private var intentFactoryShim: IntentFactoryShim,
  private val topicListController: TopicListController,
  @StoryHtmlParserEntityType private val storyEntityType: String
) :
  HomeItemViewModel(),
  RouteToRecentlyPlayedListener {
  val limit = activity.resources.getInteger(R.integer.promoted_story_list_limit)

  val promotedStoryListLiveData: LiveData<MutableList<PromotedStoryViewModel>> by lazy {
    Transformations.map(assumedSuccessfulOngoingStoryListLiveData, ::processList)
  }

  private fun processList(it: OngoingStoryList): MutableList<PromotedStoryViewModel> {
    var newPromotedStoryList: MutableList<PromotedStoryViewModel> = ArrayList()
    if (it.recentStoryCount != 0) {
      it.recentStoryList.take(limit)
        .forEach { promotedStory ->
          val recentStory = PromotedStoryViewModel(
            activity,
            internalProfileId,
            storyEntityType,
            intentFactoryShim
          )
          recentStory.setPromotedStory(promotedStory)
          newPromotedStoryList.add(recentStory)
      }
    } else {
      // TODO(#936): Optimise this as part of recommended stories.
      it.olderStoryList.take(limit)
        .forEach { promotedStory ->
          val oldStory = PromotedStoryViewModel(
            activity,
            internalProfileId,
            storyEntityType,
            intentFactoryShim
          )
          oldStory.setPromotedStory(promotedStory)
          newPromotedStoryList.add(oldStory)
        }
      }
    return newPromotedStoryList
  }

  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>>
  by lazy {
      topicListController.getOngoingStoryList(
          ProfileId.newBuilder().setInternalId(internalProfileId).build()
        ).toLiveData()
    }

  private val assumedSuccessfulOngoingStoryListLiveData: LiveData<OngoingStoryList> by lazy {
    // If there's an error loading the data, assume the default.
    Transformations.map(ongoingStoryListSummaryResultLiveData) {
      it.getOrDefault(
        OngoingStoryList.getDefaultInstance()
      )
    }
  }

  fun clickOnViewAll() {
    routeToRecentlyPlayed()
  }

  override fun routeToRecentlyPlayed() {
    val intent = intentFactoryShim.createRecentlyPlayedActivityIntent(
      activity.applicationContext,
      internalProfileId
    )
    activity.startActivity(intent)
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setActivity(activity: AppCompatActivity) {
    this.activity = activity;
  }

  fun setIntentFactoryShim(intentFactoryShim: IntentFactoryShim) {
    this.intentFactoryShim = intentFactoryShim
  }
}
