package org.oppia.app.topic.questionplayer

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.app.fragment.InjectableFragment
import org.oppia.app.model.HelpIndex
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.app.player.state.listener.NextNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.app.player.state.listener.ReplayButtonListener
import org.oppia.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.app.player.state.listener.ShowHintAvailabilityListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener
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
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return questionPlayerFragmentPresenter.handleCreateView(inflater, container)
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

  fun handleKeyboardAction() = questionPlayerFragmentPresenter.handleKeyboardAction()

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    questionPlayerFragmentPresenter.revealHint(saveUserChoice, hintIndex)
  }

  fun revealSolution(saveUserChoice: Boolean) {
    questionPlayerFragmentPresenter.revealSolution(saveUserChoice)
  }

  override fun onHintAvailable(helpIndex: HelpIndex) =
    questionPlayerFragmentPresenter.onHintAvailable(helpIndex)

  override fun hintVisibilityBetweenState(hintVisibility: Boolean) =
    questionPlayerFragmentPresenter.hintVisibilityBetweenState(hintVisibility)
}
