package org.oppia.android.app.player.state

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.StateFragmentArguments
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.android.app.player.state.listener.NextNavigationButtonListener
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.android.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.android.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.android.app.player.state.listener.ShowHintAvailabilityListener
import org.oppia.android.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

/** Fragment that represents the current state of an exploration. */
class StateFragment :
  InjectableFragment(),
  InteractionAnswerReceiver,
  InteractionAnswerHandler,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  ContinueNavigationButtonListener,
  NextNavigationButtonListener,
  PreviousNavigationButtonListener,
  ReturnToTopicNavigationButtonListener,
  SubmitNavigationButtonListener,
  PreviousResponsesHeaderClickListener,
  ShowHintAvailabilityListener {
  companion object {

    /** Arguments key for StateFragment. */
    const val STATE_FRAGMENT_ARGUMENTS_KEY = "StateFragment.arguments"

    /** Arguments key for StateFragment saved state. */
    const val STATE_FRAGMENT_STATE_KEY = "StateFragment.state"

    /**
     * Creates a new instance of a StateFragment.
     * @param internalProfileId used by StateFragment to mark progress.
     * @param topicId used by StateFragment to mark progress.
     * @param storyId used by StateFragment to mark progress.
     * @param explorationId used by StateFragment to mark progress and manage exploration.
     * @return a new instance of [StateFragment].
     */
    fun newInstance(
      internalProfileId: Int,
      topicId: String,
      storyId: String,
      explorationId: String
    ): StateFragment {

      val args = StateFragmentArguments.newBuilder().apply {
        this.internalProfileId = internalProfileId
        this.topicId = topicId
        this.storyId = storyId
        this.explorationId = explorationId
      }.build()
      return StateFragment().apply {
        arguments = Bundle().apply {
          putProto(STATE_FRAGMENT_ARGUMENTS_KEY, args)
        }
      }
    }
  }

  @Inject
  lateinit var stateFragmentPresenter: StateFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args =
      arguments?.getProto(STATE_FRAGMENT_ARGUMENTS_KEY, StateFragmentArguments.getDefaultInstance())

    val userAnswerState = savedInstanceState?.getProto(
      STATE_FRAGMENT_STATE_KEY,
      UserAnswerState.getDefaultInstance()
    ) ?: UserAnswerState.getDefaultInstance()

    val internalProfileId = args?.internalProfileId ?: -1
    val topicId = args?.topicId!!
    val storyId = args.storyId!!
    val explorationId =
      args.explorationId!!
    return stateFragmentPresenter.handleCreateView(
      inflater,
      container,
      internalProfileId,
      topicId,
      storyId,
      explorationId,
      userAnswerState
    )
  }

  override fun onAnswerReadyForSubmission(answer: UserAnswer) {
    stateFragmentPresenter.handleAnswerReadyForSubmission(answer)
  }

  override fun onContinueButtonClicked() = stateFragmentPresenter.onContinueButtonClicked()

  override fun onNextButtonClicked() = stateFragmentPresenter.onNextButtonClicked()

  override fun onPreviousButtonClicked() = stateFragmentPresenter.onPreviousButtonClicked()

  override fun onReturnToTopicButtonClicked() =
    stateFragmentPresenter.onReturnToTopicButtonClicked()

  override fun onSubmitButtonClicked() = stateFragmentPresenter.onSubmitButtonClicked()

  override fun onResponsesHeaderClicked() = stateFragmentPresenter.onResponsesHeaderClicked()

  override fun onHintAvailable(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean) =
    stateFragmentPresenter.onHintAvailable(helpIndex, isCurrentStatePendingState)

  fun handlePlayAudio() = stateFragmentPresenter.handleAudioClick()

  fun handleKeyboardAction() = stateFragmentPresenter.handleKeyboardAction()

  override fun onPendingAnswerErrorOrAvailabilityCheck(
    pendingAnswerError: String?,
    inputAnswerAvailable: Boolean
  ) {
    stateFragmentPresenter.updateSubmitButton(pendingAnswerError, inputAnswerAvailable)
  }

  fun setAudioBarVisibility(visibility: Boolean) =
    stateFragmentPresenter.setAudioBarVisibility(visibility)

  fun scrollToTop() = stateFragmentPresenter.scrollToTop()

  fun revealHint(hintIndex: Int) {
    stateFragmentPresenter.revealHint(hintIndex)
  }

  fun viewHint(hintIndex: Int) {
    stateFragmentPresenter.viewHint(hintIndex)
  }

  fun revealSolution() = stateFragmentPresenter.revealSolution()

  fun viewSolution() {
    stateFragmentPresenter.viewSolution()
  }

  fun dismissConceptCard() = stateFragmentPresenter.dismissConceptCard()

  fun getExplorationCheckpointState() = stateFragmentPresenter.getExplorationCheckpointState()

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putProto(
      STATE_FRAGMENT_STATE_KEY,
      stateFragmentPresenter.getUserAnswerState()
    )
  }
}
