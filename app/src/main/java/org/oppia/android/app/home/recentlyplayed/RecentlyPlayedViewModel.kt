package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import javax.inject.Inject
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType

@FragmentScope
class RecentlyPlayedViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val topicListController: TopicListController,
  @StoryHtmlParserEntityType private val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
) {

  private var internalProfileId: Int = -1

  val recentlyPlayedLiveData: LiveData<List<RecentlyPlayedItemViewModel>> by lazy {
    Transformations.map(promotedActivityListLiveData, ::processPromotedStoryList)
  }

  private val promotedActivityListLiveData: LiveData<PromotedActivityList> by lazy {
    getAssumedSuccessfulPromotedActivityList()
  }

  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  private val promotedStoryListSummaryResultLiveData:
    LiveData<AsyncResult<PromotedActivityList>>
    by lazy {
      topicListController.getPromotedActivityList(
        ProfileId.newBuilder().setInternalId(internalProfileId).build()
      ).toLiveData()
    }

  private fun getAssumedSuccessfulPromotedActivityList(): LiveData<PromotedActivityList> {
    return Transformations.map(promotedStoryListSummaryResultLiveData) {
      when (it) {
        // If there's an error loading the data, assume the default.
        is AsyncResult.Failure, is AsyncResult.Pending -> PromotedActivityList.getDefaultInstance()
        is AsyncResult.Success -> it.value
      }
    }
  }

  private fun processPromotedStoryList(promotedActivityList: PromotedActivityList): List<RecentlyPlayedItemViewModel> {
    val itemList: MutableList<RecentlyPlayedItemViewModel> = mutableListOf()
    if (promotedActivityList.promotedStoryList.recentlyPlayedStoryList.isNotEmpty()) {
      addRecentlyPlayedStoryListSection(
        promotedActivityList.promotedStoryList.recentlyPlayedStoryList,
        itemList
      )
    }

    if (promotedActivityList.promotedStoryList.olderPlayedStoryList.isNotEmpty()) {
      addOlderStoryListSection(
        promotedActivityList.promotedStoryList.olderPlayedStoryList,
        itemList
      )
    }

    if (promotedActivityList.promotedStoryList.suggestedStoryList.isNotEmpty()) {
      addRecommendedStoryListSection(
        promotedActivityList.promotedStoryList.suggestedStoryList,
        itemList
      )
    }
    return itemList
  }

  private fun addRecentlyPlayedStoryListSection(
    recentlyPlayedStoryList: MutableList<PromotedStory>,
    itemList: MutableList<RecentlyPlayedItemViewModel>
  ) {
    val recentSectionTitleViewModel =
      SectionTitleViewModel(
        resourceHandler.getStringInLocale(R.string.ongoing_story_last_week), false
      )
    itemList.add(recentSectionTitleViewModel)
    recentlyPlayedStoryList.forEachIndexed { index, promotedStory ->
      val ongoingStoryViewModel = createOngoingStoryViewModel(promotedStory, index)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun addOlderStoryListSection(
    olderPlayedStoryList: List<PromotedStory>,
    itemList: MutableList<RecentlyPlayedItemViewModel>
  ) {
    val showDivider = itemList.isNotEmpty()
    val olderSectionTitleViewModel =
      SectionTitleViewModel(
        resourceHandler.getStringInLocale(R.string.ongoing_story_last_month),
        showDivider
      )
    itemList.add(olderSectionTitleViewModel)
    olderPlayedStoryList.forEachIndexed { index, promotedStory ->
      val ongoingStoryViewModel = createOngoingStoryViewModel(promotedStory, index)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun addRecommendedStoryListSection(
    suggestedStoryList: List<PromotedStory>,
    itemList: MutableList<RecentlyPlayedItemViewModel>
  ) {
    val showDivider = itemList.isNotEmpty()
    val recommendedSectionTitleViewModel =
      SectionTitleViewModel(
        resourceHandler.getStringInLocale(R.string.recommended_stories),
        showDivider
      )
    itemList.add(recommendedSectionTitleViewModel)
    suggestedStoryList.forEachIndexed { index, suggestedStory ->
      val ongoingStoryViewModel = createOngoingStoryViewModel(suggestedStory, index)
      itemList.add(ongoingStoryViewModel)
    }
  }

  private fun createOngoingStoryViewModel(
    promotedStory: PromotedStory,
    index: Int
  ): RecentlyPlayedItemViewModel {
    return PromotedStoryViewModel(
      activity,
      promotedStory,
      entityType,
      fragment as PromotedStoryClickListener,
      index,
      resourceHandler,
      translationController
    )
  }
}