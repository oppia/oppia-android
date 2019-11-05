package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.audio.CellularDataInterface
import javax.inject.Inject

internal const val KEY_SELECTED_INPUT_INDEXES = "SELECTED_INPUT_INDEXES"

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), CellularDataInterface, SelectInputItemsListener {
  companion object {
    /**
     * Creates a new instance of a StateFragment.
     * @param explorationId used by StateFragment.
     * @return a new instance of [StateFragment].
     */
    fun newInstance(explorationId: String): StateFragment {
      val stateFragment = StateFragment()
      val args = Bundle()
      args.putString(STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY, explorationId)
      stateFragment.arguments = args
      return stateFragment
    }
  }

  private var selectedInputItemIndexes = ArrayList<Int>()

  @Inject
  lateinit var stateFragmentPresenter: StateFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    if (savedInstanceState != null) {
      selectedInputItemIndexes = savedInstanceState.getIntegerArrayList(KEY_SELECTED_INPUT_INDEXES)
    }
    return stateFragmentPresenter.handleCreateView(inflater, container, selectedInputItemIndexes, this as SelectInputItemsListener)
  }

  override fun enableAudioWhileOnCellular(saveUserChoice: Boolean) =
    stateFragmentPresenter.handleEnableAudio(saveUserChoice)

  override fun disableAudioWhileOnCellular(saveUserChoice: Boolean) =
    stateFragmentPresenter.handleDisableAudio(saveUserChoice)

  fun dummyButtonClicked() = stateFragmentPresenter.handleAudioClick()

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putIntegerArrayList(KEY_SELECTED_INPUT_INDEXES, selectedInputItemIndexes)
  }

  override fun onInputItemSelection(indexList: ArrayList<Int>) {
    selectedInputItemIndexes = indexList
  }
}
