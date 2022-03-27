package org.oppia.android.domain.exploration

import org.oppia.android.app.model.Exploration
import org.oppia.android.app.model.ExplorationCheckpoint
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
   * Begins playing an exploration of the specified ID.
   *
   * This method is not expected to fail.
   *
   * [ExplorationProgressController] should be used to manage the play state, and monitor the load
   * success/failure of the exploration.
   *
   * This must be called only if no active exploration is being played. The previous exploration
   * must have first been stopped using [stopPlayingExploration], otherwise the operation will fail.
   *
   * @param internalProfileId the ID corresponding to the profile for which exploration has to be
   *     played
   * @param topicId the ID corresponding to the topic for which exploration has to be played
   * @param storyId the ID corresponding to the story for which exploration has to be played
   * @param explorationId the ID of the exploration which has to be played
   * @param shouldSavePartialProgress the boolean that indicates if partial progress has to be saved
   *     for the current exploration
   * @param explorationCheckpoint the checkpoint which may be used to resume the exploration
   * @return a one-time [DataProvider] to observe whether initiating the play request succeeded.
   *     The exploration may still fail to load, but this provides early-failure detection.
   */
  fun startPlayingExploration(
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String,
    shouldSavePartialProgress: Boolean,
    explorationCheckpoint: ExplorationCheckpoint
  ): DataProvider<Any?> {
    return explorationProgressController.beginExplorationAsync(
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      shouldSavePartialProgress,
      explorationCheckpoint
    )
  }

  /**
   * Finishes the most recent exploration started by [startPlayingExploration], and returns a
   * one-off [DataProvider] indicating whether the operation succeeded.
   *
   * This method should only be called if an active exploration is being played, otherwise the
   * operation will fail.
   */
  fun stopPlayingExploration(): DataProvider<Any?> =
    explorationProgressController.finishExplorationAsync()

  /**
   * Fetches the details of the oldest saved exploration for a specified profileId.
   *
   * @param profileId the ID corresponding to the profile for which the oldest checkpoint details
   *     has to be retrieved
   * @return a [DataProvider] that indicates the success or failure of the retrieve operation
   */
  fun getOldestExplorationDetailsDataProvider(profileId: ProfileId) =
    explorationCheckpointController.retrieveOldestSavedExplorationCheckpointDetails(profileId)

  /**
   * Kicks off the operation to delete the saved progress for the exploration specified by the
   * exploration id and profile id.
   *
   * @param profileId the ID corresponding to the profile for which the oldest checkpoint details
   *     has to be retrieved
   * @param explorationId the ID of the exploration whose checkpoint has to be deleted
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
      AsyncResult.Success(explorationRetriever.loadExploration(explorationId))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e)
      AsyncResult.Failure(e)
    }
  }
}
