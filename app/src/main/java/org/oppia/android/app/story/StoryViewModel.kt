package org.oppia.android.app.story

import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.EphemeralStorySummary
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.story.storyitemviewmodel.StoryChapterSummaryViewModel
import org.oppia.android.app.story.storyitemviewmodel.StoryHeaderViewModel
import org.oppia.android.app.story.storyitemviewmodel.StoryItemViewModel
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.parser.html.StoryHtmlParserEntityType
import javax.inject.Inject

/** The ViewModel for StoryFragment. */
@FragmentScope
class StoryViewModel @Inject constructor(
  private val fragment: Fragment,
  private val topicController: TopicController,
  private val explorationCheckpointController: ExplorationCheckpointController,
  private val oppiaLogger: OppiaLogger,
  @StoryHtmlParserEntityType val entityType: String,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController
) {
  private var profileId: ProfileId = ProfileId.newBuilder().setLoggedOut(true).build()
  private lateinit var classroomId: String
  private lateinit var topicId: String

  /** [storyId] needs to be set before any of the live data members can be accessed. */
  private lateinit var storyId: String
  private val explorationSelectionListener = fragment as ExplorationSelectionListener

  private val storyResultLiveData: LiveData<AsyncResult<EphemeralStorySummary>> by lazy {
    topicController.getStory(
      ProfileId.newBuilder()
        .setLoggedInInternalProfileId(profileId.loggedInInternalProfileId).build(),
      topicId,
      storyId
    ).toLiveData()
  }

  private val storyLiveData: LiveData<EphemeralStorySummary> by lazy {
    Transformations.map(storyResultLiveData, ::processStoryResult)
  }

  val storyNameLiveData: LiveData<String> by lazy {
    Transformations.map(storyLiveData, ::processStoryTitle)
  }

  val storyChapterLiveData: LiveData<List<StoryItemViewModel>> by lazy {
    Transformations.map(storyLiveData, ::processStoryChapterList)
  }

  fun setInternalProfileId(internalProfileId: ProfileId) {
    this.profileId = internalProfileId
  }

  fun setClassroomId(classroomId: String) {
    this.classroomId = classroomId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  fun setStoryId(storyId: String) {
    this.storyId = storyId
  }

  private fun processStoryResult(
    ephemeralResult: AsyncResult<EphemeralStorySummary>
  ): EphemeralStorySummary {
    return when (ephemeralResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("StoryFragment", "Failed to retrieve Story: ", ephemeralResult.error)
        EphemeralStorySummary.getDefaultInstance()
      }
      is AsyncResult.Pending -> EphemeralStorySummary.getDefaultInstance()
      is AsyncResult.Success -> ephemeralResult.value
    }
  }

  private fun processStoryTitle(ephemeralStorySummary: EphemeralStorySummary): String {
    return translationController.extractString(
      ephemeralStorySummary.storySummary.storyTitle, ephemeralStorySummary.writtenTranslationContext
    )
  }

  private fun processStoryChapterList(
    ephemeralStorySummary: EphemeralStorySummary
  ): List<StoryItemViewModel> {
    val storySummary = ephemeralStorySummary.storySummary
    val chapterList = ephemeralStorySummary.chaptersList
    for (position in chapterList.indices) {
      if (storySummary.chapterList[position].chapterPlayState == ChapterPlayState.NOT_STARTED) {
        (fragment as StoryFragmentScroller).smoothScrollToPosition(position + 1)
        break
      }
    }

    val completedCount =
      chapterList.filter { ephemeralChapterSummary ->
        ephemeralChapterSummary.chapterSummary.chapterPlayState == ChapterPlayState.COMPLETED
      }.size

    // List with only the header
    val itemViewModelList: MutableList<StoryItemViewModel> = mutableListOf(
      StoryHeaderViewModel(completedCount, chapterList.size, resourceHandler) as StoryItemViewModel
    )

    // Add the rest of the list
    itemViewModelList.addAll(
      chapterList.mapIndexed { index, ephemeralChapterSummary ->
        StoryChapterSummaryViewModel(
          index,
          chapterList.size,
          fragment,
          explorationSelectionListener,
          explorationCheckpointController,
          profileId.loggedInInternalProfileId,
          classroomId,
          topicId,
          storyId,
          ephemeralChapterSummary,
          entityType,
          resourceHandler,
          translationController
        )
      }
    )

    return itemViewModelList
  }
}
