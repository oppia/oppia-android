package org.oppia.app.player.state

import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.R
import org.oppia.app.customview.inputInteractionView.FractionInputInteractionView
import org.oppia.app.customview.inputInteractionView.NumberInputInteractionView
import org.oppia.app.customview.inputInteractionView.TextInputInteractionView
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>
) {

  private var showCellularDataDialog = true
  private var useCellularData = false

  private var llRoot: LinearLayout? = null
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    cellularDialogController.getCellularDataPreference()
      .observe(fragment, Observer<AsyncResult<CellularDataPreference>>{
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
    llRoot = binding.root.findViewById(R.id.llRoot)
    addTextInputContentCard("text")
    addNumberInputContentCard("number 1")
    addFractionInputContentCard("fraction 1/1")
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

  /** The function for adding [TextInputInteractionView]. */
  fun addTextInputContentCard(placeholder: String) {
    var contentComponent = TextInputInteractionView(
      fragment.context!!,
      placeholder,
      1
    )
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
    llRoot!!.addView(contentComponent, params)
  }

  /** The function for adding [NumberInputInteractionView]. */
  fun addNumberInputContentCard(placeholder: String) {
    var contentComponent = NumberInputInteractionView(
      fragment.context!!,
      placeholder,
      1
    )
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
    llRoot!!.addView(contentComponent, params)

  }

  /** The function for adding [FractionInputInteractionView]. */
  fun addFractionInputContentCard(placeholder: String) {
    var contentComponent = FractionInputInteractionView(
      fragment.context!!,
      placeholder,
      1
    )
    val params = LinearLayout.LayoutParams(
      LinearLayout.LayoutParams.MATCH_PARENT,
      LinearLayout.LayoutParams.WRAP_CONTENT
    )
    params.setMargins(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8))
    llRoot!!.addView(contentComponent, params)
  }

  fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().getDisplayMetrics().density).toInt()
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    getStateViewModel().setAudioFragmentVisible(isVisible)
  }
}
