package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.player.audio.CellularDataInterface
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import javax.inject.Inject

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), CellularDataInterface {

  companion object {
    /**
     * Creates a new instance of a StateFragment
     * @param topicId
     * @return [StateFragment]: Fragment
     */
    fun newInstance(topicId: String): StateFragment {
      val stateFragment = StateFragment()
      val args = Bundle()
      args.putString(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY, topicId)
      stateFragment.arguments = args
      return stateFragment
    }
  }

  @Inject lateinit var stateFragmentPresenter: StateFragmentPresenter

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = stateFragmentPresenter.handleCreateView(inflater, container)
  override fun enableAudioWhileOnCellular(saveUserChoice: Boolean) = stateFragmentPresenter.handleEnableAudio(saveUserChoice)
  override fun disableAudioWhileOnCellular(saveUserChoice: Boolean) = stateFragmentPresenter.handleDisableAudio(saveUserChoice)
  fun dummyButtonClicked() = stateFragmentPresenter.handleAudioClick()
}
