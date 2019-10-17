package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"
private const val TAG_AUDIO_FRAGMENT = "AUDIO_FRAGMENT"

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) {

  private var showCellularDataDialog = true
  private var useCellularData = false
  private var explorationId: String? = null

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    cellularDialogController.getCellularDataPreference()
      .observe(fragment, Observer<AsyncResult<CellularDataPreference>> {
        if (it.isSuccess()) {
          val prefs = it.getOrDefault(CellularDataPreference.getDefaultInstance())
          showCellularDataDialog = !(prefs.hideDialog)
          useCellularData = prefs.useCellularData
        }
      })

    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }
    explorationId = fragment.arguments!!.getString(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)

    subscribeToCurrentState()

    return binding.root
  }

  fun handleAudioClick() {
    if (showCellularDataDialog) {
      showHideAudioFragment(false)
      showCellularDataDialogFragment()
    } else {
      if (useCellularData) {
        showHideAudioFragment(getAudioFragment() == null)
      } else {
        showHideAudioFragment(false)
      }
    }
  }

  fun handleEnableAudio(saveUserChoice: Boolean) {
    showHideAudioFragment(true)
    if (saveUserChoice) {
      cellularDialogController.setAlwaysUseCellularDataPreference()
    }
  }

  fun handleDisableAudio(saveUserChoice: Boolean) {
    if (saveUserChoice) {
      cellularDialogController.setNeverUseCellularDataPreference()
    }
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(TAG_AUDIO_FRAGMENT)
  }

  private fun showHideAudioFragment(isVisible: Boolean) {
    if (isVisible) {
      if (getAudioFragment() == null) {
        fragment.childFragmentManager.beginTransaction().add(
          R.id.audio_fragment_placeholder, AudioFragment(),
          TAG_AUDIO_FRAGMENT
        ).commitNow()
      }
    } else {
      if (getAudioFragment() != null) {
        fragment.childFragmentManager.beginTransaction().remove(getAudioFragment()!!).commitNow()
      }
    }
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment, Observer<EphemeralState> { result ->
      logger.d("StateFragment", "getCurrentState: ${result.state.name}")
    })
  }

  private val ephemeralStateLiveData: LiveData<EphemeralState> by lazy {
    getEphemeralState()
  }

  private fun getEphemeralState(): LiveData<EphemeralState> {
    return Transformations.map(explorationProgressController.getCurrentState(), ::processCurrentState)
  }

  private fun processCurrentState(ephemeralStateResult: AsyncResult<EphemeralState>): EphemeralState {
    if (ephemeralStateResult.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state", ephemeralStateResult.getErrorOrNull()!!)
    }
    return ephemeralStateResult.getOrDefault(EphemeralState.getDefaultInstance())
  }
}
