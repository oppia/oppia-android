package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StateViewModel>
) {
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }
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

  fun setActiveSubmitButtonVisible(isVisible: Boolean) {
    getStateViewModel().setActiveSubmitButtonVisible(isVisible)
  }

  fun setInactiveSubmitButtonVisible(isVisible: Boolean) {
    getStateViewModel().setInactiveSubmitButtonVisible(isVisible)
  }

  fun setLearnAgainButtonVisible(isVisible: Boolean) {
    getStateViewModel().setLearnAgainButtonVisible(isVisible)
  }

  fun setEndExplorationButtonVisible(isVisible: Boolean) {
    getStateViewModel().setEndExplorationButtonVisible(isVisible)
  }

  fun hideAllButtons() {
    getStateViewModel().hideAllButtons()
  }
}
