package org.oppia.app.player.state.hintsandsolution

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.EphemeralState
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val KEY_EXPLORATION_ID = "EXPLORATION_ID"
private const val KEY_NEW_AVAILABLE_HINT_INDEX = "NEW_AVAILABLE_HINT_INDEX"
private const val KEY_ALL_HINTS_EXHAUSTED = "ALL_HINTS_EXHAUSTED"

/**
 * ManagerFragment of [ExplorationFragment] that observes data provider that retrieve Exploration State.
 */
class HintsAndSolutionExplorationManagerFragment : InjectableFragment() {
  @Inject
  lateinit var hintsAndSolutionExplorationManagerFragmentPresenter: HintsAndSolutionExplorationManagerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      checkNotNull(arguments) { "Expected arguments to be passed to HintsAndSolutionFragment" }
    val id =
      checkNotNull(args.getString(KEY_EXPLORATION_ID)) { "Expected id to be passed to HintsAndSolutionFragment" }
    val newAvailableHintIndex =
      checkNotNull(args.getInt(KEY_NEW_AVAILABLE_HINT_INDEX)) { "Expected hint index to be passed to HintsAndSolutionFragment" }
    val allHintsExhausted =
      checkNotNull(args.getBoolean(KEY_ALL_HINTS_EXHAUSTED)) { "Expected if hints exhausted to be passed to HintsAndSolutionFragment" }

    return hintsAndSolutionExplorationManagerFragmentPresenter.handleCreateView(
      id,
      newAvailableHintIndex,
      allHintsExhausted
    )
  }

  companion object {

    fun newInstance(
      explorationId: String,
      newAvailableHintIndex: Int,
      allHintsExhausted: Boolean
    ): HintsAndSolutionExplorationManagerFragment {
      val explorationManagerFragment = HintsAndSolutionExplorationManagerFragment()
      val args = Bundle()
      args.putString(KEY_EXPLORATION_ID, explorationId)
      args.putInt(KEY_NEW_AVAILABLE_HINT_INDEX, newAvailableHintIndex)
      args.putBoolean(KEY_ALL_HINTS_EXHAUSTED, allHintsExhausted)
      explorationManagerFragment.arguments = args
      return explorationManagerFragment
    }
  }
}

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
