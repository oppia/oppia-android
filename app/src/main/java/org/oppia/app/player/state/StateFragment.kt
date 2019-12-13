package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import javax.inject.Inject

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), InteractionAnswerReceiver {
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

  @Inject
  lateinit var stateFragmentPresenter: StateFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return stateFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onAnswerReadyForSubmission(answer: UserAnswer) {
    stateFragmentPresenter.handleAnswerReadyForSubmission(answer)
  }

  fun handlePlayAudio() = stateFragmentPresenter.handleAudioClick()

  fun handleKeyboardAction() = stateFragmentPresenter.handleKeyboardAction()

  fun setAudioBarVisibility(visibility: Boolean) = stateFragmentPresenter.setAudioBarVisibility(visibility)

  fun scrollToTop() = stateFragmentPresenter.scrollToTop()
}
