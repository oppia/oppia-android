package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.audio.CellularDataInterface
import javax.inject.Inject
/** Fragment that contains skills for topic train mode. */
private const val KEY_DIGIT_ID = "DIGIT_ID"
/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), CellularDataInterface {

  @Inject lateinit var stateFragmentPresenter: StateFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    var digit :String =""
    if (savedInstanceState != null) {
      digit = savedInstanceState.getString(KEY_DIGIT_ID)
    }
    return stateFragmentPresenter.handleCreateView(inflater, container, digit)
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString(KEY_DIGIT_ID,stateFragmentPresenter.getNumberTextInputText())
  }
  override fun enableAudioWhileOnCellular(saveUserChoice: Boolean) = stateFragmentPresenter.handleEnableAudio(saveUserChoice)
  override fun disableAudioWhileOnCellular(saveUserChoice: Boolean) = stateFragmentPresenter.handleDisableAudio(saveUserChoice)
  fun dummyButtonClicked() = stateFragmentPresenter.handleAudioClick()
}
