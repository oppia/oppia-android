package org.oppia.domain.exploration

import androidx.lifecycle.LiveData
import org.oppia.app.model.Exploration
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders
import javax.inject.Inject

private const val EXPLORATION_DATA_PROVIDER_ID = "ExplorationDataProvider"

/**
 * Controller for loading explorations by ID, or beginning to play an exploration.
 *
 * At most one exploration may be played at a given time, and its state will be managed by
 * [ExplorationProgressController].
 */
class ExplorationDataController @Inject constructor(
  private val explorationProgressController: ExplorationProgressController,
  private val explorationRetriever: ExplorationRetriever,
  private val dataProviders: DataProviders
) {
  /** Returns an [Exploration] given an ID. */
  fun getExplorationById(id: String): LiveData<AsyncResult<Exploration>> {
    val dataProvider = dataProviders.createInMemoryDataProviderAsync(EXPLORATION_DATA_PROVIDER_ID) {
      retrieveExplorationById(id)
    }
    return dataProviders.convertToLiveData(dataProvider)
  }

  /**
   * Begins playing an exploration of the specified ID. This method is not expected to fail.
   * [ExplorationProgressController] should be used to manage the play state, and monitor the load success/failure of
   * the exploration.
   *
   * This must be called only if no active exploration is being played. The previous exploration must have first been
   * stopped using [stopPlayingExploration] otherwise an exception will be thrown.
   */
  fun startPlayingExploration(explorationId: String) {
    explorationProgressController.beginExploration(explorationId)
  }

  /**
   * Finishes the most recent exploration started by [startPlayingExploration]. This method should only be called if an
   * active exploration is being played, otherwise an exception will be thrown.
   */
  fun stopPlayingExploration() {
    explorationProgressController.finishExploration()
  }

  @Suppress("RedundantSuspendModifier") // DataProviders expects this function to be a suspend function.
  private suspend fun retrieveExplorationById(explorationId: String): AsyncResult<Exploration> {
    return try {
      AsyncResult.success(explorationRetriever.loadExploration(explorationId))
    } catch (e: Exception) {
      AsyncResult.failed(e)
    }
  }
}
