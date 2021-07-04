package org.oppia.android.domain.topic

import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_ALL_TOPICS_PROVIDER_ID = "get_all_topics_provider_id"
private const val GET_ALL_TOPICS_COMBINED_PROVIDER_ID = "get_all_topics_combined_provider_id"
private const val GET_ALL_STORIES_PROVIDER_ID = "get_all_stories_provider_id"

/** Controller to modify lesson progress such as marking chapters/stories/topics completed. */
@Singleton
class ModifyLessonProgressController @Inject constructor(
  private val topicController: TopicController,
  private val topicListController: TopicListController,
  private val storyProgressController: StoryProgressController
) {

  /**
   * Fetches a list of topics given a profile ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @return a [DataProvider] for [List] of [Topic] combined with [TopicProgress].
   */
  fun getAllTopicsWithProgress(profileId: ProfileId): DataProvider<List<Topic>> {
    val allTopicsDataProvider = topicListController.getTopicList()
      .transformAsync(GET_ALL_TOPICS_PROVIDER_ID) { topicList ->
        val listOfTopics = mutableListOf<Topic>()
        topicList.topicSummaryList.forEach { topicSummary ->
          val topicId = topicSummary.topicId
          listOfTopics.add(topicController.retrieveTopic(topicId))
        }
        AsyncResult.success(listOfTopics.toList())
      }
    val topicProgressListDataProvider =
      storyProgressController.retrieveTopicProgressListDataProvider(profileId)
    return allTopicsDataProvider.combineWith(
      topicProgressListDataProvider,
      GET_ALL_TOPICS_COMBINED_PROVIDER_ID,
      ::combineTopicListAndTopicProgressList
    )
  }

  /**
   * Fetches a list of stories given a profile ID.
   *
   * @param profileId the ID corresponding to the profile for which progress needs fetched.
   * @return a [DataProvider] for [List] of [StorySummary] combined with [StoryProgress].
   */
  fun getAllStoriesWithProgress(profileId: ProfileId): DataProvider<List<StorySummary>> {
    return getAllTopicsWithProgress(profileId)
      .transformAsync(GET_ALL_STORIES_PROVIDER_ID) { listOfTopics ->
        val storyList = mutableListOf<StorySummary>()
        listOfTopics.forEach { topic ->
          storyList.addAll(topic.storyList)
        }
        AsyncResult.success(storyList.toList())
      }
  }

  /**
   * Checks if a topic is completed or not.
   *
   * @param topicWithProgress the topic for which progress needs to be fetched.
   * @return a [Boolean] indicating whether the topic is completed or not.
   */
  fun checkIfTopicIsCompleted(topicWithProgress: Topic): Boolean {
    topicWithProgress.storyList.forEach { storySummary ->
      storySummary.chapterList.forEach { chapterSummary ->
        if (chapterSummary.chapterPlayState != ChapterPlayState.COMPLETED) return false
      }
    }
    return true
  }

  /**
   * Checks if a story is completed or not.
   *
   * @param storyWithProgress the story for which progress needs to be fetched.
   * @return a [Boolean] indicating whether the story is completed or not.
   */
  fun checkIfStoryIsCompleted(storyWithProgress: StorySummary): Boolean {
    storyWithProgress.chapterList.forEach { chapterSummary ->
      if (chapterSummary.chapterPlayState != ChapterPlayState.COMPLETED) return false
    }
    return true
  }

  /** Combines list of topics without progress and list of [TopicProgress] into a list of [Topic]. */
  private fun combineTopicListAndTopicProgressList(
    allTopics: List<Topic>,
    topicProgressList: List<TopicProgress>
  ): List<Topic> {
    val topicProgressMap = topicProgressList.associateBy({ it.topicId }, { it })
    val allTopicsWithProgress = mutableListOf<Topic>()
    allTopics.forEach { topic ->
      allTopicsWithProgress.add(
        topicController.combineTopicAndTopicProgress(
          topic = topic,
          topicProgress = topicProgressMap[topic.topicId] ?: TopicProgress.getDefaultInstance()
        )
      )
    }
    return allTopicsWithProgress
  }
}
