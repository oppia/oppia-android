package org.oppia.android.domain.exploration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ProfileId
import org.oppia.android.domain.exploration.lightweightcheckpointing.ExplorationCheckpointController
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import javax.inject.Inject

private const val GET_EXPLORATION_BY_ID_PROVIDER_ID =
  "get_exploration_by_id_provider_id"

/**
 * Controller for loading explorations by ID, or beginning to play an exploration. This controller
 * is also responsible for controlling the saved checkpoints when the exploration is started or
 * stopped.
 *
 * At most one exploration may be played at a given time, and its state will be managed by
 * [ExplorationProgressController].
 */
class ExplorationDataController @Inject constructor(
  private val explorationProgressController: ExplorationProgressController,
  private val explorationRetriever: ExplorationRetriever,
  private val dataProviders: DataProviders,
  private val exceptionsController: ExceptionsController,
  private val explorationCheckpointController: ExplorationCheckpointController
) {
  /** Returns an [Exploration] given an ID. */
  fun getExplorationById(id: String): DataProvider<Exploration> {
    return dataProviders.createInMemoryDataProviderAsync(
      GET_EXPLORATION_BY_ID_PROVIDER_ID
    ) {
      retrieveExplorationById(id)
    }
  }

  /**
   * Begins playing an exploration of the specified ID. This method is not expected to fail.
   * [ExplorationProgressController] should be used to manage the play state, and monitor the load success/failure of
   * the exploration.
   *
   * This must be called only if no active exploration is being played. The previous exploration must have first been
   * stopped using [stopPlayingExploration] otherwise an exception will be thrown.
   *
   * @return a one-time [LiveData] to observe whether initiating the play request succeeded. The exploration may still
   *     fail to load, but this provides early-failure detection.
   */
  fun startPlayingExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    isCheckpointingEnabled: Boolean
  ): LiveData<AsyncResult<Any?>> {
    return try {
      explorationProgressController.beginExplorationAsync(
        internalProfileId,
        topicId,
        storyId,
        explorationId,
        isCheckpointingEnabled
      )
      MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  /**
   * Finishes the most recent exploration started by [startPlayingExploration]. This method should only be called if an
   * active exploration is being played, otherwise an exception will be thrown.
   */
  fun stopPlayingExploration(): LiveData<AsyncResult<Any?>> {
    return try {
      explorationProgressController.finishExplorationAsync()
      MutableLiveData(AsyncResult.success(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  /**
   * Checks the checkpoint state to make sure that checkpointing has been successful up-to that
   * point in the exploration.
   *
   * @return a one-time [LiveData] with success result if checkpointing has been successful
   *         otherwise a failure result with an appropriate exception is returned.
   */
  fun checkHasCheckpointingBeenSuccessful(): LiveData<AsyncResult<Any?>> {
    return try {
      explorationProgressController.isCurrentCheckpointStateIsSavedDatabaseNotExceededLimit()
      MutableLiveData(AsyncResult.success(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  /** Function to fetch the details of the oldest saved exploration for a specified profileId.*/
  fun getOldestExplorationDetailsDataProvider(profileId: ProfileId) =
    explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(profileId)

  /**
   * Kicks off the operation to delete the saved progress for the exploration specified by the
   * exploration id and profile id.
   */
  fun deleteExplorationProgressById(profileId: ProfileId, explorationId: String) {
    explorationCheckpointController.deleteSavedExplorationCheckpoint(
      profileId,
      explorationId
    )
  }

  // DataProviders expects this function to be a suspend function.
  @Suppress("RedundantSuspendModifier")
  private suspend fun retrieveExplorationById(explorationId: String): AsyncResult<Exploration> {
    return try {
      AsyncResult.success(explorationRetriever.loadExploration(explorationId))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.failed(e)
    }
  }
}
