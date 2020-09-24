package org.oppia.domain.topic

import kotlinx.coroutines.Deferred
import org.oppia.app.model.ChapterPlayState
import org.oppia.app.model.ChapterProgress
import org.oppia.app.model.ProfileId
import org.oppia.app.model.StoryProgress
import org.oppia.app.model.TopicProgress
import org.oppia.app.model.TopicProgressDatabase
import org.oppia.data.persistence.PersistentCacheStore
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProvider
import org.oppia.util.data.DataProviders
import org.oppia.util.data.DataProviders.Companion.transformAsync
import org.oppia.util.logging.ConsoleLogger
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
const val TEST_EXPLORATION_ID_5 = "13"
const val TEST_EXPLORATION_ID_6 = "ratio_input_exploration"
const val FRACTIONS_EXPLORATION_ID_0 = "umPkwp0L1M0-"
const val FRACTIONS_EXPLORATION_ID_1 = "MjZzEVOG47_1"
const val RATIOS_EXPLORATION_ID_0 = "2mzzFVDLuAj8"
const val RATIOS_EXPLORATION_ID_1 = "5NWuolNcwH6e"
const val RATIOS_EXPLORATION_ID_2 = "k2bQ7z5XHNbK"
const val RATIOS_EXPLORATION_ID_3 = "tIoSb3HZFN6e"

private const val CACHE_NAME = "topic_progress_database"

private const val TRANSFORMED_GET_STORY_PROGRESS_LIST_PROVIDER_ID =
  "transformed_get_story_progress_list_provider_id"
private const val TRANSFORMED_GET_TOPIC_PROGRESS_PROVIDER_ID =
  "transformed_get_topic_progress_provider_id"
private const val TRANSFORMED_GET_STORY_PROGRESS_PROVIDER_ID =
  "transformed_get_story_progress_provider_id"
private const val ADD_STORY_PROGRESS_TRANSFORMED_PROVIDER_ID = "add_story_progress_transformed_id"
private const val RECENTLY_PLAYED_CHAPTER_TRANSFORMED_PROVIDER_ID =
  "recently_played_chapter_transformed_id"

/**
 * Controller that records and provides completion statuses of chapters within the context of a
 * story.
 */
@Singleton
class StoryProgressController @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val logger: ConsoleLogger
) {
  // TODO(#21): Determine whether chapters can have missing prerequisites in the initial prototype,
  //  or if that just indicates that they can't be started due to previous chapter not yet being
  //  completed.

  /** These Statuses correspond to the exceptions above such that if the deferred contains. */
  private enum class StoryProgressActionStatus {
    SUCCESS
  }

  private val cacheStoreMap = mutableMapOf<ProfileId, PersistentCacheStore<TopicProgressDatabase>>()

  /**
   * Records the specified chapter completed within the context of the specified exploration, story,
   * topic. Returns a [DataProvider] that provides exactly one [AsyncResult] to indicate whether
   * this operation has succeeded. This method will never return a pending result.
   *
   * @param profileId the ID corresponding to the profile for which progress needs to be stored.
   * @param topicId the ID corresponding to the topic for which progress needs to be stored.
   * @param storyId the ID corresponding to the story for which progress needs to be stored.
   * @param explorationId the chapter id which will marked as [ChapterPlayState.COMPLETED]
   * @param completionTimestamp the timestamp at the exploration was finished.
   * @return a [DataProvider] that indicates the success/failure of this record progress operation.
   */
  fun recordCompletedChapter(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    completionTimestamp: Long
  ): DataProvider<Any?> {
    val deferred =
      retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
        updateInMemoryCache = true
      ) { topicProgressDatabase ->
        val chapterProgress = ChapterProgress.newBuilder()
          .setExplorationId(explorationId)
          .setChapterPlayState(ChapterPlayState.COMPLETED)
          .setLastPlayedTimestamp(completionTimestamp)
          .build()

        val storyProgressBuilder = StoryProgress.newBuilder()
          .setStoryId(storyId)
        if (topicProgressDatabase.topicProgressMap[topicId]?.storyProgressMap?.get(storyId)
          != null
        ) {
          storyProgressBuilder.putAllChapterProgress(
            topicProgressDatabase
              .topicProgressMap[topicId]!!.storyProgressMap[storyId]!!.chapterProgressMap
          )
        }
        storyProgressBuilder.putChapterProgress(explorationId, chapterProgress)
        val storyProgress = storyProgressBuilder.build()

        val topicProgressBuilder = TopicProgress.newBuilder().setTopicId(topicId)
        if (topicProgressDatabase.topicProgressMap[topicId] != null) {
          topicProgressBuilder
            .putAllStoryProgress(topicProgressDatabase.topicProgressMap[topicId]!!.storyProgressMap)
        }
        topicProgressBuilder.putStoryProgress(storyId, storyProgress)
        val topicProgress = topicProgressBuilder.build()

        val topicDatabaseBuilder =
          topicProgressDatabase.toBuilder().putTopicProgress(topicId, topicProgress)
        Pair(topicDatabaseBuilder.build(), StoryProgressActionStatus.SUCCESS)
      }

    return dataProviders.createInMemoryDataProviderAsync(
      ADD_STORY_PROGRESS_TRANSFORMED_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  /**
   * Records the recently played chapter for a specified exploration, story, topic. Returns a
   * [DataProvider] that provides exactly one [AsyncResult] to indicate whether this operation has
   * succeeded. This method will never return a pending result.
   *
   * @param profileId the ID corresponding to the profile for which progress needs to be stored.
   * @param topicId the ID corresponding to the topic for which progress needs to be stored.
   * @param storyId the ID corresponding to the story for which progress needs to be stored.
   * @param explorationId the chapter id which will marked as [ChapterPlayState.NOT_STARTED] if it
   *    has not been [ChapterPlayState.COMPLETED] already.
   * @param lastPlayedTimestamp the timestamp at which the exploration was last played.
   * @return a [DataProvider] that indicates the success/failure of this record progress operation.
   */
  fun recordRecentlyPlayedChapter(
    profileId: ProfileId,
    topicId: String,
    storyId: String,
    explorationId: String,
    lastPlayedTimestamp: Long
  ): DataProvider<Any?> {
    val deferred =
      retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
        updateInMemoryCache = true
      ) { topicProgressDatabase ->
        val previousChapterProgress =
          topicProgressDatabase
            .topicProgressMap[topicId]?.storyProgressMap?.get(storyId)?.chapterProgressMap?.get(
            explorationId
          )

        val chapterProgressBuilder = if (previousChapterProgress != null) {
          previousChapterProgress.toBuilder()
        } else {
          ChapterProgress.newBuilder()
            .setChapterPlayState(ChapterPlayState.STARTED_NOT_COMPLETED)
            .setExplorationId(explorationId)
        }
        if (previousChapterProgress != null) {
          chapterProgressBuilder.lastPlayedTimestamp =
            if (previousChapterProgress.lastPlayedTimestamp < lastPlayedTimestamp &&
              previousChapterProgress.chapterPlayState != ChapterPlayState.COMPLETED
            ) {
              lastPlayedTimestamp
            } else {
              previousChapterProgress.lastPlayedTimestamp
            }
        } else {
          chapterProgressBuilder.lastPlayedTimestamp = lastPlayedTimestamp
        }
        val storyProgressBuilder = StoryProgress.newBuilder().setStoryId(storyId)
        if (topicProgressDatabase.topicProgressMap[topicId]?.storyProgressMap?.get(storyId)
          != null
        ) {
          storyProgressBuilder.putAllChapterProgress(
            topicProgressDatabase
              .topicProgressMap[topicId]!!.storyProgressMap[storyId]!!.chapterProgressMap
          )
        }
        storyProgressBuilder.putChapterProgress(explorationId, chapterProgressBuilder.build())
        val storyProgress = storyProgressBuilder.build()

        val topicProgressBuilder = TopicProgress.newBuilder().setTopicId(topicId)
        if (topicProgressDatabase.topicProgressMap[topicId] != null) {
          topicProgressBuilder
            .putAllStoryProgress(topicProgressDatabase.topicProgressMap[topicId]!!.storyProgressMap)
        }
        topicProgressBuilder.putStoryProgress(storyId, storyProgress)
        val topicProgress = topicProgressBuilder.build()

        val topicDatabaseBuilder =
          topicProgressDatabase.toBuilder().putTopicProgress(topicId, topicProgress)
        Pair(topicDatabaseBuilder.build(), StoryProgressActionStatus.SUCCESS)
      }

    return dataProviders.createInMemoryDataProviderAsync(
      RECENTLY_PLAYED_CHAPTER_TRANSFORMED_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  /** Returns list of [TopicProgress] [DataProvider] for a particular profile. */
  internal fun retrieveTopicProgressListDataProvider(
    profileId: ProfileId
  ): DataProvider<List<TopicProgress>> {
    return retrieveCacheStore(profileId)
      .transformAsync(TRANSFORMED_GET_STORY_PROGRESS_LIST_PROVIDER_ID) { topicProgressDatabase ->
        val topicProgressList = mutableListOf<TopicProgress>()
        topicProgressList.addAll(topicProgressDatabase.topicProgressMap.values)
        AsyncResult.success(topicProgressList.toList())
      }
  }

  /** Returns a [TopicProgress] [DataProvider] for a specific topicId, per-profile basis. */
  internal fun retrieveTopicProgressDataProvider(
    profileId: ProfileId,
    topicId: String
  ): DataProvider<TopicProgress> {
    return retrieveCacheStore(profileId)
      .transformAsync(TRANSFORMED_GET_TOPIC_PROGRESS_PROVIDER_ID) {
        AsyncResult.success(it.topicProgressMap[topicId] ?: TopicProgress.getDefaultInstance())
      }
  }

  /** Returns a [StoryProgress] [DataProvider] for a specific storyId, per-profile basis. */
  internal fun retrieveStoryProgressDataProvider(
    profileId: ProfileId,
    topicId: String,
    storyId: String
  ): DataProvider<StoryProgress> {
    return retrieveTopicProgressDataProvider(profileId, topicId)
      .transformAsync(TRANSFORMED_GET_STORY_PROGRESS_PROVIDER_ID) {
        AsyncResult.success(it.storyProgressMap[storyId] ?: StoryProgress.getDefaultInstance())
      }
  }

  private suspend fun getDeferredResult(
    deferred: Deferred<StoryProgressActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      StoryProgressActionStatus.SUCCESS -> AsyncResult.success(null)
    }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<TopicProgressDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore =
        cacheStoreFactory.createPerProfile(
          CACHE_NAME,
          TopicProgressDatabase.getDefaultInstance(),
          profileId
        )
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
}
