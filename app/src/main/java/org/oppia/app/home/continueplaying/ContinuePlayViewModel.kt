package org.oppia.app.home.continueplaying

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.model.OngoingStoryList
import org.oppia.domain.topic.TopicListController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

// TODO(#297): Add download status information to promoted-story-card.

/** [ViewModel] for displaying a promoted story. */
class ContinuePlayViewModel @Inject constructor(
  private val fragment: Fragment,
  private val topicListController: TopicListController
) : ContinuePlayingItemViewModel() {

  private val itemList: MutableList<ContinuePlayingItemViewModel> = ArrayList()

  private val ongoingStoryListSummaryResultLiveData: LiveData<AsyncResult<OngoingStoryList>> by lazy {
    topicListController.getOngoingStoryList()
  }

  val ongoingStoryLiveData: LiveData<List<ContinuePlayingItemViewModel>>by lazy {
    Transformations.map(ongoingStoryListSummaryResultLiveData, ::processOngoingStoryList)
  }

  private fun processOngoingStoryList(ongoingStoryList: AsyncResult<OngoingStoryList>): List<ContinuePlayingItemViewModel> {
    if (ongoingStoryList.isSuccess()) {
      if (ongoingStoryList.getOrThrow().recentStoryList.isNotEmpty()) {
        val recentSectionTitleViewModel =
          SectionTitleViewModel(fragment.getString(R.string.ongoing_story_last_week), false)
        itemList.add(recentSectionTitleViewModel)
        for (promotedStory in ongoingStoryList.getOrThrow().recentStoryList) {
          val ongoingStoryViewModel = OngoingStoryViewModel(promotedStory, fragment as OngoingStoryClickListener)
          itemList.add(ongoingStoryViewModel)
        }
      }

      if (ongoingStoryList.getOrThrow().olderStoryList.isNotEmpty()) {
        val showDivider = itemList.isNotEmpty()
        val olderSectionTitleViewModel =
          SectionTitleViewModel(fragment.getString(R.string.ongoing_story_last_month), showDivider)
        itemList.add(olderSectionTitleViewModel)
        for (promotedStory in ongoingStoryList.getOrThrow().olderStoryList) {
          val ongoingStoryViewModel = OngoingStoryViewModel(promotedStory, fragment as OngoingStoryClickListener)
          itemList.add(ongoingStoryViewModel)
        }
      }
    }
    return itemList
  }
}
