package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Deferred
import org.json.JSONArray
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryProgress
import org.oppia.app.model.StoryProgressList
import org.oppia.app.model.TopicProgress
import org.oppia.app.model.TopicProgressDatabase
import org.oppia.app.model.TopicProgressList
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.domain.util.JsonAssetRetriever
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.logging.Logger
import javax.inject.Inject
import javax.inject.Singleton

const val TEST_STORY_ID_0 = "test_story_id_0"
const val TEST_STORY_ID_1 = "test_story_id_1"
const val TEST_STORY_ID_2 = "test_story_id_2"
const val FRACTIONS_STORY_ID_0 = "wANbh4oOClga"
const val RATIOS_STORY_ID_0 = "wAMdg4oOClga"
const val RATIOS_STORY_ID_1 = "xBSdg4oOClga"
const val TEST_EXPLORATION_ID_0 = "test_exp_id_0"
const val TEST_EXPLORATION_ID_1 = "test_exp_id_1"
const val TEST_EXPLORATION_ID_2 = "test_exp_id_2"
const val TEST_EXPLORATION_ID_3 = "test_exp_id_3"
const val TEST_EXPLORATION_ID_4 = "test_exp_id_4"
const val FRACTIONS_EXPLORATION_ID_0 = "umPkwp0L1M0-"
const val FRACTIONS_EXPLORATION_ID_1 = "MjZzEVOG47_1"
const val RATIOS_EXPLORATION_ID_0 = "2mzzFVDLuAj8"
const val RATIOS_EXPLORATION_ID_1 = "5NWuolNcwH6e"
const val RATIOS_EXPLORATION_ID_2 = "k2bQ7z5XHNbK"
const val RATIOS_EXPLORATION_ID_3 = "tIoSb3HZFN6e"

private const val CACHE_NAME = "topic_progress_database"

private const val TRANSFORMED_GET_STORY_PROGRESS_LIST_PROVIDER_ID = "transformed_get_story_progress_list_provider_id"
private const val TRANSFORMED_GET_TOPIC_PROGRESS_PROVIDER_ID = "transformed_get_topic_progress_provider_id"
private const val TRANSFORMED_GET_STORY_PROGRESS_PROVIDER_ID = "transformed_get_story_progress_provider_id"
private const val ADD_STORY_PROGRESS_TRANSFORMED_PROVIDER_ID = "add_story_progress_transformed_id"

/** Controller that records and provides completion statuses of chapters within the context of a story. */
@Singleton
class StoryProgressController @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val jsonAssetRetriever: JsonAssetRetriever,
  private val logger: Logger
) {
  // TODO(#21): Determine whether chapters can have missing prerequisites in the initial prototype, or if that just
  //  indicates that they can't be started due to previous chapter not yet being completed.

  /** These Statuses correspond to the exceptions above such that if the deferred contains. */
  private enum class StoryProgressActionStatus {
    SUCCESS
  }

  private val trackedStoriesProgress: Map<String, TrackedStoryProgress> by lazy { createInitialStoryProgressState() }

  private val cacheStoreMap = mutableMapOf<ProfileId, PersistentCacheStore<TopicProgressDatabase>>()

  fun retrieveStoryProgress(storyId: String): StoryProgress {
    return createStoryProgressSnapshot(storyId)
  }

  /**
   * Records the specified chapter completed within the context of the specified exploration, story, topic. Returns a [LiveData] that
   * provides exactly one [AsyncResult] to indicate whether this operation has succeeded. This method will never return
   * a pending result.
   *
   * @param profileId the ID corresponding to the profile for which progress needs to be stored.
   * @param topicId the ID corresponding to the topic for which progress needs to be stored.
   * @param storyId the ID corresponding to the story for which progress needs to be stored.
   * @param explorationId the chapter id which will marked as [ChapterPlayState.COMPLETED]
   * @return a [LiveData] that indicates the success/failure of this record progress operation.
   */
  fun recordCompletedChapter(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String
  ): LiveData<AsyncResult<Any?>> {
    val deferred =
      retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(updateInMemoryCache = true) { topicProgressDatabase ->
        val chapterPlayState = ChapterPlayState.COMPLETED

        val storyProgressBuilder = StoryProgress.newBuilder().setStoryId(storyId)
        if (topicProgressDatabase.topicProgressMap[topicId]?.storyProgressMap?.get(storyId) != null) {
          storyProgressBuilder.putAllChapterProgress(topicProgressDatabase.topicProgressMap[topicId]!!.storyProgressMap[storyId]!!.chapterProgressMap)
        }
        storyProgressBuilder.putChapterProgress(explorationId, chapterPlayState)
        val storyProgress = storyProgressBuilder.build()

        val topicProgressBuilder = TopicProgress.newBuilder().setTopicId(topicId)
        if (topicProgressDatabase.topicProgressMap[topicId] != null) {
          topicProgressBuilder
            .putAllStoryProgress(topicProgressDatabase.topicProgressMap[topicId]!!.storyProgressMap)
        }
        topicProgressBuilder.putStoryProgress(storyId, storyProgress)
        val topicProgress = topicProgressBuilder.build()

        val topicDatabaseBuilder = topicProgressDatabase.toBuilder().putTopicProgress(topicId, topicProgress)
        Pair(topicDatabaseBuilder.build(), StoryProgressActionStatus.SUCCESS)
      }

    return dataProviders.convertToLiveData(
      dataProviders.createInMemoryDataProviderAsync(ADD_STORY_PROGRESS_TRANSFORMED_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync getDeferredResult(profileId, deferred)
      })
  }

  /** Returns [TopicProgressList] [DataProvider] for a particular profile. */
  internal fun retrieveTopicProgressListDataProvider(profileId: ProfileId): DataProvider<TopicProgressList> {
    return dataProviders.transformAsync(
      TRANSFORMED_GET_STORY_PROGRESS_LIST_PROVIDER_ID,
      retrieveCacheStore(profileId)
    ) { topicProgressDatabase ->
      val topicProgressListBuilder = TopicProgressList.newBuilder()
      topicProgressListBuilder.addAllTopicProgress(topicProgressDatabase.topicProgressMap.values)
      AsyncResult.success(topicProgressListBuilder.build())
    }
  }

  /** Returns [StoryProgressList] [DataProvider] for a particular profile. */
  internal fun retrieveStoryProgressListDataProvider(profileId: ProfileId): DataProvider<StoryProgressList> {
    return dataProviders.transformAsync(
      TRANSFORMED_GET_STORY_PROGRESS_LIST_PROVIDER_ID,
      retrieveCacheStore(profileId)
    ) { topicProgressDatabase ->
      val storyProgressListBuilder = StoryProgressList.newBuilder()
      val transformedStoryProgressList =
        topicProgressDatabase.topicProgressMap.values.map { topicProgress -> topicProgress!!.storyProgressMap!!.values.toList() }
      storyProgressListBuilder.addAllStoryProgress(transformedStoryProgressList.flatten())
      AsyncResult.success(storyProgressListBuilder.build())
    }
  }

  /** Returns a [TopicProgress] [DataProvider] for a specific topicId, per-profile basis. */
  internal fun retrieveTopicProgressDataProvider(profileId: ProfileId, topicId: String): DataProvider<TopicProgress> {
    return dataProviders.transformAsync(
      TRANSFORMED_GET_TOPIC_PROGRESS_PROVIDER_ID,
      retrieveCacheStore(profileId)
    ) {
      AsyncResult.success(it.topicProgressMap[topicId] ?: TopicProgress.getDefaultInstance())
    }
  }

  /** Returns a [StoryProgress] [DataProvider] for a specific storyId, per-profile basis. */
  internal fun retrieveStoryProgressDataProvider(
    profileId: ProfileId,
    topicId: String,
    storyId: String
  ): DataProvider<StoryProgress> {
    return dataProviders.transformAsync(
      TRANSFORMED_GET_STORY_PROGRESS_PROVIDER_ID,
      retrieveTopicProgressDataProvider(profileId, topicId)
    ) {
      AsyncResult.success(it.storyProgressMap[storyId] ?: StoryProgress.getDefaultInstance())
    }
  }

  private suspend fun getDeferredResult(
    profileId: ProfileId?,
    deferred: Deferred<StoryProgressActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      StoryProgressActionStatus.SUCCESS -> AsyncResult.success(null)
    }
  }

  // TODO(#21): Implement notifying story progress changes when a chapter is recorded as complete, and add tests for
  //  this case.

  /**
   * Returns a [LiveData] corresponding to the story progress of the specified story, or a failure if no such story can
   * be identified. This [LiveData] will update as the story's progress changes.
   */
  fun getStoryProgress(storyId: String): LiveData<AsyncResult<StoryProgress>> {
    return try {
      MutableLiveData(AsyncResult.success(createStoryProgressSnapshot(storyId)))
    } catch (e: Exception) {
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  private fun createStoryProgressSnapshot(storyId: String): StoryProgress {
    check(storyId in trackedStoriesProgress) { "No story found with ID: $storyId" }
    return trackedStoriesProgress.getValue(storyId).toStoryProgress()
  }

  private fun createInitialStoryProgressState(): Map<String, TrackedStoryProgress> {
    return mapOf(
      TEST_STORY_ID_0 to createStoryProgress0(),
      TEST_STORY_ID_1 to createStoryProgress1(),
      TEST_STORY_ID_2 to createStoryProgress2(),
      FRACTIONS_STORY_ID_0 to createStoryProgressForJsonStory("fractions_stories.json", /* index= */ 0),
      RATIOS_STORY_ID_0 to createStoryProgressForJsonStory("ratios_stories.json", /* index= */ 0),
      RATIOS_STORY_ID_1 to createStoryProgressForJsonStory("ratios_stories.json", /* index= */ 1)
    )
  }

  private fun createStoryProgressForJsonStory(fileName: String, index: Int): TrackedStoryProgress {
    val storyData = jsonAssetRetriever.loadJsonFromAsset(fileName)?.getJSONArray("story_list")!!
    val explorationIdList = getExplorationIdsFromStory(
      storyData.getJSONObject(index).getJSONObject("story")
        .getJSONObject("story_contents").getJSONArray("nodes")
    )
    return TrackedStoryProgress(
      chapterList = explorationIdList,
      completedChapters = setOf()
    )
  }

  private fun getExplorationIdsFromStory(chapterData: JSONArray): List<String> {
    val explorationIdList = mutableListOf<String>()
    for (i in 0 until chapterData.length()) {
      val chapter = chapterData.getJSONObject(i)
      explorationIdList.add(chapter.getString("exploration_id"))
    }
    return explorationIdList
  }

  private fun createStoryProgress0(): TrackedStoryProgress {
    return TrackedStoryProgress(
      chapterList = listOf(TEST_EXPLORATION_ID_0),
      completedChapters = setOf()
    )
  }

  private fun createStoryProgress1(): TrackedStoryProgress {
    return TrackedStoryProgress(
      chapterList = listOf(TEST_EXPLORATION_ID_1, TEST_EXPLORATION_ID_2, TEST_EXPLORATION_ID_3),
      completedChapters = setOf()
    )
  }

  private fun createStoryProgress2(): TrackedStoryProgress {
    return TrackedStoryProgress(
      chapterList = listOf(TEST_EXPLORATION_ID_4),
      completedChapters = setOf()
    )
  }

  private fun retrieveCacheStore(profileId: ProfileId): PersistentCacheStore<TopicProgressDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore =
        cacheStoreFactory.createPerProfile(CACHE_NAME, TopicProgressDatabase.getDefaultInstance(), profileId)
      cacheStoreMap[profileId] = cacheStore
      cacheStore
    }

    cacheStore.primeCacheAsync().invokeOnCompletion {
      it?.let { it ->
        logger.e(
          "StoryProgressController",
          "Failed to prime cache ahead of LiveData conversion for StoryProgressController.",
          it
        )
      }
    }

    return cacheStore
  }

  /**
   * Mutable container for [StoryProgress] that provides support for determining whether a specific chapter can be
   * played in the context of this story, marking a chapter as played, and converting to a [StoryProgress] object for
   * reporting to the UI.
   */
  private class TrackedStoryProgress(private val chapterList: List<String>, completedChapters: Set<String>) {
    private val trackedCompletedChapters: MutableSet<String> = completedChapters.toMutableSet()

    // TODO(#21): Implement tests for the following invariant checking logic, if possible.
    init {
      // Verify that the progress object is well-defined by ensuring that the invariant where lessons must be played in
      // order holds.
      var expectedCompleted: Boolean? = null
      chapterList.reversed().forEach { explorationId ->
        val completedChapter = explorationId in trackedCompletedChapters
        val expectedCompletedSnapshot = expectedCompleted
        if (expectedCompletedSnapshot == null) {
          // This should always be initialized for the last lesson. If  it's completed, all previous lessons must be
          // completed. If it's not, then previous lessons may be completed or incomplete.
          expectedCompleted = completedChapter
        } else if (completedChapter != expectedCompletedSnapshot) {
          // There's exactly one case where the expectation can change: if the next lesson is not completed. This means
          // the current lesson is the most recent one completed in the list, and all previous lessons must also be
          // completed.
          check(!expectedCompletedSnapshot) {
            "Expected lessons to be completed in order with no holes between them, and starting from the beginning " +
                "of the story. Encountered uncompleted chapter right before a completed chapter: $explorationId"
          }
          // This is the first lesson that was completed after encountering one or more lessons that are not completed.
          // All previous lessons in the list (the lessons next to be iterated) must be completed in order for the
          // in-order invariant to hold.
          expectedCompleted = true
        }
        // Otherwise, the invariant holds. Continue on to the previous lesson.
      }
    }

    /**
     * Returns whether the specified exploration ID can be played, or if it's missing prerequisites. Fails if the
     * specified exploration ID is not contained in this story.
     */
    fun canPlayChapter(explorationId: String): Boolean {
      // The chapter can be played only if it's the first one, or the chapter before it has been completed.
      check(explorationId in chapterList) { "Chapter not found in story: $explorationId" }
      val chapterIndex = chapterList.indexOf(explorationId)
      return if (chapterIndex == 0) true else chapterList[chapterIndex - 1] in trackedCompletedChapters
    }

    /** Returns an immutable [StoryProgress] representation of this progress object. */
    fun toStoryProgress(): StoryProgress {
      val storyProgressBuilder = StoryProgress.newBuilder()
      for (explorationId in chapterList) {
        storyProgressBuilder.putChapterProgress(explorationId, buildChapterProgress(explorationId))
      }
      return storyProgressBuilder.build()
    }

    private fun buildChapterProgress(explorationId: String): ChapterPlayState {
      return when {
        explorationId in trackedCompletedChapters -> ChapterPlayState.COMPLETED
        canPlayChapter(explorationId) -> ChapterPlayState.NOT_STARTED
        else -> ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES /* Assume only reason is missing prerequisites. */
      }
    }
  }
}
