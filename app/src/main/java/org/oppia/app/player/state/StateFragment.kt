package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.audio.CellularDataInterface
import org.oppia.domain.audio.CellularDialogController
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

/** Fragment that represents the current state of an exploration. */
class StateFragment @Inject constructor(
  private val cellularDialogController: CellularDialogController
) : InjectableFragment(), CellularDataInterface {
  @Inject
  lateinit var stateFragmentPresenter: StateFragmentPresenter
  // Control this boolean value from controllers in domain module.
  private var showCellularDataDialog = true //TODO

  init {
    cellularDialogController.getCellularDataPreference()
      .observe(this, Observer<AsyncResult<CellularDataPreference>>{
      if (it.isSuccess()) {
        showCellularDataDialog = it.getOrDefault(CellularDataPreference.getDefaultInstance()).showDialog
      }
    })
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return stateFragmentPresenter.handleCreateView(inflater, container)
  }

  fun dummyButtonClicked() {
    if (showCellularDataDialog) {
      stateFragmentPresenter.setAudioFragmentVisible(false)
      showCellularDataDialogFragment()
    } else {
      stateFragmentPresenter.setAudioFragmentVisible(true)
    }
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = childFragmentManager.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance()
    dialogFragment.showNow(childFragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  override fun enableAudioWhileOnCellular(saveUserChoice: Boolean) {
    stateFragmentPresenter.setAudioFragmentVisible(true)
    if (saveUserChoice) cellularDialogController.setShowDialogPreference(true)
  }

  override fun disableAudioWhileOnCellular(saveUserChoice: Boolean) {
    if (saveUserChoice) cellularDialogController.setShowDialogPreference(false)
  }
}
