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
import org.oppia.android.app.model.QuestionPlayerFragmentArguments
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
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
    questionPlayerFragmentPresenter.handleAttach(context)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val args = checkNotNull(arguments) {
      "Expected arguments to be passed to QuestionPlayerFragment"
    }
    val userAnswerState = savedInstanceState?.getProto(
      QUESTION_PLAYER_FRAGMENT_STATE_KEY,
      UserAnswerState.getDefaultInstance()
    ) ?: UserAnswerState.getDefaultInstance()

    val arguments =
      args.getProto(ARGUMENTS_KEY, QuestionPlayerFragmentArguments.getDefaultInstance())
    val profileId = arguments.profileId
    return questionPlayerFragmentPresenter.handleCreateView(
      inflater, container, profileId, userAnswerState
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

  fun handleKeyboardAction() = questionPlayerFragmentPresenter.handleKeyboardAction()

  fun revealHint(hintIndex: Int) {
    questionPlayerFragmentPresenter.revealHint(hintIndex)
  }

  fun revealSolution() {
    questionPlayerFragmentPresenter.revealSolution()
  }

  fun dismissConceptCard() = questionPlayerFragmentPresenter.dismissConceptCard()

  companion object {

    /** Arguments key for [QuestionPlayerFragment]. */
    const val ARGUMENTS_KEY = "QuestionPlayerFragment.arguments"

    /** Arguments key for QuestionPlayerFragment saved state. */
    const val QUESTION_PLAYER_FRAGMENT_STATE_KEY = "QuestionPlayerFragment.state"

    /**
     * Creates a new fragment to play a question session.
     *
     * @param profileId the profile in which the question play session will be played
     * @return a new [QuestionPlayerFragment] to start a question play session
     */
    fun newInstance(profileId: ProfileId, readingTextSize: ReadingTextSize):
      QuestionPlayerFragment {
        val args = QuestionPlayerFragmentArguments.newBuilder().apply {
          this.profileId = profileId
          this.readingTextSize = readingTextSize
        }.build()
        return QuestionPlayerFragment().apply {
          arguments = Bundle().apply {
            putProto(ARGUMENTS_KEY, args)
          }
        }
      }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putProto(
      QUESTION_PLAYER_FRAGMENT_STATE_KEY,
      questionPlayerFragmentPresenter.getUserAnswerState()
    )
  }
}
