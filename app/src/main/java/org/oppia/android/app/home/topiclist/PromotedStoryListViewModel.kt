package org.oppia.android.app.home.topiclist

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.home.HomeItemViewModel
import org.oppia.android.app.home.RouteToRecentlyPlayedListener
import org.oppia.android.app.model.CompletedStoryList
import org.oppia.android.app.model.OngoingStoryList
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.recyclerview.StartSnapHelper
import org.oppia.android.app.shim.IntentFactoryShim
import org.oppia.android.databinding.PromotedStoryListBinding
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.parser.StoryHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] promoted story list in [HomeFragment]. */
class PromotedStoryListViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val internalProfileId: Int,
  private val intentFactoryShim: IntentFactoryShim,
  private val topicListController: TopicListController,
  @StoryHtmlParserEntityType private val storyEntityType: String
) :
  HomeItemViewModel(),
  RouteToRecentlyPlayedListener {
  private val limit = activity.resources.getInteger(R.integer.promoted_story_list_limit)
  val paddingEnd =
    (activity as Context).resources.getDimensionPixelSize(R.dimen.home_padding_end)
  val paddingStart =
    (activity as Context).resources.getDimensionPixelSize(R.dimen.home_padding_start)

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

  val promotedStoryListLiveData: LiveData<List<PromotedStoryViewModel>> by lazy {
    Transformations.map(assumedSuccessfulOngoingStoryListLiveData, ::processOngoingStoryList)
  }

  private fun processOngoingStoryList(ongoingStoryList: OngoingStoryList) : List<PromotedStoryViewModel> {
    var newPromotedStoryList: MutableList<PromotedStoryViewModel> = ArrayList()
    if (ongoingStoryList.recentStoryCount != 0) {
      ongoingStoryList.recentStoryList.take(limit)
        .forEach { promotedStory ->
          val recentStory = PromotedStoryViewModel(
            activity,
            internalProfileId,
            storyEntityType,
            intentFactoryShim
          )
          recentStory.setPromotedStory(promotedStory)
          recentStory.setStoryCount(ongoingStoryList.recentStoryCount)
          newPromotedStoryList.add(recentStory)
      }
    } else {
      // TODO(#936): Optimise this as part of recommended stories.
      ongoingStoryList.olderStoryList.take(limit)
        .forEach { promotedStory ->
          val oldStory = PromotedStoryViewModel(
            activity,
            internalProfileId,
            storyEntityType,
            intentFactoryShim
          )
          oldStory.setPromotedStory(promotedStory)
          oldStory.setStoryCount(ongoingStoryList.olderStoryCount)
          newPromotedStoryList.add(oldStory)
        }
      }
    return newPromotedStoryList
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


//    /*
//     * The StartSnapHelper is used to snap between items rather than smooth scrolling,
//     * so that the item is completely visible in [HomeFragment] as soon as learner lifts the finger after scrolling.
//     */
//    val snapHelper = StartSnapHelper()
//    binding.promotedStoryListRecyclerView.layoutManager = horizontalLayoutManager
//    binding.promotedStoryListRecyclerView.setOnFlingListener(null)
//    snapHelper.attachToRecyclerView(binding.promotedStoryListRecyclerView)
//  }
}
