package org.oppia.app.player.state.hintsandsolution

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.EphemeralState
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [HintsAndSolutionExplorationManagerFragment]. */
@FragmentScope
class HintsAndSolutionExplorationManagerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val logger: Logger,
  private val explorationProgressController: ExplorationProgressController
) {
  private var explorationId = ""
  private var newAvailableHintIndex = 0
  private var allHintsExhausted = false

  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState()
  }

  fun handleCreateView(
    id: String,
    newAvailableHintIndex: Int,
    allHintsExhausted: Boolean
  ): View? {
    this.explorationId = id
    this.newAvailableHintIndex = newAvailableHintIndex
    this.allHintsExhausted = allHintsExhausted
    subscribeToCurrentState()

    return null // Headless fragment.
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(activity, Observer<AsyncResult<EphemeralState>> { result ->
      processEphemeralStateResult(result)
    })
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralState>) {
    if (result.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state", result.getErrorOrNull()!!)
      return
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }

    val ephemeralState = result.getOrThrow()

    // Check if hints are available for this state.
    if (ephemeralState.state.interaction.hintList.size != 0) {
      (activity as HintsAndSolutionExplorationManagerListener).onExplorationStateLoaded(
        ephemeralState.state, explorationId, newAvailableHintIndex, allHintsExhausted
      )
    }
  }
}
