package org.oppia.android.app.home.recentlyplayed

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.R
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.PromotedActivityList
import org.oppia.android.app.model.PromotedStory
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import org.oppia.android.util.platformparameter.EnableMultipleClassrooms
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

/** View model for [RecentlyPlayedFragment]. */
class RecentlyPlayedViewModel private constructor(
  private val activity: AppCompatActivity,
  private val topicListController: TopicListController,
  @StoryHtmlParserEntityType private val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private val enableMultipleClassrooms: PlatformParameterValue<Boolean>,
  private val promotedStoryClickListener: PromotedStoryClickListener,
  private val profileId: ProfileId,
) {

  /** Factory of RecentlyPlayedViewModel. */
  class Factory @Inject constructor(
    private val activity: AppCompatActivity,
    private val topicListController: TopicListController,
    @StoryHtmlParserEntityType private val entityType: String,
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController,
    @EnableMultipleClassrooms
    private val enableMultipleClassrooms: PlatformParameterValue<Boolean>,
  ) {

    /** Creates an instance of [RecentlyPlayedViewModel]. */
    fun create(
      promotedStoryClickListener: PromotedStoryClickListener,
      profileId: ProfileId
    ): RecentlyPlayedViewModel {
      return RecentlyPlayedViewModel(
        activity,
        topicListController,
        entityType,
        resourceHandler,
        translationController,
        enableMultipleClassrooms,
        promotedStoryClickListener,
        profileId,
      )
    }
  }

  /**
   * [LiveData] with the list of recently played items for a ProfileId, organized in sections.
   */
  val recentlyPlayedItems: LiveData<List<RecentlyPlayedItemViewModel>> by lazy {
    Transformations.map(promotedActivityListLiveData, ::processPromotedStoryList)
  }

  private val promotedActivityListLiveData: LiveData<PromotedActivityList> by lazy {
    getAssumedSuccessfulPromotedActivityList()
  }

  private val promotedStoryListSummaryResultLiveData:
    LiveData<AsyncResult<PromotedActivityList>>
    by lazy {
      topicListController.getPromotedActivityList(profileId).toLiveData()
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

  private fun processPromotedStoryList(
    promotedActivityList: PromotedActivityList
  ): List<RecentlyPlayedItemViewModel> {
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
      promotedStoryClickListener,
      index,
      resourceHandler,
      enableMultipleClassrooms.value,
      translationController
    )
  }
}
