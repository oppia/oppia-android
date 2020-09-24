package org.oppia.android.domain.exploration

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.oppia.android.app.model.Exploration
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders
import org.oppia.android.util.system.OppiaClock
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
  private val dataProviders: DataProviders,
  private val exceptionsController: ExceptionsController,
  private val oppiaClock: OppiaClock
) {
  /** Returns an [Exploration] given an ID. */
  fun getExplorationById(id: String): DataProvider<Exploration> {
    return dataProviders.createInMemoryDataProviderAsync(
      EXPLORATION_DATA_PROVIDER_ID
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
  fun startPlayingExploration(explorationId: String): LiveData<AsyncResult<Any?>> {
    return try {
      explorationProgressController.beginExplorationAsync(explorationId)
      MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e, oppiaClock.getCurrentCalendar().timeInMillis)
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
      MutableLiveData(AsyncResult.success<Any?>(null))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e, oppiaClock.getCurrentCalendar().timeInMillis)
      MutableLiveData(AsyncResult.failed(e))
    }
  }

  // DataProviders expects this function to be a suspend function.
  @Suppress("RedundantSuspendModifier")
  private suspend fun retrieveExplorationById(explorationId: String): AsyncResult<Exploration> {
    return try {
      AsyncResult.success(explorationRetriever.loadExploration(explorationId))
    } catch (e: Exception) {
      exceptionsController.logNonFatalException(e, oppiaClock.getCurrentCalendar().timeInMillis)
      AsyncResult.failed(e)
    }
  }
}
