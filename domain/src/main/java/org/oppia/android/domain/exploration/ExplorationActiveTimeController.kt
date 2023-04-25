package org.oppia.android.domain.exploration

import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.TopicLearningTime
import org.oppia.android.app.model.TopicLearningTimeDatabase
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transform
import org.oppia.android.util.system.OppiaClock
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val CACHE_NAME = "topic_learning_time_database"
private const val RECORD_AGGREGATE_LEARNING_TIME_PROVIDER_ID =
  "record_aggregate_learning_time_provider_id"
private const val RETRIEVE_AGGREGATE_LEARNING_TIME_PROVIDER_ID =
  "retrieve_aggregate_learning_time_provider_id"
private val LEARNING_TIME_EXPIRATION_MILLIS = TimeUnit.DAYS.toMillis(10)

/** Controller for tracking the amount of active time a user has spent in a topic. */
class ExplorationActiveTimeController @Inject constructor(
  private val oppiaClock: OppiaClock,
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val oppiaLogger: OppiaLogger
) {

  /**
   * Statuses correspond to the exceptions such that if the deferred contains an error state,
   * a corresponding exception will be passed to a failed AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  private enum class TopicLearningTimeActionStatus {
    SUCCESS
  }

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<TopicLearningTimeDatabase>>()

  private var startExplorationTimestampMs: Long = 0L

  /**
   * Begin tracking the active learning time in an exploration.
   *
   * We define the active time loosely as the time spent in an exploration when the app is in the
   * foreground.
   *
   * This method is called when the [ExplorationProgressController.beginExplorationImpl] finishes
   * executing successfully, or when the app comes back to the foreground after being previously
   * backgrounded.
   */
  fun setExplorationSessionStarted() {
    this.startExplorationTimestampMs = oppiaClock.getCurrentTimeMs()
  }

  /**
   * Stops tracking the active learning time in an exploration.
   *
   * This method is called when the [ExplorationProgressController.finishExplorationImpl] finishes
   * executing, or the app goes to the background.
   */
  fun setExplorationSessionPaused(profileId: ProfileId, topicId: String) {
    recordAggregateTopicLearningTime(
      profileId = profileId,
      topicId = topicId,
      sessionDuration = oppiaClock.getCurrentTimeMs() - startExplorationTimestampMs
    )
  }

  /**
   * Records the tracked active exploration time.
   *
   * @param profileId the ID corresponding to the profile for which progress needs to be stored
   * @param topicId the ID corresponding to the topic for which duration needs to be stored
   * @param sessionDuration the tracked exploration duration between start and pause
   * @return a [DataProvider] that indicates the success/failure of this record operation
   */
  private fun recordAggregateTopicLearningTime(
    profileId: ProfileId,
    topicId: String,
    sessionDuration: Long
  ): DataProvider<Any?> {
    val deferred = retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { topicLearningTimeDatabase ->

      val previousAggregateLearningTime =
        topicLearningTimeDatabase.aggregateTopicLearningTimeMap[topicId]

      val topicLearningTimeBuilder = if (previousAggregateLearningTime != null) {
        previousAggregateLearningTime.toBuilder()
      } else {
        TopicLearningTime.newBuilder()
          .setTopicId(topicId)
          .setLastUpdatedTimeMs(oppiaClock.getCurrentTimeMs())
      }

      if (isLastUpdatedTimestampStale(topicLearningTimeBuilder.lastUpdatedTimeMs)
      ) {
        topicLearningTimeBuilder.topicLearningTimeMs = sessionDuration
      } else {
        topicLearningTimeBuilder.topicLearningTimeMs += sessionDuration
      }
      topicLearningTimeBuilder.lastUpdatedTimeMs = oppiaClock.getCurrentTimeMs()

      val topicLearningTime = topicLearningTimeBuilder.build()

      val topicLearningTimeDatabaseBuilder = topicLearningTimeDatabase.toBuilder()
        .putAggregateTopicLearningTime(topicId, topicLearningTime)
      Pair(topicLearningTimeDatabaseBuilder.build(), TopicLearningTimeActionStatus.SUCCESS)
    }
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_AGGREGATE_LEARNING_TIME_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(deferred)
    }
  }

  private fun isLastUpdatedTimestampStale(lastUpdatedTimestamp: Long): Boolean {
    val currentTime = oppiaClock.getCurrentTimeMs()
    val differenceInMillis = currentTime - lastUpdatedTimestamp
    return differenceInMillis > LEARNING_TIME_EXPIRATION_MILLIS
  }

  /** Returns the [TopicLearningTime] [DataProvider] for a specific topicId, per-profile basis. */
  fun retrieveAggregateTopicLearningTimeDataProvider(
    profileId: ProfileId,
    topicId: String
  ): DataProvider<TopicLearningTime> {
    return retrieveCacheStore(profileId)
      .transform(
        RETRIEVE_AGGREGATE_LEARNING_TIME_PROVIDER_ID
      ) { learningTimeDb ->
        learningTimeDb.aggregateTopicLearningTimeMap[topicId]
          ?: TopicLearningTime.getDefaultInstance()
      }
  }

  private suspend fun getDeferredResult(
    deferred: Deferred<TopicLearningTimeActionStatus>
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      TopicLearningTimeActionStatus.SUCCESS -> AsyncResult.Success(null)
    }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<TopicLearningTimeDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore =
        cacheStoreFactory.createPerProfile(
          CACHE_NAME,
          TopicLearningTimeDatabase.getDefaultInstance(),
          profileId
        )
      cacheStoreMap[profileId] = cacheStore
      cacheStore
    }

    cacheStore.primeInMemoryAndDiskCacheAsync(
      updateMode = PersistentCacheStore.UpdateMode.UPDATE_IF_NEW_CACHE,
      publishMode = PersistentCacheStore.PublishMode.PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion {
      if (it != null) {
        oppiaLogger.e(
          "ExplorationActiveTimeController",
          "Failed to prime cache ahead of data retrieval.",
          it
        )
      }
    }
    return cacheStore
  }
}
