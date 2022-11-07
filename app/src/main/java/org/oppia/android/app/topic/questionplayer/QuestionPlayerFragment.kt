package org.oppia.android.app.topic.questionplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.FragmentComponentImpl
import org.oppia.android.app.fragment.InjectableFragment
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.RawUserAnswer
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.android.app.player.state.listener.NextNavigationButtonListener
import org.oppia.android.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.android.app.player.state.listener.ReplayButtonListener
import org.oppia.android.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.android.app.player.state.listener.ShowHintAvailabilityListener
import org.oppia.android.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.extensions.putProto
import javax.inject.Inject

private const val QUESTION_PLAYER_FRAGMENT_RAW_USER_ANSWER_KEY =
  "QuestionPlayerFragment.raw_user_answer"
private const val QUESTION_PLAYER_FRAGMENT_IS_PREVIOUS_RESPONSES_HEADER_EXPANDED_KEY =
  "QuestionPlayerFragment.is_previous_responses_header_expanded"

/** Fragment that contains all questions in Question Player. */
class QuestionPlayerFragment :
  InjectableFragment(),
  InteractionAnswerReceiver,
  InteractionAnswerErrorOrAvailabilityCheckReceiver,
  ContinueNavigationButtonListener,
  NextNavigationButtonListener,
  ReplayButtonListener,
  ReturnToTopicNavigationButtonListener,
  SubmitNavigationButtonListener,
  PreviousResponsesHeaderClickListener,
  ShowHintAvailabilityListener {

  @Inject
  lateinit var questionPlayerFragmentPresenter: QuestionPlayerFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    (fragmentComponent as FragmentComponentImpl).inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to QuestionPlayerFragment"
    }
    val rawUserAnswer = savedInstanceState?.getProto(
      QUESTION_PLAYER_FRAGMENT_RAW_USER_ANSWER_KEY, RawUserAnswer.getDefaultInstance()
    ) ?: RawUserAnswer.getDefaultInstance()
    val isPreviousResponsesExpanded =
      savedInstanceState?.getBoolean(QUESTION_PLAYER_FRAGMENT_IS_PREVIOUS_RESPONSES_HEADER_EXPANDED_KEY)
        ?: false
    val profileId = args.getProto(PROFILE_ID_ARGUMENT_KEY, ProfileId.getDefaultInstance())
    return questionPlayerFragmentPresenter.handleCreateView(
      inflater, container, rawUserAnswer, isPreviousResponsesExpanded, profileId
    )
  }

  override fun onAnswerReadyForSubmission(answer: UserAnswer) {
    questionPlayerFragmentPresenter.handleAnswerReadyForSubmission(answer)
  }

  override fun onContinueButtonClicked() = questionPlayerFragmentPresenter.onContinueButtonClicked()

  override fun onNextButtonClicked() = questionPlayerFragmentPresenter.onNextButtonClicked()

  override fun onReplayButtonClicked() = questionPlayerFragmentPresenter.onReplayButtonClicked()

  override fun onReturnToTopicButtonClicked() =
    questionPlayerFragmentPresenter.onReturnToTopicButtonClicked()

  override fun onSubmitButtonClicked() = questionPlayerFragmentPresenter.onSubmitButtonClicked()

  override fun onResponsesHeaderClicked() =
    questionPlayerFragmentPresenter.onResponsesHeaderClicked()

  override fun onPendingAnswerErrorOrAvailabilityCheck(
    pendingAnswerError: String?,
    inputAnswerAvailable: Boolean
  ) =
    questionPlayerFragmentPresenter.updateSubmitButton(pendingAnswerError, inputAnswerAvailable)

  override fun onHintAvailable(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean) =
    questionPlayerFragmentPresenter.onHintAvailable(helpIndex, isCurrentStatePendingState)

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putProto(
      QUESTION_PLAYER_FRAGMENT_RAW_USER_ANSWER_KEY,
      questionPlayerFragmentPresenter.getRawUserAnswer()
    )
    outState.putBoolean(
      QUESTION_PLAYER_FRAGMENT_IS_PREVIOUS_RESPONSES_HEADER_EXPANDED_KEY,
      questionPlayerFragmentPresenter.getIsPreviousResponsesExpanded()
    )
  }

  fun handleKeyboardAction() = questionPlayerFragmentPresenter.handleKeyboardAction()

  fun revealHint(hintIndex: Int) {
    questionPlayerFragmentPresenter.revealHint(hintIndex)
  }

  fun revealSolution() {
    questionPlayerFragmentPresenter.revealSolution()
  }

  fun dismissConceptCard() = questionPlayerFragmentPresenter.dismissConceptCard()

  companion object {
    private const val PROFILE_ID_ARGUMENT_KEY = "QuestionPlayerFragment.profile_id"

    /**
     * Creates a new fragment to play a question session.
     *
     * @param profileId the profile in which the question play session will be played
     * @return a new [QuestionPlayerFragment] to start a question play session
     */
    fun newInstance(profileId: ProfileId): QuestionPlayerFragment {
      return QuestionPlayerFragment().apply {
        arguments = Bundle().apply {
          putProto(PROFILE_ID_ARGUMENT_KEY, profileId)
        }
      }
    }
  }
}
