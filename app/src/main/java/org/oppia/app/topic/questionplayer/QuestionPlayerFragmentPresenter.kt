package org.oppia.app.topic.questionplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.QuestionPlayerFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.EventLog
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.StatePlayerRecyclerViewAssembler
import org.oppia.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.analytics.AnalyticsController
import org.oppia.domain.question.QuestionAssessmentProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.QuestionResourceBucketName
import org.oppia.util.logging.Logger
import org.oppia.util.system.OppiaClock
import javax.inject.Inject

/** The presenter for [QuestionPlayerFragment]. */
@FragmentScope
class QuestionPlayerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<QuestionPlayerViewModel>,
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val analyticsController: AnalyticsController,
  private val oppiaClock: OppiaClock,
  private val logger: Logger,
  @QuestionResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory
) {
  // TODO(#503): Add tests for the question player.

  private val questionViewModel by lazy { getQuestionPlayerViewModel() }
  private val ephemeralQuestionLiveData: LiveData<AsyncResult<EphemeralQuestion>> by lazy {
    questionAssessmentProgressController.getCurrentQuestion()
  }
  private lateinit var binding: QuestionPlayerFragmentBinding
  private lateinit var recyclerViewAssembler: StatePlayerRecyclerViewAssembler

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    binding = QuestionPlayerFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )

    recyclerViewAssembler = createRecyclerViewAssembler(
      assemblerBuilderFactory.create(resourceBucketName, "skill"),
      binding.congratulationsTextView
    )

    binding.apply {
      lifecycleOwner = fragment
      viewModel = questionViewModel
    }
    binding.questionRecyclerView.apply {
      adapter = recyclerViewAssembler.adapter
    }
    subscribeToCurrentQuestion()
    return binding.root
  }

  fun handleAnswerReadyForSubmission(answer: UserAnswer) {
    // An interaction has indicated that an answer is ready for submission.
    handleSubmitAnswer(answer)
  }

  fun onContinueButtonClicked() {
    hideKeyboard()
    moveToNextState()
  }

  fun onNextButtonClicked() = moveToNextState()

  fun onReplayButtonClicked() {
    hideKeyboard()
    (activity as RestartPlayingSessionListener).restartSession()
  }

  fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    (activity as StopStatePlayingSessionListener).stopSession()
  }

  fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(questionViewModel.getPendingAnswer(recyclerViewAssembler))
  }

  fun onResponsesHeaderClicked() {
    recyclerViewAssembler.togglePreviousAnswers(questionViewModel.itemList)
    recyclerViewAssembler.adapter.notifyDataSetChanged()
  }

  /**
   * Updates whether the submit button should be active based on whether the pending answer is in an
   * error state.
   */
  fun updateSubmitButton(pendingAnswerError: String?) {
    questionViewModel.setCanSubmitAnswer(pendingAnswerError == null)
  }

  fun handleKeyboardAction() = onSubmitButtonClicked()

  private fun subscribeToCurrentQuestion() {
    ephemeralQuestionLiveData.observe(
      fragment,
      Observer {
        processEphemeralQuestionResult(it)
      }
    )
  }

  private fun processEphemeralQuestionResult(result: AsyncResult<EphemeralQuestion>) {
    if (result.isFailure()) {
      logger.e(
        "QuestionPlayerFragment",
        "Failed to retrieve ephemeral question",
        result.getErrorOrNull()!!
      )
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }
    val ephemeralQuestion = result.getOrThrow()
    // TODO(#497): Update this to properly link to question assets.
    val skillId = ephemeralQuestion.question.linkedSkillIdsList.firstOrNull() ?: ""
    updateProgress(ephemeralQuestion.currentQuestionIndex, ephemeralQuestion.totalQuestionCount)
    logQuestionPlayerEvent(
      ephemeralQuestion.question.questionId,
      ephemeralQuestion.question.linkedSkillIdsList
    )
    updateEndSessionMessage(ephemeralQuestion.ephemeralState)
    questionViewModel.itemList.clear()
    questionViewModel.itemList += recyclerViewAssembler.compute(
      ephemeralQuestion.ephemeralState,
      skillId
    )
  }

  private fun updateProgress(currentQuestionIndex: Int, questionCount: Int) {
    questionViewModel.currentQuestion.set(currentQuestionIndex + 1)
    questionViewModel.questionCount.set(questionCount)
    questionViewModel.progressPercentage.set(
      (((currentQuestionIndex + 1) / questionCount.toDouble()) * 100).toInt()
    )
    questionViewModel.isAtEndOfSession.set(currentQuestionIndex == questionCount)
  }

  private fun updateEndSessionMessage(ephemeralState: EphemeralState) {
    val isStateTerminal =
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    val endSessionViewsVisibility = if (isStateTerminal) View.VISIBLE else View.GONE
    binding.endSessionHeaderTextView.visibility = endSessionViewsVisibility
    binding.endSessionBodyTextView.visibility = endSessionViewsVisibility
  }

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(questionAssessmentProgressController.submitAnswer(answer))
  }

  /** This function listens to and processes the result of submitAnswer from QuestionAssessmentProgressController. */
  private fun subscribeToAnswerOutcome(
    answerOutcomeResultLiveData: LiveData<AsyncResult<AnsweredQuestionOutcome>>
  ) {
    val answerOutcomeLiveData =
      Transformations.map(answerOutcomeResultLiveData, ::processAnsweredQuestionOutcome)
    answerOutcomeLiveData.observe(
      fragment,
      Observer<AnsweredQuestionOutcome> { result ->
        recyclerViewAssembler.isCorrectAnswer.set(result.isCorrectAnswer)
        if (result.isCorrectAnswer) {
          recyclerViewAssembler.showCongratulationMessageOnCorrectAnswer()
        }
      }
    )
  }

  /** Helper for subscribeToAnswerOutcome. */
  private fun processAnsweredQuestionOutcome(
    answeredQuestionOutcomeResult: AsyncResult<AnsweredQuestionOutcome>
  ): AnsweredQuestionOutcome {
    if (answeredQuestionOutcomeResult.isFailure()) {
      logger.e(
        "StateFragment",
        "Failed to retrieve answer outcome",
        answeredQuestionOutcomeResult.getErrorOrNull()!!
      )
    }
    return answeredQuestionOutcomeResult.getOrDefault(AnsweredQuestionOutcome.getDefaultInstance())
  }

  private fun moveToNextState() {
    questionAssessmentProgressController.moveToNextQuestion()
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager =
      activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
      fragment.view!!.windowToken,
      InputMethodManager.SHOW_FORCED
    )
  }

  private fun createRecyclerViewAssembler(
    builder: StatePlayerRecyclerViewAssembler.Builder,
    congratulationsTextView: TextView
  ): StatePlayerRecyclerViewAssembler {
    // TODO(#501): Add support early exit detection & message, which requires changes in the training progress
    //  controller & possibly the ephemeral question data model.
    // TODO(#502): Add support for surfacing skills that need to be reviewed by the learner.
    return builder.addContentSupport()
      .addFeedbackSupport()
      .addInteractionSupport(questionViewModel.getCanSubmitAnswer())
      .addPastAnswersSupport()
      .addWrongAnswerCollapsingSupport()
      .addForwardNavigationSupport()
      .addReplayButtonSupport()
      .addReturnToTopicSupport()
      .addCongratulationsForCorrectAnswers(congratulationsTextView)
      .build()
  }

  private fun getQuestionPlayerViewModel(): QuestionPlayerViewModel {
    return viewModelProvider.getForFragment(fragment, QuestionPlayerViewModel::class.java)
  }

  private fun logQuestionPlayerEvent(questionId: String, skillIds: List<String>) {
    analyticsController.logTransitionEvent(
      activity.applicationContext,
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_QUESTION_PLAYER,
      analyticsController.createQuestionContext(
        questionId,
        skillIds
      )
    )
  }
}
