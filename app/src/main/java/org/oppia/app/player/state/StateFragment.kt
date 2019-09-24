package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ObservableField
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.audio.CellularDataInterface
import javax.inject.Inject

private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), CellularDataInterface {
  @Inject
  lateinit var stateFragmentPresenter: StateFragmentPresenter
  // Control this boolean value from controllers in domain module.
  private var showCellularDataDialog = true
  var isAudioFragmentShowing = ObservableField<Boolean>(false)

  init {
    // TODO(#116): Code to control the value of showCellularDataDialog using AudioController.
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return stateFragmentPresenter.handleCreateView(inflater, container)
  }

  fun dummyButtonClicked() {
    if (showCellularDataDialog && !isAudioFragmentShowing.get()!!) {
      showCellularDataDialogFragment()
    } else {
      isAudioFragmentShowing.set(false)
    }
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragmentManager?.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragmentManager?.beginTransaction()?.remove(previousFragment)?.commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance(this)
    dialogFragment.showNow(fragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  override fun enableAudioWhileOnCellular(saveUserChoice: Boolean) {
    isAudioFragmentShowing.set(true)
    // saveUserChoice -> true -> save this preference
    // saveUserChoice -> false -> do not save this preference
  }

  override fun disableAudioWhileOnCellular(saveUserChoice: Boolean) {
    // saveUserChoice -> true -> save this preference
    // saveUserChoice -> false -> do not save this preference
  }
}
