package org.oppia.android.domain.devoptions

import org.oppia.android.app.model.ChapterPlayState
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StorySummary
import org.oppia.android.app.model.Topic
import org.oppia.android.app.model.TopicProgress
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.domain.topic.TopicController
import org.oppia.android.domain.topic.TopicListController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.combineWith
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject
import javax.inject.Singleton

private const val GET_ALL_TOPICS_PROVIDER_ID = "get_all_topics_provider_id"
private const val GET_ALL_TOPICS_COMBINED_PROVIDER_ID = "get_all_topics_combined_provider_id"
private const val GET_ALL_STORIES_PROVIDER_ID = "get_all_stories_provider_id"

// TODO(#3423): Remove ModifyLessonProgressController from prod build of the app.
/** Controller to modify lesson progress such as marking chapters/stories/topics completed. */
@Singleton
class ModifyLessonProgressController @Inject constructor(
  private val topicController: TopicController,
  private val topicListController: TopicListController,
  private val storyProgressController: StoryProgressController,
  private val oppiaClock: OppiaClock
) {

  /**
   * Fetches a list of topics given a profile ID.
   *
   * @param profileId: the ID corresponding to the profile for which progress needs fetched.
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
   * Fetches a list of stories mapped to their corresponding topic ids given a profile ID.
   *
   * @param profileId: the ID corresponding to the profile for which progress needs fetched.
   * @return a [DataProvider] for [Map] of topic id mapped to list of [StorySummary] combined
   * with [StoryProgress].
   */
  fun getStoryMapWithProgress(
    profileId: ProfileId
  ): DataProvider<Map<String, List<StorySummary>>> {
    return getAllTopicsWithProgress(profileId)
      .transformAsync(GET_ALL_STORIES_PROVIDER_ID) { listOfTopics ->
        val storyMap = linkedMapOf<String, List<StorySummary>>()
        listOfTopics.forEach { topic ->
          storyMap[topic.topicId] = topic.storyList
        }
        AsyncResult.success(storyMap.toMap())
      }
  }

  /**
   * Checks if a topic is completed or not.
   *
   * @param topicWithProgress: the topic for which progress needs to be fetched.
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
   * @param storyWithProgress: the story for which progress needs to be fetched.
   * @return a [Boolean] indicating whether the story is completed or not.
   */
  fun checkIfStoryIsCompleted(storyWithProgress: StorySummary): Boolean {
    storyWithProgress.chapterList.forEach { chapterSummary ->
      if (chapterSummary.chapterPlayState != ChapterPlayState.COMPLETED) return false
    }
    return true
  }

  /**
   * Modifies lesson progress by marking multiple topics as completed for the current user profile.
   *
   * @param profileId: the ID corresponding to the profile for which progress needs modified.
   * @param topicIdList: the list of topic IDs for which progress needs modified.
   */
  fun markMultipleTopicsCompleted(profileId: ProfileId, topicIdList: List<String>) {
    topicIdList.forEach { topicId ->
      val topic = topicController.retrieveTopic(topicId)
      topic.storyList.forEach { storySummary ->
        storySummary.chapterList.forEach { chapterSummary ->
          storyProgressController.recordCompletedChapter(
            profileId = profileId,
            topicId = topic.topicId,
            storyId = storySummary.storyId,
            explorationId = chapterSummary.explorationId,
            completionTimestamp = oppiaClock.getCurrentTimeMs()
          )
        }
      }
    }
  }

  /**
   * Modifies lesson progress by marking multiple stories as completed for the current user profile.
   *
   * @param profileId: the ID corresponding to the profile for which progress needs modified.
   * @param storyMap: the list of topic IDs mapped to corresponding story IDs for which progress
   * needs modified.
   */
  fun markMultipleStoriesCompleted(profileId: ProfileId, storyMap: Map<String, String>) {
    storyMap.forEach {
      val storySummary = topicController.retrieveStory(topicId = it.value, storyId = it.key)
      storySummary.chapterList.forEach { chapterSummary ->
        storyProgressController.recordCompletedChapter(
          profileId = profileId,
          topicId = it.value,
          storyId = storySummary.storyId,
          explorationId = chapterSummary.explorationId,
          completionTimestamp = oppiaClock.getCurrentTimeMs()
        )
      }
    }
  }

  /**
   * Modifies lesson progress by marking multiple chapters as completed for the current user profile.
   *
   * @param profileId: the ID corresponding to the profile for which progress needs modified.
   * @param chapterMap: the list of [Pair] of topic IDs and story IDs mapped to corresponding
   * exploration IDs for which progress needs modified.
   */
  fun markMultipleChaptersCompleted(
    profileId: ProfileId,
    chapterMap: Map<String, Pair<String, String>>
  ) {
    chapterMap.forEach {
      storyProgressController.recordCompletedChapter(
        profileId = profileId,
        topicId = it.value.second,
        storyId = it.value.first,
        explorationId = it.key,
        completionTimestamp = oppiaClock.getCurrentTimeMs()
      )
    }
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
