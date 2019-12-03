package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.audio.CellularDataInterface
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.listener.AudioContentIdListener
import org.oppia.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.app.player.state.listener.NextNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), CellularDataInterface, InteractionAnswerReceiver,
  ContinueNavigationButtonListener, NextNavigationButtonListener, PreviousNavigationButtonListener,
  ReturnToTopicNavigationButtonListener, SubmitNavigationButtonListener, PreviousResponsesHeaderClickListener, HtmlParser.CustomOppiaTagActionListener,
  AudioContentIdListener {

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

  override fun enableAudioWhileOnCellular(saveUserChoice: Boolean) {
    stateFragmentPresenter.handleEnableAudio(saveUserChoice)
  }

  override fun disableAudioWhileOnCellular(saveUserChoice: Boolean) {
    stateFragmentPresenter.handleDisableAudio(saveUserChoice)
  }

  override fun onAnswerReadyForSubmission(answer: UserAnswer) {
    stateFragmentPresenter.handleAnswerReadyForSubmission(answer)
  }

  override fun onContinueButtonClicked() = stateFragmentPresenter.onContinueButtonClicked()

  override fun onNextButtonClicked() = stateFragmentPresenter.onNextButtonClicked()

  override fun onPreviousButtonClicked() = stateFragmentPresenter.onPreviousButtonClicked()

  override fun onReturnToTopicButtonClicked() = stateFragmentPresenter.onReturnToTopicButtonClicked()

  override fun onSubmitButtonClicked() = stateFragmentPresenter.onSubmitButtonClicked()

  override fun onResponsesHeaderClicked() = stateFragmentPresenter.onResponsesHeaderClicked()

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    stateFragmentPresenter.onConceptCardLinkClicked(view, skillId)
  }

  override fun contentIdForCurrentAudio(contentId: String, isPlaying: Boolean) {
    stateFragmentPresenter.handleContentCardHighlighting(contentId, isPlaying)
  }

  fun handlePlayAudio() = stateFragmentPresenter.handleAudioClick()

  fun handleKeyboardAction() = stateFragmentPresenter.handleKeyboardAction()

  fun dismissConceptCard() = stateFragmentPresenter.dismissConceptCard()
}
