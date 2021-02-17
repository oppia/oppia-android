package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.StoryHtmlParserEntityType
import javax.inject.Inject

/** [ViewModel] for displaying a recently played story. */
class RecentlyPlayedViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val topicListController: TopicListController,
  private val internalProfileId: Int,
  @StoryHtmlParserEntityType private val entityType: String
) : ObservableViewModel() {

  private val itemList: MutableList<RecentlyPlayedItemViewModel> = ArrayList()
  var recentStoryCount = 0
  var oldStoryCount = 0
  var suggestedStoryCount = 2
  private val ongoingStoryListSummaryResultLiveData:
    LiveData<AsyncResult<PromotedActivityList>>
    by lazy {
      topicListController.getPromotedActivityList(
        ProfileId.newBuilder().setInternalId(internalProfileId).build()
      ).toLiveData()
    }

  val ongoingStoryLiveData: LiveData<List<RecentlyPlayedItemViewModel>> by lazy {
    Transformations.map(ongoingStoryListSummaryResultLiveData, ::processOngoingStoryList)
  }

  private fun processOngoingStoryList(promotedActivityList: AsyncResult<PromotedActivityList>): List<RecentlyPlayedItemViewModel> {
    if (promotedActivityList.isSuccess()) {
      if (promotedActivityList.getOrThrow().promotedStoryList.recentlyPlayedStoryList.isNotEmpty()) {
        val recentSectionTitleViewModel =
          SectionTitleViewModel(activity.getString(R.string.ongoing_story_last_week), false)
        itemList.add(recentSectionTitleViewModel)
        for (promotedStory in promotedActivityList.getOrThrow().promotedStoryList.recentlyPlayedStoryList) {
          val ongoingStoryViewModel =
            OngoingStoryViewModel(
              promotedStory,
              entityType,
              fragment as OngoingStoryClickListener
            )
          itemList.add(ongoingStoryViewModel)
        }
      }
    }

    if (promotedActivityList.getOrThrow().promotedStoryList.olderPlayedStoryList.isNotEmpty()) {
      val showDivider = itemList.isNotEmpty()
      val olderSectionTitleViewModel =
        SectionTitleViewModel(
          activity.getString(R.string.ongoing_story_last_month),
          showDivider
        )
      itemList.add(olderSectionTitleViewModel)
      for (promotedStory in promotedActivityList.getOrThrow().promotedStoryList.olderPlayedStoryList) {
        val ongoingStoryViewModel =
          OngoingStoryViewModel(
            promotedStory,
            entityType,
            fragment as OngoingStoryClickListener
          )
        itemList.add(ongoingStoryViewModel)
      }
    }

    if (promotedActivityList.getOrThrow().promotedStoryList.suggestedStoryList.isNotEmpty()) {
      val showDivider = itemList.isNotEmpty()
      val recommendedSectionTitleViewModel =
        SectionTitleViewModel(
          activity.getString(R.string.recommended_stories),
          showDivider
        )
      itemList.add(recommendedSectionTitleViewModel)
      for (suggestedStory in promotedActivityList.getOrThrow().promotedStoryList.suggestedStoryList) {
        val ongoingStoryViewModel =
          OngoingStoryViewModel(
            suggestedStory,
            entityType,
            fragment as OngoingStoryClickListener
          )
        itemList.add(ongoingStoryViewModel)
      }
      recentStoryCount =
        promotedActivityList.getOrThrow().promotedStoryList.recentlyPlayedStoryCount
      oldStoryCount = promotedActivityList.getOrThrow().promotedStoryList.olderPlayedStoryCount
      suggestedStoryCount = promotedActivityList.getOrThrow().promotedStoryList.suggestedStoryCount
    }

    return itemList
  }

  fun createLayoutManager(
    recentStoryCount: Int,
    oldStoryCount: Int,
    suggestedStoryCount: Int
  ): RecyclerView.LayoutManager {
    val sectionTitle0Position = if (recentStoryCount == 0) {
      // If recent story count is 0, that means that section title 0 will not be visible.
      -1
    } else {
      0
    }
    val sectionTitle1Position = if (oldStoryCount == 0) {
      // If old story count is 0, that means that section title 1 will not be visible.
      -1
    } else if (recentStoryCount == 0) {
      0
    } else {
      recentStoryCount + 1
    }
    val sectionTitle2Position = when {
      suggestedStoryCount == 0 -> {
        -1 // If suggested story count is 0, that means that section title 1 will not be visible.
      }
      oldStoryCount == 0 && recentStoryCount == 0 -> {
        0
      }
      oldStoryCount > 0 && recentStoryCount > 0 -> {
        recentStoryCount + oldStoryCount + 2
      }
      else -> {
        recentStoryCount + oldStoryCount + 1
      }
    }

    val spanCount = activity.resources.getInteger(R.integer.recently_played_span_count)

    val layoutManager = GridLayoutManager(activity.applicationContext, spanCount)
    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
      override fun getSpanSize(position: Int): Int {
        return when (position) {
          sectionTitle0Position, sectionTitle1Position, sectionTitle2Position -> {
            /* number of spaces this item should occupy = */ spanCount
          }
          else -> {
            /* number of spaces this item should occupy = */ 1
          }
        }
      }
    }
    return layoutManager
  }
}