package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_5
import org.oppia.domain.exploration.TEST_EXPLORATION_ID_6
import javax.inject.Inject

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationDataController: ExplorationDataController
) {
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
