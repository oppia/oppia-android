package org.oppia.domain.topic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Deferred
import org.json.JSONArray
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryProgress
import org.oppia.app.model.TopicProgress
import org.oppia.app.model.TopicProgressDatabase
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

private const val TRANSFORMED_GET_PROFILE_PROGRESS_PROVIDER_ID = "transformed_get_profile_progress_provider_id"
private const val TRANSFORMED_GET_TOPIC_PROGRESS_PROVIDER_ID = "transformed_get_topic_progress_provider_id"
private const val TRANSFORMED_GET_STORY_PROGRESS_PROVIDER_ID = "transformed_get_story_progress_provider_id"
private const val TRANSFORMED_GET_CHAPTER_PROGRESS_PROVIDER_ID = "transformed_get_chapter_progress_provider_id"
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

  /** Indicates that the given exploration id is not unique. */
  class ExplorationAlreadyCompletedException(msg: String) : java.lang.Exception(msg)

  /** Indicates that the given exploration id is not not found. */
  class ExplorationNotFoundException(msg: String) : java.lang.Exception(msg)

  /** Indicates that the given story id is not not found. */
  class StoryNotFoundException(msg: String) : java.lang.Exception(msg)

  /** Indicates that the given topic id is not not found. */
  class TopicNotFoundException(msg: String) : java.lang.Exception(msg)

  /** Indicates that the given story id does not have any associated story progress. */
  class StoryProgressNotFoundException(msg: String) : java.lang.Exception(msg)

  /** Indicates that the given topic id does not have any associated topic progress. */
  class TopicProgressNotFoundException(msg: String) : java.lang.Exception(msg)

  /** Indicates that the given profile id does not have any associated progress. */
  class ProfileProgressNotFoundException(msg: String) : java.lang.Exception(msg)

  /** These Statuses correspond to the exceptions above such that if the deferred contains. */
  private enum class StoryProgressActionStatus {
    SUCCESS,
    EXPLORATION_ALREADY_COMPLETED,
    EXPLORATION_NOT_FOUND,
    STORY_NOT_FOUND,
    TOPIC_NOT_FOUND,
    STORY_PROGRESS_NOT_FOUND,
    TOPIC_PROGRESS_NOT_FOUND,
    PROFILE_PROGRESS_NOT_FOUND
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
   */
  fun recordCompletedChapter(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String
  ): LiveData<AsyncResult<Any?>> {
    val deferred = retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(updateInMemoryCache = true) {
      val chapterPlayState = ChapterPlayState.COMPLETED

      val storyProgress = if (it.topicProgressMap[topicId]?.storyProgressMap?.get(storyId) != null) {
        val storyProgressBuilder = StoryProgress.newBuilder()
          .putAllChapterProgress(it.topicProgressMap[topicId]!!.storyProgressMap[storyId]!!.chapterProgressMap)
          .putChapterProgress(explorationId, chapterPlayState)
        storyProgressBuilder.build()
      } else {
        val storyProgressBuilder = StoryProgress.newBuilder().putChapterProgress(
          explorationId,
          chapterPlayState
        )
        storyProgressBuilder.build()
      }

      val topicProgress = if (it.topicProgressMap[topicId] != null) {
        val topicProgressBuilder =
          TopicProgress.newBuilder().putAllStoryProgress(it.topicProgressMap[topicId]!!.storyProgressMap)
            .putStoryProgress(storyId, storyProgress)
        topicProgressBuilder.build()
      } else {
        val topicProgressBuilder = TopicProgress.newBuilder().putStoryProgress(storyId, storyProgress)
        topicProgressBuilder.build()
      }

      val topicDatabaseBuilder = it.toBuilder().putTopicProgress(topicId, topicProgress)
      Pair(topicDatabaseBuilder.build(), StoryProgressActionStatus.SUCCESS)
    }

    return dataProviders.convertToLiveData(
      dataProviders.createInMemoryDataProviderAsync(ADD_STORY_PROGRESS_TRANSFORMED_PROVIDER_ID) {
        return@createInMemoryDataProviderAsync getDeferredResult(
          profileId,
          explorationId,
          storyId,
          topicId,
          deferred
        )
      })
  }

  /** Returns entire progress of user. */
  fun getProfileProgress(profileId: ProfileId): LiveData<AsyncResult<TopicProgressDatabase>> {
    return dataProviders.convertToLiveData(retrieveProfileProgressDataProvider(profileId))
  }

  /** Returns topic progress on per-profile basis specified by topicId. */
  fun getTopicProgress(profileId: ProfileId, topicId: String): LiveData<AsyncResult<TopicProgress>> {
    return dataProviders.convertToLiveData(retrieveTopicProgressDataProvider(profileId, topicId))
  }

  /** Returns story progress on per-profile basis specified by topicId and storyId. */
  fun getStoryProgress(
    profileId: ProfileId,
    topicId: String,
    storyId: String
  ): LiveData<AsyncResult<StoryProgress>> {
    return dataProviders.convertToLiveData(retrieveStoryProgressDataProvider(profileId, topicId, storyId))
  }

  /** Returns chapter progress on per-profile basis specified by topicId, storyId and explorationId. */
  fun getChapterProgress(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    exploration: String
  ): LiveData<AsyncResult<ChapterPlayState>> {
    return dataProviders.convertToLiveData(
      retrieveChapterProgressDataProvider(
        profileId,
        topicId,
        storyId,
        exploration
      )
    )
  }

  /** Returns all topics progress [DataProvider] for a particular profile. */
  internal fun retrieveProfileProgressDataProvider(profileId: ProfileId): DataProvider<TopicProgressDatabase> {
    return dataProviders.transformAsync(
      TRANSFORMED_GET_PROFILE_PROGRESS_PROVIDER_ID,
      retrieveCacheStore(profileId)
    ) {
      if (it.topicProgressMap.keys.isNotEmpty()) {
        AsyncResult.success(it)
      } else {
        AsyncResult.success(TopicProgressDatabase.getDefaultInstance())
      }
    }
  }

  /** Returns a [TopicProgress] [DataProvider] for a specific topicId, per-profile basis. */
  internal fun retrieveTopicProgressDataProvider(profileId: ProfileId, topicId: String): DataProvider<TopicProgress> {
    return dataProviders.transformAsync(
      TRANSFORMED_GET_TOPIC_PROGRESS_PROVIDER_ID,
      retrieveCacheStore(profileId)
    ) {
      val topicProgress = it.topicProgressMap[topicId]
      if (topicProgress != null) {
        AsyncResult.success(topicProgress)
      } else {
        AsyncResult.success(TopicProgress.getDefaultInstance())
      }
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
      val storyProgress = it.storyProgressMap[storyId]
      if (storyProgress != null) {
        AsyncResult.success(storyProgress)
      } else {
        AsyncResult.success(StoryProgress.getDefaultInstance())
      }
    }
  }

  /** Returns a [ChapterPlayState] [DataProvider] for a specific explorationId, per-profile basis. */
  internal fun retrieveChapterProgressDataProvider(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String
  ): DataProvider<ChapterPlayState> {
    return dataProviders.transformAsync(
      TRANSFORMED_GET_CHAPTER_PROGRESS_PROVIDER_ID,
      retrieveStoryProgressDataProvider(profileId, topicId, storyId)
    ) {
      val chapterPlayState = it.chapterProgressMap[explorationId]
      if (chapterPlayState != null) {
        AsyncResult.success(chapterPlayState)
      } else {
        AsyncResult.success(ChapterPlayState.NOT_PLAYABLE_MISSING_PREREQUISITES)
      }
    }
  }

  private suspend fun getDeferredResult(
    profileId: ProfileId?,
    explorationId: String?,
    storyId: String?,
    topicId: String?,
    deferred: Deferred<StoryProgressActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      StoryProgressActionStatus.SUCCESS -> AsyncResult.success(null)
      StoryProgressActionStatus.EXPLORATION_ALREADY_COMPLETED -> AsyncResult.failed(
        ExplorationAlreadyCompletedException("$explorationId is already present.")
      )
      StoryProgressActionStatus.EXPLORATION_NOT_FOUND -> AsyncResult.failed(
        ExplorationNotFoundException("ExplorationId $explorationId does not match an existing Exploration")
      )
      StoryProgressActionStatus.STORY_NOT_FOUND -> AsyncResult.failed(
        StoryNotFoundException("StoryId $storyId does not match an existing story")
      )
      StoryProgressActionStatus.TOPIC_NOT_FOUND -> AsyncResult.failed(
        TopicNotFoundException("TopicId $topicId does not match an existing topic")
      )
      StoryProgressActionStatus.STORY_PROGRESS_NOT_FOUND -> AsyncResult.failed(
        StoryProgressNotFoundException("StoryProgress not found for profile $profileId")
      )
      StoryProgressActionStatus.TOPIC_PROGRESS_NOT_FOUND -> AsyncResult.failed(
        TopicProgressNotFoundException("TopicProgress not found for profile $profileId")
      )
      StoryProgressActionStatus.PROFILE_PROGRESS_NOT_FOUND -> AsyncResult.failed(
        ProfileProgressNotFoundException("Profile progress not found for profile $profileId")
      )
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

  internal fun retrieveCacheStore(profileId: ProfileId): PersistentCacheStore<TopicProgressDatabase> {
    //return profileDataStore
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
