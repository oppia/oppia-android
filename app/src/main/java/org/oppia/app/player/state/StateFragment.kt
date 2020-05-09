package org.oppia.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.app.player.state.listener.NextNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener
import javax.inject.Inject

/** Fragment that represents the current state of an exploration. */
class StateFragment : InjectableFragment(), InteractionAnswerReceiver, InteractionAnswerHandler,
  InteractionAnswerErrorReceiver, ContinueNavigationButtonListener, NextNavigationButtonListener,
  PreviousNavigationButtonListener, ReturnToTopicNavigationButtonListener, SubmitNavigationButtonListener,
  PreviousResponsesHeaderClickListener {
  companion object {
    /**
     * Creates a new instance of a StateFragment.
     * @param internalProfileId used by StateFragment to mark progress.
     * @param topicId used by StateFragment to mark progress.
     * @param storyId used by StateFragment to mark progress.
     * @param explorationId used by StateFragment to mark progress and manage exploration.
     * @return a new instance of [StateFragment].
     */
    fun newInstance(internalProfileId: Int, topicId: String, storyId: String, explorationId: String): StateFragment {
      val stateFragment = StateFragment()
      val args = Bundle()
      args.putInt(STATE_FRAGMENT_PROFILE_ID_ARGUMENT_KEY, internalProfileId)
      args.putString(STATE_FRAGMENT_TOPIC_ID_ARGUMENT_KEY, topicId)
      args.putString(STATE_FRAGMENT_STORY_ID_ARGUMENT_KEY, storyId)
      args.putString(STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY, explorationId)
      stateFragment.arguments = args
      return stateFragment
    }
  }

  @Inject lateinit var stateFragmentPresenter: StateFragmentPresenter

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

  override fun onContinueButtonClicked() = stateFragmentPresenter.onContinueButtonClicked()

  override fun onNextButtonClicked() = stateFragmentPresenter.onNextButtonClicked()

  override fun onPreviousButtonClicked() = stateFragmentPresenter.onPreviousButtonClicked()

  override fun onReturnToTopicButtonClicked() = stateFragmentPresenter.onReturnToTopicButtonClicked()

  override fun onSubmitButtonClicked() = stateFragmentPresenter.onSubmitButtonClicked()

  override fun onResponsesHeaderClicked() = stateFragmentPresenter.onResponsesHeaderClicked()

  fun handlePlayAudio() = stateFragmentPresenter.handleAudioClick()

  fun handleKeyboardAction() = stateFragmentPresenter.handleKeyboardAction()

  override fun onPendingAnswerError(pendingAnswerError: String?) {
    stateFragmentPresenter.updateSubmitButton(pendingAnswerError)
  }

  fun setAudioBarVisibility(visibility: Boolean) = stateFragmentPresenter.setAudioBarVisibility(visibility)

  fun scrollToTop() = stateFragmentPresenter.scrollToTop()
}
