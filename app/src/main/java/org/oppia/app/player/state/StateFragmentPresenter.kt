package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_6
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
  ) {

  var currentExplorationId: String = TEST_EXPLORATION_ID_5

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }

    explorationDataController.startPlayingExploration(TEST_EXPLORATION_ID_6)

    return binding.root
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  fun loadExploration(explorationId: String){
    explorationDataController.startPlayingExploration(
      explorationId
    ).observe(fragment, Observer<AsyncResult<Any?>> { result ->
      if (result.isPending()) {
        // Loading exploration
      } else if (result.isFailure()) {
        // Failed to load exploration.
        logger.e("StateFragmentPresenter", "Failed to load exploration", result.getErrorOrNull()!!)
      } else {
        // Exploration loaded successfully

      }
    })
  }

  fun unloadExploration(explorationId: String){
    explorationDataController.stopPlayingExploration().observe(fragment, Observer<AsyncResult<Any?>> { result ->
      if (result.isPending()) {
        // Failed to unload exploration.
      } else if (result.isFailure()) {
        // Unloading exploration
        logger.e("StateFragmentPresenter", "Failed to unload exploration", result.getErrorOrNull()!!)
      } else {
        // Exploration unloaded successfully
      }
    })
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    getStateViewModel().setAudioFragmentVisible(isVisible)
  }

  fun setNextButtonVisible(isVisible: Boolean) {
    getStateViewModel().setNextButtonVisible(isVisible)
  }

  fun setPreviousButtonVisible(isVisible: Boolean) {
    getStateViewModel().setPreviousButtonVisible(isVisible)
  }

  fun setContinueButtonVisible(isVisible: Boolean) {
    getStateViewModel().setContinueButtonVisible(isVisible)
  }

  fun setSubmitButtonVisible(isVisible: Boolean) {
    getStateViewModel().setSubmitButtonVisible(isVisible)
  }

  fun setEndExplorationButtonVisible(isVisible: Boolean) {
    getStateViewModel().setEndExplorationButtonVisible(isVisible)
  }

  fun hideAllButtons() {
    getStateViewModel().hideAllButtons()
  }
}
