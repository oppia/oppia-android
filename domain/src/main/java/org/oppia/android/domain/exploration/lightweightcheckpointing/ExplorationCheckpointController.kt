package org.oppia.android.domain.exploration.lightweightcheckpointing

import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.Deferred
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.ExplorationCheckpoint
import org.oppia.android.app.model.ExplorationCheckpointDatabase
import org.oppia.android.app.model.ExplorationCheckpointDetails
import org.oppia.android.app.model.ProfileId
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.data.persistence.PersistentCacheStore.PublishMode
import org.oppia.android.data.persistence.PersistentCacheStore.UpdateMode
import org.oppia.android.domain.exploration.ExplorationRetriever
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
private const val CHECK_IS_EXPLORATION_CHECKPOINT_COMPATIBLE_WITH_EXPLORATION_DATA_PROVIDER_ID =
  "check_is_exploration_checkpoint_compatible_with_exploration_provider_id"

/**
 * Controller for saving, retrieving, updating, and deleting exploration checkpoints.
 */
@Singleton
class ExplorationCheckpointController @Inject constructor(
  private val cacheStoreFactory: PersistentCacheStore.Factory,
  private val dataProviders: DataProviders,
  private val oppiaLogger: OppiaLogger,
  @ExplorationStorageDatabaseSize private val explorationCheckpointDatabaseSizeLimit: Int,
  private val explorationRetriever: ExplorationRetriever
) {

  /** Indicates that no checkpoint was found for the specified explorationId and profileId. */
  class ExplorationCheckpointNotFoundException(message: String) : Exception(message)

  /** Indicates that no checkpoint was found for the specified explorationId and profileId. */
  class OutdatedExplorationCheckpointException(message: String) : Exception(message)

  /**
   * These Statuses correspond to the result of the deferred such that if the deferred contains
   *
   * CHECKPOINT_NOT_FOUND, the [ExplorationCheckpointNotFoundException] will be passed to a failed
   * AsyncResult.
   *
   * SUCCESS corresponds to a successful AsyncResult.
   */
  enum class ExplorationCheckpointActionStatus {
    CHECKPOINT_NOT_FOUND,
    SUCCESS
  }

  private val cacheStoreMap =
    mutableMapOf<ProfileId, PersistentCacheStore<ExplorationCheckpointDatabase>>()

  /**
   * Records an exploration checkpoint for the specified profile.
   *
   * @return a [Deferred] that upon completion indicates the current [CheckpointState].
   *     If the size of the checkpoint database is less than the allocated limit of
   *     [ExplorationStorageDatabaseSize] then the deferred upon completion gives the result
   *     [CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT]. If the size of the
   *     checkpoint database exceeded [ExplorationStorageDatabaseSize] then
   *     [CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT] is returned upon successful
   *     completion of deferred.
   */
  internal fun recordExplorationCheckpointAsync(
    profileId: ProfileId,
    explorationId: String,
    explorationCheckpoint: ExplorationCheckpoint
  ): Deferred<CheckpointState> {
    return retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) {
      val explorationCheckpointDatabaseBuilder = it.toBuilder()

      val checkpoint = explorationCheckpointDatabaseBuilder
        .explorationCheckpointMap[explorationId]

      // Add checkpoint to the map if it was not saved previously.
      if (checkpoint == null) {
        explorationCheckpointDatabaseBuilder
          .putExplorationCheckpoint(explorationId, explorationCheckpoint)
      } else {
        // Update the timestamp to the time when the checkpoint was saved for the first time and
        // then replace the existing checkpoint in the map with the updated checkpoint.
        explorationCheckpointDatabaseBuilder.putExplorationCheckpoint(
          explorationId,
          explorationCheckpoint.toBuilder()
            .setTimestampOfFirstCheckpoint(checkpoint.timestampOfFirstCheckpoint)
            .build()
        )
      }

      val explorationCheckpointDatabase = explorationCheckpointDatabaseBuilder.build()

      if (explorationCheckpointDatabase.serializedSize <= explorationCheckpointDatabaseSizeLimit) {
        Pair(
          explorationCheckpointDatabase,
          CheckpointState.CHECKPOINT_SAVED_DATABASE_NOT_EXCEEDED_LIMIT
        )
      } else {
        Pair(
          explorationCheckpointDatabase,
          CheckpointState.CHECKPOINT_SAVED_DATABASE_EXCEEDED_LIMIT
        )
      }
    }
  }

  /**
   * Returns a [DataProvider] for the [Deferred] returned from [recordExplorationCheckpointAsync].
   */
  fun recordExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String,
    explorationCheckpoint: ExplorationCheckpoint
  ): DataProvider<Any?> {
    val deferred = recordExplorationCheckpointAsync(
      profileId,
      explorationId,
      explorationCheckpoint
    )
    return dataProviders.createInMemoryDataProviderAsync(
      RECORD_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID
    ) {
      return@createInMemoryDataProviderAsync AsyncResult.Success(deferred.await())
    }
  }

  /** Returns the saved checkpoint for a specified explorationId and profileId. */
  fun retrieveExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): DataProvider<ExplorationCheckpoint> {
    return retrieveCacheStore(profileId)
      .transformAsync(
        RETRIEVE_EXPLORATION_CHECKPOINT_DATA_PROVIDER_ID
      ) { explorationCheckpointDatabase ->

        val checkpoint = explorationCheckpointDatabase.explorationCheckpointMap[explorationId]
        val exploration = explorationRetriever.loadExploration(explorationId)

        when {
          checkpoint != null && exploration.version == checkpoint.explorationVersion -> {
            AsyncResult.Success(checkpoint)
          }
          checkpoint != null && exploration.version != checkpoint.explorationVersion -> {
            AsyncResult.Failure(
              OutdatedExplorationCheckpointException(
                "checkpoint with version: ${checkpoint.explorationVersion} cannot be used to " +
                  "resume exploration $explorationId with version: ${exploration.version}"
              )
            )
          }
          else -> {
            AsyncResult.Failure(
              ExplorationCheckpointNotFoundException(
                "Checkpoint with the explorationId $explorationId was not found " +
                  "for profileId ${profileId.internalId}."
              )
            )
          }
        }
      }
  }

  /**
   * Retrieves details about the oldest saved exploration checkpoint.
   *
   * @return [ExplorationCheckpointDetails]  which contains the explorationId, explorationTitle
   *      and explorationVersion of the oldest saved checkpoint for the specified profile.
   */
  fun retrieveOldestSavedExplorationCheckpointDetails(
    profileId: ProfileId
  ): DataProvider<ExplorationCheckpointDetails> {
    return retrieveCacheStore(profileId)
      .transformAsync(
        RETRIEVE_OLDEST_CHECKPOINT_DETAILS_DATA_PROVIDER_ID
      ) { explorationCheckpointDatabase ->

        // Find the oldest checkpoint by timestamp or null if no checkpoints is saved.
        val oldestCheckpoint =
          explorationCheckpointDatabase.explorationCheckpointMap.minByOrNull {
            it.value.timestampOfFirstCheckpoint
          }

        if (oldestCheckpoint != null) {
          val explorationCheckpointDetails = ExplorationCheckpointDetails.newBuilder()
            .setExplorationId(oldestCheckpoint.key)
            .setExplorationTitle(oldestCheckpoint.value.explorationTitle)
            .setExplorationVersion(oldestCheckpoint.value.explorationVersion)
            .build()
          AsyncResult.Success(explorationCheckpointDetails)
        } else {
          AsyncResult.Failure(
            ExplorationCheckpointNotFoundException(
              "No saved checkpoints in $CACHE_NAME for profileId ${profileId.internalId}."
            )
          )
        }
      }
  }

  /** Deletes the saved checkpoint for a specified explorationId and profileId. */
  fun deleteSavedExplorationCheckpoint(
    profileId: ProfileId,
    explorationId: String
  ): DataProvider<Any?> {
    val deferred = retrieveCacheStore(profileId).storeDataWithCustomChannelAsync(
      updateInMemoryCache = true
    ) { explorationCheckpointDatabase ->

      if (!explorationCheckpointDatabase.explorationCheckpointMap.containsKey(explorationId)) {
        return@storeDataWithCustomChannelAsync Pair(
          explorationCheckpointDatabase,
          ExplorationCheckpointActionStatus.CHECKPOINT_NOT_FOUND
        )
      }

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
      ExplorationCheckpointActionStatus.CHECKPOINT_NOT_FOUND ->
        AsyncResult.Failure(
          ExplorationCheckpointNotFoundException(
            "No saved checkpoint with explorationId ${explorationId!!} found for " +
              "the profileId ${profileId!!.internalId}."
          )
        )
      ExplorationCheckpointActionStatus.SUCCESS -> AsyncResult.Success(null)
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

    cacheStore.primeInMemoryAndDiskCacheAsync(
      updateMode = UpdateMode.UPDATE_IF_NEW_CACHE,
      publishMode = PublishMode.DO_NOT_PUBLISH_TO_IN_MEMORY_CACHE
    ).invokeOnCompletion { throwable ->
      throwable?.let {
        oppiaLogger.e(
          "ExplorationCheckpointController",
          "Failed to prime cache ahead of data retrieval for ExplorationCheckpointController.",
          it
        )
      }
    }
    return cacheStore
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun getExplorationCheckpointDatabaseSizeLimit(): Int = explorationCheckpointDatabaseSizeLimit
}
