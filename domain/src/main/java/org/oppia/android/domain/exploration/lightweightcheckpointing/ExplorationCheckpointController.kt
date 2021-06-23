package org.oppia.android.domain.exploration.lightweightcheckpointing

import android.util.Log
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ExplorationCheckpointDatabase
import org.oppia.android.app.model.ExplorationCheckpointDetails
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.data.DataProviders.Companion.transformAsync
import javax.inject.Inject
import javax.inject.Singleton

private const val CACHE_NAME = "exploration_checkpoint_database"
private const val RETRIEVE_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID =
  "retrieve_exploration_checkpoint_provider_id"
private const val RETRIEVE_OLDEST_CHECKPOINT_DETAILS_DATA_PROVIDER_ID =
  "retrieve_oldest_checkpoint_details_provider_id"
private const val RECORD_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID =
  "record_exploration_checkpoint_provider_id"
private const val DELETE_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID =
  "delete_exploration_checkpoint_provider_id"

/**
 * Controller for saving, retrieving, updating, and deleting exploration checkpoints.
 */
@Singleton
class ExplorationCheckpointController @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val oppiaLogger: OppiaLogger,
  @ExplorationStorageDatabaseSize private val explorationCheckpointDatabaseSizeLimit: Int
) {

  /** Different stages in which the exploration checkpoint database can exist. */
  enum class ExplorationCheckpointDatabaseState {

    /** checkpoint database has not exceeded the allocated size limit. */
    CHECKPOINT_DATABASE_SIZE_LIMIT_NOT_EXCEEDED,

    /** checkpoint database has exceeded the allocated size limit. */
    CHECKPOINT_DATABASE_SIZE_LIMIT_EXCEEDED
  }

  /** Indicates that no checkpoint was found for the specified explorationId and profileId. */
  class ExplorationCheckpointNotFoundException(message: String) : Exception(message)

  /**
   * These Statuses correspond to the exception and the checkpoint database states above
   * such that if the deferred result contains
   *
   * CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_EXCEEDED, the
   * [ExplorationCheckpointDatabaseState.CHECKPOINT_DATABASE_SIZE_LIMIT_EXCEEDED] will be
   * passed to a successful AsyncResult.
   *
   * CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_NOT_EXCEEDED,
   * [ExplorationCheckpointDatabaseState.CHECKPOINT_DATABASE_SIZE_LIMIT_NOT_EXCEEDED] will be
   * passed to a successful AsyncResult.
   *
   * CHECKPOINT_NOT_FOUND, the [ExplorationCheckpointNotFoundException] will be passed to a failed
   * AsyncResult.
   *
   * SUCCESS corresponds to successful AsyncResult with value as null.
   */
  enum class ExplorationCheckpointActionStatus {
    CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_NOT_EXCEEDED,
    CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_EXCEEDED,
    CHECKPOINT_NOT_FOUND,
    SUCCESS
  }

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<ExplorationCheckpointDatabase>>()

  /**
   * Records an exploration checkpoint for the specified profile.
   *
   * @return a [DataProvider] that indicates the success/failure of the save operation.
   *
   * Success result is returned if the checkpoint is saved successfully. The success result
   * returned has the value
   * [ExplorationCheckpointDatabaseState.CHECKPOINT_DATABASE_SIZE_LIMIT_EXCEEDED]
   * if the database has exceeded the size limit of [explorationCheckpointDatabaseSizeLimit],
   * otherwise the success result is returned with the value
   * [ExplorationCheckpointDatabaseState.CHECKPOINT_DATABASE_SIZE_LIMIT_NOT_EXCEEDED].
   */
  fun recordExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String,
    explorationCheckpoint: ExplorationCheckpoint
  ): DataProvider<Any?> {
    Log.d("12345", "recordExplorationCheckpoint: SAVING CHECKPOINT WITH TIMESTAMP ${explorationCheckpoint}")
    val deferred =
      retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
        updateInMemoryCache = true
      ) {
        val explorationCheckpointDatabaseBuilder = it.toBuilder()

        val checkpoint = explorationCheckpointDatabaseBuilder
          .explorationCheckpointMap[explorationId]

        // add checkpoint to the map if it was not saved previously.
        if (checkpoint == null) {
          explorationCheckpointDatabaseBuilder
            .putExplorationCheckpoint(explorationId, explorationCheckpoint)
        } else {
          // update the timestamp to the time when the checkpoint was saved for the first time and
          // then replace the existing checkpoint in the map with the updated checkpoint.
          explorationCheckpointDatabaseBuilder.putExplorationCheckpoint(
            explorationId,
            explorationCheckpoint.toBuilder()
              .setTimestampOfFirstCheckpoint(checkpoint.timestampOfFirstCheckpoint)
              .build()
          )
        }

        val explorationCheckpointDatabase = explorationCheckpointDatabaseBuilder.build()

        if (explorationCheckpointDatabase.serializedSize <= explorationCheckpointDatabaseSizeLimit)
          Pair(
            explorationCheckpointDatabase,
            ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_NOT_EXCEEDED
          )
        else
          Pair(
            explorationCheckpointDatabase,
            ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_EXCEEDED
          )
      }
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(
        deferred = deferred,
        profileId = profileId,
        explorationId = null
      )
    }
  }

  /** returns the saved checkpoint for a specified explorationId and profileId. */
  fun retrieveExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): DataProvider<ExplorationCheckpoint> {
    return retrieveCacheStore(profileId)
      .transformAsync(
        RETRIEVE_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID
      ) { explorationCheckpointDatabase ->

        val checkpoint = explorationCheckpointDatabase.explorationCheckpointMap[explorationId]

        if (checkpoint != null) {
          AsyncResult.success(checkpoint)
        } else
          AsyncResult.failed(
            ExplorationCheckpointNotFoundException(
              "Checkpoint with the explorationId $explorationId was not found " +
                "for profileId ${profileId.internalId}."
            )
          )
      }
  }

  /**
   * @return [ExplorationCheckpointDetails]  which contains the explorationId, explorationTitle
   * and explorationVersion of the oldest saved checkpoint for the specified profile.
   */
  fun retrieveOldestSavedExplorationCheckpointDetails(
    profileId: ProfileId
  ): DataProvider<ExplorationCheckpointDetails> {
    return retrieveCacheStore(profileId)
      .transformAsync(
        RETRIEVE_OLDEST_CHECKPOINT_DETAILS_DATA_PROVIDER_ID
      ) { explorationCheckpointDatabase ->

        // find the oldest checkpoint by timestamp or null if no checkpoints is saved.
        val oldestCheckpoint = explorationCheckpointDatabase.explorationCheckpointMap.minByOrNull {
          it.value.timestampOfFirstCheckpoint
        }

        if (oldestCheckpoint == null) {
          AsyncResult.failed(
            ExplorationCheckpointNotFoundException(
              "No saved checkpoints in $CACHE_NAME for profileId ${profileId.internalId}."
            )
          )
        } else {
          val explorationCheckpointDetails = ExplorationCheckpointDetails.newBuilder()
            .setExplorationId(oldestCheckpoint.key)
            .setExplorationTitle(oldestCheckpoint.value.explorationTitle)
            .setExplorationVersion(oldestCheckpoint.value.explorationVersion)
            .build()
          AsyncResult.success(explorationCheckpointDetails)
        }
      }
  }

  /** deletes the saved checkpoint for a specified explorationId and profileId. */
  fun deleteSavedExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): DataProvider<Any?> {
    val deferred = retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { explorationCheckpointDatabase ->

      if (!explorationCheckpointDatabase.explorationCheckpointMap.containsKey(explorationId))
        return@storeDataWithCustomChannelAsync Pair(
          explorationCheckpointDatabase,
          ExplorationCheckpointActionStatus.CHECKPOINT_NOT_FOUND
        )

      val explorationCheckpointDatabaseBuilder = explorationCheckpointDatabase.toBuilder()

      explorationCheckpointDatabaseBuilder
        .removeExplorationCheckpoint(explorationId)

      Pair(
        explorationCheckpointDatabaseBuilder.build(),
        ExplorationCheckpointActionStatus.SUCCESS
      )
    }
    return dataProviders.createInMemoryDataProviderAsync(
      DELETE_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync getDeferredResult(
        deferred = deferred,
        profileId = profileId,
        explorationId = explorationId
      )
    }
  }

  private suspend fun getDeferredResult(
    deferred: Deferred<ExplorationCheckpointActionStatus>,
    explorationId: String?,
    profileId: ProfileId?,
  ): AsyncResult<Any?> {
    return when (deferred.await()) {
      ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_NOT_EXCEEDED ->
        AsyncResult.success(
          ExplorationCheckpointDatabaseState.CHECKPOINT_DATABASE_SIZE_LIMIT_NOT_EXCEEDED
        )
      ExplorationCheckpointActionStatus.CHECKPOINT_SAVED_DATABASE_SIZE_LIMIT_EXCEEDED ->
        AsyncResult.success(
          ExplorationCheckpointDatabaseState.CHECKPOINT_DATABASE_SIZE_LIMIT_EXCEEDED
        )
      ExplorationCheckpointActionStatus.CHECKPOINT_NOT_FOUND ->
        AsyncResult.failed(
          ExplorationCheckpointNotFoundException(
            "No saved checkpoint with explorationId ${explorationId!!} found for " +
              "the profileId ${profileId!!.internalId}."
          )
        )
      ExplorationCheckpointActionStatus.SUCCESS ->
        AsyncResult.success(null)
    }
  }

  private fun retrieveCacheStore(
    profileId: ProfileId
  ): PersistentCacheStore<ExplorationCheckpointDatabase> {
    val cacheStore = if (profileId in cacheStoreMap) {
      cacheStoreMap[profileId]!!
    } else {
      val cacheStore =
        cacheStoreFactory.createPerProfile(
          CACHE_NAME,
          ExplorationCheckpointDatabase.getDefaultInstance(),
          profileId
        )
      cacheStoreMap[profileId] = cacheStore
      cacheStore
    }

    cacheStore.primeCacheAsync().invokeOnCompletion { throwable ->
      throwable?.let {
        oppiaLogger.e(
          "ExplorationCheckpointController",
          "Failed to prime cache ahead of LiveData conversion " +
            "for ExplorationCheckpointController.",
          it
        )
      }
    }
    return cacheStore
  }
}
