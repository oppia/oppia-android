package org.oppia.android.app.player.exploration

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import javax.inject.Inject

/** The presenter for [HintsAndSolutionExplorationManagerFragment]. */
@FragmentScope
class HintsAndSolutionExplorationManagerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val oppiaLogger: OppiaLogger,
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
    when (result) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "HintsAndSolutionExplorationManagerFragmentPresenter",
          "Failed to retrieve ephemeral state",
          result.error
        )
      }
      // Display nothing until a valid result is available.
      is AsyncResult.Pending -> {}
      is AsyncResult.Success -> {
        // Check if hints are available for this state.
        val ephemeralState = result.value
        val state = ephemeralState.state
        if (state.interaction.hintList.isNotEmpty() || state.interaction.hasSolution()) {
          (activity as HintsAndSolutionExplorationManagerListener).onExplorationStateLoaded(
            state, ephemeralState.writtenTranslationContext
          )
        }
      }
    }
  }
}
