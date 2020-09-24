package org.oppia.android.app.player.exploration

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.logging.ConsoleLogger
import javax.inject.Inject

/** The presenter for [HintsAndSolutionExplorationManagerFragment]. */
@FragmentScope
class HintsAndSolutionExplorationManagerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: ConsoleLogger,
  private val explorationProgressController: ExplorationProgressController
) {

  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState().toLiveData()
  }

  fun handleCreateView(): View? {
    subscribeToCurrentState()

    return null // Headless fragment.
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(
      activity,
      Observer<AsyncResult<EphemeralState>> { result ->
        processEphemeralStateResult(result)
      }
    )
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralState>) {
    if (result.isFailure()) {
      logger.e(
        "HintsAndSolutionExplorationManagerFragmentPresenter",
        "Failed to retrieve ephemeral state",
        result.getErrorOrNull()!!
      )
      return
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }

    val ephemeralState = result.getOrThrow()

    // Check if hints are available for this state.
    if (ephemeralState.state.interaction.hintList.size != 0) {
      (activity as HintsAndSolutionExplorationManagerListener).onExplorationStateLoaded(
        ephemeralState.state
      )
    }
  }
}
