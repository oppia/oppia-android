package org.oppia.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val context: Context,
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) {

  private var showCellularDataDialog = true
  private var useCellularData = false

  private var items: Array<String>? = null
  var customizationArgsMap  = HashMap<String,InteractionObject>()
  var interactionInstanceId: String? = null
  private var entity_type: String = ""
  private var entity_id: String = ""

  var interactionAdapter: InteractionAdapter? = null


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

    getCurrentState(binding)

    return binding.root
  }

  fun handleAudioClick() {
    if (showCellularDataDialog) {
      setAudioFragmentVisible(false)
      showCellularDataDialogFragment()
    } else {
      setAudioFragmentVisible(useCellularData)
    }
  }

  fun handleEnableAudio(saveUserChoice: Boolean) {
    setAudioFragmentVisible(true)
    if (saveUserChoice)
      cellularDialogController.setAlwaysUseCellularDataPreference()
  }

  fun handleDisableAudio(saveUserChoice: Boolean) {
    if (saveUserChoice)
      cellularDialogController.setNeverUseCellularDataPreference()
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

  fun setAudioFragmentVisible(isVisible: Boolean) {
    getStateViewModel().setAudioFragmentVisible(isVisible)
  }

  private fun getCurrentState(binding: StateFragmentBinding) {
    ephemeralStateLiveData.observe(fragment, Observer<EphemeralState> { result ->

      entity_type = "exploration"
      // TODO replace exploration Id from result. Exploration id is missing in proto
      entity_id = "DIWZiVgs0km-"
      val customizationArgsMap: Map<String, InteractionObject> = result.state.interaction.customizationArgsMap
      logger.d("TAG", "getCurrentState: " + result.state)
      val allKeys: Set<String> = customizationArgsMap.keys

      for (key in allKeys) {
        logger.d("StateFragment", key)
      }
      if (customizationArgsMap.contains("choices")) {
        val customizationArgs : InteractionObject?= customizationArgsMap["choices"]
        logger.d("StateFragment", "value: ${customizationArgs}")
      }
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
      logger.e("StateFragmentPresenter", "Failed to retrieve ephemeral state", ephemeralStateResult.getErrorOrNull()!!)
    }
    return ephemeralStateResult.getOrDefault(EphemeralState.getDefaultInstance())
  }

  private fun showInputInteractions(binding: StateFragmentBinding) {
    val gaeCustomizationArgs: Any? = customizationArgsMap.values
    if (interactionInstanceId.equals("MultipleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      items = gaeCustomArgsInString.split(",").toTypedArray()
      interactionAdapter = InteractionAdapter(context, entity_type, entity_id, items, interactionInstanceId);
      binding.rvInteractions.adapter = interactionAdapter

    } else if (interactionInstanceId.equals("ItemSelectionInput") || interactionInstanceId.equals("SingleChoiceInput")) {
      val gaeCustomArgsInString: String = gaeCustomizationArgs.toString().replace("[", "").replace("]", "")
      items = gaeCustomArgsInString.split(",").toTypedArray()
      interactionAdapter = InteractionAdapter(context, entity_type, entity_id, items, interactionInstanceId);
      binding.rvInteractions.adapter = interactionAdapter
    } else {
      //Do no show any view
    }
  }
}
