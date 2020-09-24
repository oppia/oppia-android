package org.oppia.android.app.topic.questionplayer

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
<<<<<<< HEAD:app/src/main/java/org/oppia/android/app/topic/questionplayer/QuestionPlayerFragmentPresenter.kt
import org.oppia.android.databinding.QuestionPlayerFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AnsweredQuestionOutcome
import org.oppia.android.app.model.EphemeralQuestion
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.EventLog
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Hint
import org.oppia.android.app.model.Solution
import org.oppia.android.app.model.State
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.state.CONCEPT_CARD_DIALOG_FRAGMENT_TAG
import org.oppia.android.app.player.state.StatePlayerRecyclerViewAssembler
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.android.app.utility.SplitScreenManager
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.question.QuestionAssessmentProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.gcsresource.QuestionResourceBucketName
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.system.OppiaClock
=======
import org.oppia.app.databinding.QuestionPlayerFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.EventLog
import org.oppia.app.model.HelpIndex
import org.oppia.app.model.Hint
import org.oppia.app.model.Solution
import org.oppia.app.model.State
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.CONCEPT_CARD_DIALOG_FRAGMENT_TAG
import org.oppia.app.player.state.StatePlayerRecyclerViewAssembler
import org.oppia.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.app.utility.SplitScreenManager
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.oppialogger.OppiaLogger
import org.oppia.domain.question.QuestionAssessmentProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.data.DataProviders.Companion.toLiveData
import org.oppia.util.gcsresource.QuestionResourceBucketName
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.system.OppiaClock
>>>>>>> develop:app/src/main/java/org/oppia/app/topic/questionplayer/QuestionPlayerFragmentPresenter.kt
import javax.inject.Inject

/** The presenter for [QuestionPlayerFragment]. */
@FragmentScope
class QuestionPlayerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<QuestionPlayerViewModel>,
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val oppiaLogger: OppiaLogger,
  private val oppiaClock: OppiaClock,
  private val logger: ConsoleLogger,
  @QuestionResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory,
  private val splitScreenManager: SplitScreenManager
) {
  // TODO(#503): Add tests for the question player.

  private val routeToHintsAndSolutionListener = activity as RouteToHintsAndSolutionListener
  private val hasConversationView = false

  private val questionViewModel by lazy { getQuestionPlayerViewModel() }
  private val ephemeralQuestionLiveData: LiveData<AsyncResult<EphemeralQuestion>> by lazy {
    questionAssessmentProgressController.getCurrentQuestion().toLiveData()
  }
  private lateinit var binding: QuestionPlayerFragmentBinding
  private lateinit var recyclerViewAssembler: StatePlayerRecyclerViewAssembler
  private lateinit var questionId: String
  private lateinit var currentQuestionState: State

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
    binding.extraInteractionRecyclerView.apply {
      adapter = recyclerViewAssembler.rhsAdapter
    }

    binding.hintsAndSolutionFragmentContainer.setOnClickListener {
      routeToHintsAndSolutionListener.routeToHintsAndSolution(
        questionId,
        questionViewModel.newAvailableHintIndex,
        questionViewModel.allHintsExhausted
      )
    }
    subscribeToCurrentQuestion()
    return binding.root
  }

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    subscribeToHint(
      questionAssessmentProgressController.submitHintIsRevealed(
        currentQuestionState,
        saveUserChoice,
        hintIndex
      )
    )
  }

  fun revealSolution() {
    subscribeToSolution(
      questionAssessmentProgressController.submitSolutionIsRevealed(currentQuestionState)
    )
  }

  fun dismissConceptCard() {
    fragment.childFragmentManager.findFragmentByTag(
      CONCEPT_CARD_DIALOG_FRAGMENT_TAG
    )?.let { dialogFragment ->
      fragment.childFragmentManager.beginTransaction().remove(dialogFragment).commitNow()
    }
  }

  fun onHintAvailable(helpIndex: HelpIndex) {
    when (helpIndex.indexTypeCase) {
      HelpIndex.IndexTypeCase.HINT_INDEX, HelpIndex.IndexTypeCase.SHOW_SOLUTION -> {
        if (helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.HINT_INDEX) {
          questionViewModel.newAvailableHintIndex = helpIndex.hintIndex
        }
        questionViewModel.allHintsExhausted =
          helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.SHOW_SOLUTION
        questionViewModel.setHintOpenedAndUnRevealedVisibility(true)
        questionViewModel.setHintBulbVisibility(true)
      }
      HelpIndex.IndexTypeCase.EVERYTHING_REVEALED -> {
        questionViewModel.setHintOpenedAndUnRevealedVisibility(false)
        questionViewModel.setHintBulbVisibility(true)
      }
      else -> {
        questionViewModel.setHintOpenedAndUnRevealedVisibility(false)
        questionViewModel.setHintBulbVisibility(false)
      }
    }
  }

  fun handleAnswerReadyForSubmission(answer: UserAnswer) {
    // An interaction has indicated that an answer is ready for submission.
    handleSubmitAnswer(answer)
  }

  fun onContinueButtonClicked() {
    questionViewModel.setHintBulbVisibility(false)
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
    handleSubmitAnswer(
      questionViewModel.getPendingAnswer(
        recyclerViewAssembler::getPendingAnswerHandler
      )
    )
  }

  fun onResponsesHeaderClicked() {
    recyclerViewAssembler.togglePreviousAnswers(questionViewModel.itemList)
    recyclerViewAssembler.adapter.notifyDataSetChanged()
  }

  /**
   * Updates whether the submit button should be active based on whether the pending answer is in an
   * error state.
   */
  fun updateSubmitButton(pendingAnswerError: String?, inputAnswerAvailable: Boolean) {
    if (inputAnswerAvailable) {
      questionViewModel.setCanSubmitAnswer(pendingAnswerError == null)
    } else {
      questionViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
    }
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

    questionId = ephemeralQuestion.question.questionId

    updateProgress(ephemeralQuestion.currentQuestionIndex, ephemeralQuestion.totalQuestionCount)
    logQuestionPlayerEvent(
      ephemeralQuestion.question.questionId,
      ephemeralQuestion.question.linkedSkillIdsList
    )
    updateEndSessionMessage(ephemeralQuestion.ephemeralState)

    currentQuestionState = ephemeralQuestion.ephemeralState.state

    val isSplitView =
      splitScreenManager.shouldSplitScreen(ephemeralQuestion.ephemeralState.state.interaction.id)

    if (isSplitView) {
      questionViewModel.isSplitView.set(true)
      questionViewModel.centerGuidelinePercentage.set(0.5f)
    } else {
      questionViewModel.isSplitView.set(false)
      questionViewModel.centerGuidelinePercentage.set(1f)
    }

    val dataPair = recyclerViewAssembler.compute(
      ephemeralQuestion.ephemeralState,
      skillId,
      isSplitView
    )

    questionViewModel.itemList.clear()
    questionViewModel.itemList += dataPair.first
    questionViewModel.rightItemList.clear()
    questionViewModel.rightItemList += dataPair.second
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
          recyclerViewAssembler.stopHintsFromShowing()
          questionViewModel.setHintBulbVisibility(false)
          recyclerViewAssembler.showCongratulationMessageOnCorrectAnswer()
        } else {
          questionViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
        }
      }
    )
  }

  /**
   * This function listens to the result of RevealHint.
   * Whenever a hint is revealed using QuestionAssessmentProgressController.submitHintIsRevealed function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToHint(hintResultLiveData: LiveData<AsyncResult<Hint>>) {
    val hintLiveData = getHintIsRevealed(hintResultLiveData)
    hintLiveData.observe(
      fragment,
      Observer { result ->
        // If the hint was revealed remove dot and radar.
        if (result.hintIsRevealed) {
          questionViewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
      }
    )
  }

  /**
   * This function listens to the result of RevealSolution.
   * Whenever a hint is revealed using QuestionAssessmentProgressController.submitHintIsRevealed function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToSolution(solutionResultLiveData: LiveData<AsyncResult<Solution>>) {
    val solutionLiveData = getSolutionIsRevealed(solutionResultLiveData)
    solutionLiveData.observe(
      fragment,
      Observer { result ->
        // If the hint was revealed remove dot and radar.
        if (result.solutionIsRevealed) {
          questionViewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
      }
    )
  }

  /** Helper for [subscribeToSolution]. */
  private fun getSolutionIsRevealed(hint: LiveData<AsyncResult<Solution>>): LiveData<Solution> {
    return Transformations.map(hint, ::processSolution)
  }

  /** Helper for [subscribeToHint]. */
  private fun getHintIsRevealed(hint: LiveData<AsyncResult<Hint>>): LiveData<Hint> {
    return Transformations.map(hint, ::processHint)
  }

  /** Helper for [subscribeToHint]. */
  private fun processHint(hintResult: AsyncResult<Hint>): Hint {
    if (hintResult.isFailure()) {
      logger.e(
        "QuestionPlayerFragment",
        "Failed to retrieve Hint",
        hintResult.getErrorOrNull()!!
      )
    }
    return hintResult.getOrDefault(Hint.getDefaultInstance())
  }

  /** Helper for [subscribeToSolution]. */
  private fun processSolution(solutionResult: AsyncResult<Solution>): Solution {
    if (solutionResult.isFailure()) {
      logger.e(
        "QuestionPlayerFragment",
        "Failed to retrieve Solution",
        solutionResult.getErrorOrNull()!!
      )
    }
    return solutionResult.getOrDefault(Solution.getDefaultInstance())
  }

  /** Helper for subscribeToAnswerOutcome. */
  private fun processAnsweredQuestionOutcome(
    answeredQuestionOutcomeResult: AsyncResult<AnsweredQuestionOutcome>
  ): AnsweredQuestionOutcome {
    if (answeredQuestionOutcomeResult.isFailure()) {
      logger.e(
        "QuestionPlayerFragment",
        "Failed to retrieve answer outcome",
        answeredQuestionOutcomeResult.getErrorOrNull()!!
      )
    }
    return answeredQuestionOutcomeResult.getOrDefault(AnsweredQuestionOutcome.getDefaultInstance())
  }

  private fun moveToNextState() {
    questionViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
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
    return builder
      .hasConversationView(hasConversationView)
      .addContentSupport()
      .addFeedbackSupport()
      .addInteractionSupport(questionViewModel.getCanSubmitAnswer())
      .addPastAnswersSupport()
      .addWrongAnswerCollapsingSupport()
      .addForwardNavigationSupport()
      .addReplayButtonSupport()
      .addReturnToTopicSupport()
      .addHintsAndSolutionsSupport()
      .addCongratulationsForCorrectAnswers(congratulationsTextView)
      .addConceptCardSupport()
      .build()
  }

  private fun getQuestionPlayerViewModel(): QuestionPlayerViewModel {
    return viewModelProvider.getForFragment(fragment, QuestionPlayerViewModel::class.java)
  }

  private fun logQuestionPlayerEvent(questionId: String, skillIds: List<String>) {
    oppiaLogger.logTransitionEvent(
      oppiaClock.getCurrentCalendar().timeInMillis,
      EventLog.EventAction.OPEN_QUESTION_PLAYER,
      oppiaLogger.createQuestionContext(
        questionId,
        skillIds
      )
    )
  }
}
