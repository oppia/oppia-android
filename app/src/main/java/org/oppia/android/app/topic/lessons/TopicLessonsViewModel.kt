package org.oppia.android.app.topic.lessons

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.home.RouteToExplorationListener
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.app.topic.RouteToStoryListener
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeFooterViewModel
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeHeaderViewModel
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeItemViewModel
import org.oppia.android.app.topic.practice.practiceitemviewmodel.TopicPracticeSubtopicViewModel
import org.oppia.android.app.viewmodel.ObservableViewModel
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

@FragmentScope
class TopicLessonsViewModel @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val logger: ConsoleLogger,
  private val explorationDataController: ExplorationDataController,
  private val topicController: TopicController
): ObservableViewModel(), StorySummarySelector, ChapterSummarySelector {
  private val routeToExplorationListener = activity as RouteToExplorationListener
  private val routeToStoryListener = activity as RouteToStoryListener
  private val itemViewModelList: MutableList<TopicLessonsItemViewModel> = ArrayList()
  private lateinit var topicId: String
  private var internalProfileId: Int = -1

  private val topicResultLiveData: LiveData<AsyncResult<Topic>> by lazy {
    topicController.getTopic(
      ProfileId.newBuilder().setInternalId(internalProfileId).build(),
      topicId
    ).toLiveData()
  }

  private val topicLiveData: LiveData<Topic> by lazy { getTopicList() }

  private fun getTopicList(): LiveData<Topic> {
    return Transformations.map(topicResultLiveData, ::processTopicResult)
  }

  val topicLessonsLiveData: LiveData<List<TopicLessonsItemViewModel>> by lazy {
    Transformations.map(topicLiveData, ::processTopicLessonsList)
  }

  private fun processTopicResult(topic: AsyncResult<Topic>): Topic {
    if (topic.isFailure()) {
      logger.e(
        "TopicLessonsFragment",
        "Failed to retrieve topic",
        topic.getErrorOrNull()!!
      )
    }
    return topic.getOrDefault(Topic.getDefaultInstance())
  }


  fun setInternalProfileId(internalProfileId: Int) {
    this.internalProfileId = internalProfileId
  }

  fun setTopicId(topicId: String) {
    this.topicId = topicId
  }

  private fun processTopicLessonsList(topic: Topic): List<TopicLessonsItemViewModel> {
    itemViewModelList.clear()
    itemViewModelList.add(TopicLessonsTitleViewModel() as TopicLessonsTitleViewModel)

//
//    for (storySummary in topic.storyList) {
//      itemViewModelList.add(
//        StorySummaryViewModel(
//          storySummary,
//          fragment as StorySummarySelector,
//          this as ChapterSummarySelector
//        )
//      )
//    }
    itemViewModelList.addAll(
      topic.storyList.map { storySummary ->
        StorySummaryViewModel(
          storySummary,
          fragment as StorySummarySelector,
          this as ChapterSummarySelector
        ) as StorySummaryViewModel
      }
    )

    return itemViewModelList
  }

  override fun selectStorySummary(storySummary: StorySummary) {
    routeToStoryListener.routeToStory(internalProfileId, topicId, storySummary.storyId)
  }

  override fun selectChapterSummary(storyId: String, explorationId: String) {
    playExploration(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      /* backflowScreen= */ 0
    )
  }

  private fun playExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    backflowScreen: Int?
  ) {
    explorationDataController.startPlayingExploration(
      explorationId
    ).observe(
      fragment,
      Observer<AsyncResult<Any?>> { result ->
        when {
          result.isPending() -> logger.d("TopicLessonsFragment", "Loading exploration")
          result.isFailure() -> logger.e(
            "TopicLessonsFragment",
            "Failed to load exploration",
            result.getErrorOrNull()!!
          )
          else -> {
            logger.d("TopicLessonsFragment", "Successfully loaded exploration")
            routeToExplorationListener.routeToExploration(
              internalProfileId,
              topicId,
              storyId,
              explorationId,
              backflowScreen
            )
          }
        }
      }
    )
  }

}