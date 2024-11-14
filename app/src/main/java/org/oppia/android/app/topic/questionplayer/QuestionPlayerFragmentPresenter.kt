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
import nl.dionsegijn.konfetti.KonfettiView
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AnsweredQuestionOutcome
import org.oppia.android.app.model.EphemeralQuestion
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.QuestionPlayerFragmentArguments
import org.oppia.android.app.model.State
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.player.state.ConfettiConfig.MINI_CONFETTI_BURST
import org.oppia.android.app.player.state.StatePlayerRecyclerViewAssembler
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.stopplaying.RestartPlayingSessionListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.android.app.utility.FontScaleConfigurationUtil
import org.oppia.android.app.utility.SplitScreenManager
import org.oppia.android.databinding.QuestionPlayerFragmentBinding
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.AnalyticsController
import org.oppia.android.domain.question.QuestionAssessmentProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.extensions.getProto
import org.oppia.android.util.gcsresource.QuestionResourceBucketName
import javax.inject.Inject

/** The presenter for [QuestionPlayerFragment]. */
@FragmentScope
class QuestionPlayerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val questionViewModel: QuestionPlayerViewModel,
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val oppiaLogger: OppiaLogger,
  private val analyticsController: AnalyticsController,
  @QuestionResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory,
  private val splitScreenManager: SplitScreenManager,
  private val fontScaleConfigurationUtil: FontScaleConfigurationUtil
) {
  // TODO(#503): Add tests for the question player.

  private val routeToHintsAndSolutionListener = activity as RouteToHintsAndSolutionListener
  private val hasConversationView = false

  private val ephemeralQuestionLiveData: LiveData<AsyncResult<EphemeralQuestion>> by lazy {
    questionAssessmentProgressController.getCurrentQuestion().toLiveData()
  }

  private lateinit var binding: QuestionPlayerFragmentBinding
  private lateinit var recyclerViewAssembler: StatePlayerRecyclerViewAssembler
  private lateinit var questionId: String
  private lateinit var currentQuestionState: State
  private lateinit var helpIndex: HelpIndex
  private lateinit var profileId: ProfileId

  /** Handles the [Fragment.onAttach] portion of [QuestionPlayerFragment]'s lifecycle. */
  fun handleAttach(context: Context) {
    fontScaleConfigurationUtil.adjustFontScale(context, retrieveArguments().readingTextSize)
  }

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    profileId: ProfileId,
    userAnswerState: UserAnswerState
  ): View? {
    binding = QuestionPlayerFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    this.profileId = profileId

    recyclerViewAssembler = createRecyclerViewAssembler(
      assemblerBuilderFactory.create(resourceBucketName, "skill", profileId, userAnswerState),
      binding.congratulationsTextView,
      binding.congratulationsTextConfettiView
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
        helpIndex
      )
    }
    subscribeToCurrentQuestion()
    return binding.root
  }

  fun revealHint(hintIndex: Int) {
    subscribeToHintSolution(questionAssessmentProgressController.submitHintIsRevealed(hintIndex))
  }

  fun revealSolution() {
    subscribeToHintSolution(questionAssessmentProgressController.submitSolutionIsRevealed())
  }

  private fun retrieveArguments(): QuestionPlayerFragmentArguments {
    return fragment.requireArguments().getProto(
      QuestionPlayerFragment.ARGUMENTS_KEY, QuestionPlayerFragmentArguments.getDefaultInstance()
    )
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

  /** Returns the [UserAnswerState] representing the user's current pending answer. */
  fun getUserAnswerState(): UserAnswerState {
    return questionViewModel.getUserAnswerState(recyclerViewAssembler::getPendingAnswerHandler)
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

  fun onHintAvailable(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean) {
    this.helpIndex = helpIndex
    showHintsAndSolutions(helpIndex, isCurrentStatePendingState)
  }

  private fun subscribeToCurrentQuestion() {
    ephemeralQuestionLiveData.observe(
      fragment,
      Observer {
        processEphemeralQuestionResult(it)
      }
    )
  }

  private fun processEphemeralQuestionResult(result: AsyncResult<EphemeralQuestion>) {
    when (result) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "QuestionPlayerFragment", "Failed to retrieve ephemeral question", result.error
        )
      }
      is AsyncResult.Pending -> {} // Display nothing until a valid result is available.
      is AsyncResult.Success -> processEphemeralQuestion(result.value)
    }
  }

  private fun processEphemeralQuestion(ephemeralQuestion: EphemeralQuestion) {
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
    questionViewModel.updateQuestionProgress(
      currentQuestion = currentQuestionIndex + 1,
      questionCount = questionCount,
      progressPercentage = (((currentQuestionIndex + 1) / questionCount.toDouble()) * 100).toInt(),
      isAtEndOfSession = currentQuestionIndex == questionCount
    )
  }

  private fun updateEndSessionMessage(ephemeralState: EphemeralState) {
    val isStateTerminal =
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    val endSessionViewsVisibility = if (isStateTerminal) View.VISIBLE else View.GONE
    binding.endSessionHeaderTextView.visibility = endSessionViewsVisibility
    binding.endSessionBodyTextView.visibility = endSessionViewsVisibility
  }

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(questionAssessmentProgressController.submitAnswer(answer).toLiveData())
  }

  /** This function listens to and processes the result of submitAnswer from QuestionAssessmentProgressController. */
  private fun subscribeToAnswerOutcome(
    answerOutcomeResultLiveData: LiveData<AsyncResult<AnsweredQuestionOutcome>>
  ) {
    if (questionViewModel.getCanSubmitAnswer().get() == true) {
      recyclerViewAssembler.resetUserAnswerState()
    }
    val answerOutcomeLiveData =
      Transformations.map(answerOutcomeResultLiveData, ::processAnsweredQuestionOutcome)
    answerOutcomeLiveData.observe(
      fragment,
      Observer<AnsweredQuestionOutcome> { result ->
        if (result.isCorrectAnswer) {
          questionViewModel.setHintBulbVisibility(false)
          recyclerViewAssembler.showCelebrationOnCorrectAnswer(result.feedback)
        } else {
          questionViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
        }
      }
    )
  }

  /** Subscribes to the result of requesting to show a hint or solution. */
  private fun subscribeToHintSolution(resultDataProvider: DataProvider<Any?>) {
    resultDataProvider.toLiveData().observe(
      fragment,
      { result ->
        if (result is AsyncResult.Failure) {
          oppiaLogger.e("StateFragment", "Failed to retrieve hint/solution", result.error)
        } else {
          // If the hint/solution, was revealed remove dot and radar.
          questionViewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
      }
    )
  }

  /** Helper for subscribeToAnswerOutcome. */
  private fun processAnsweredQuestionOutcome(
    answeredQuestionOutcomeResult: AsyncResult<AnsweredQuestionOutcome>
  ): AnsweredQuestionOutcome {
    return when (answeredQuestionOutcomeResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "QuestionPlayerFragment",
          "Failed to retrieve answer outcome",
          answeredQuestionOutcomeResult.error
        )
        AnsweredQuestionOutcome.getDefaultInstance()
      }
      is AsyncResult.Pending -> AnsweredQuestionOutcome.getDefaultInstance()
      is AsyncResult.Success -> answeredQuestionOutcomeResult.value
    }
  }

  private fun moveToNextState() {
    questionViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
    questionAssessmentProgressController.moveToNextQuestion()
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager =
      activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
      fragment.requireView().windowToken,
      0 // Flag value to force hide the keyboard when possible.
    )
  }

  private fun createRecyclerViewAssembler(
    builder: StatePlayerRecyclerViewAssembler.Builder,
    congratulationsTextView: TextView,
    congratulationsTextConfettiView: KonfettiView
  ): StatePlayerRecyclerViewAssembler {
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
      .addCelebrationForCorrectAnswers(
        congratulationsTextView,
        congratulationsTextConfettiView,
        MINI_CONFETTI_BURST
      )
      .addConceptCardSupport()
      .build()
  }

  private fun logQuestionPlayerEvent(questionId: String, skillIds: List<String>) {
    analyticsController.logImportantEvent(
      oppiaLogger.createOpenQuestionPlayerContext(questionId, skillIds),
      profileId
    )
  }

  private fun showHintsAndSolutions(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean) {
    if (!isCurrentStatePendingState) {
      // If current question state is not the pending top question state, hide the hint bulb.
      questionViewModel.setHintOpenedAndUnRevealedVisibility(false)
      questionViewModel.setHintBulbVisibility(false)
    } else {
      when (helpIndex.indexTypeCase) {
        HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX -> {
          questionViewModel.setHintBulbVisibility(true)
          questionViewModel.setHintOpenedAndUnRevealedVisibility(true)
        }
        HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX -> {
          questionViewModel.setHintBulbVisibility(true)
          questionViewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
        HelpIndex.IndexTypeCase.SHOW_SOLUTION -> {
          questionViewModel.setHintBulbVisibility(true)
          questionViewModel.setHintOpenedAndUnRevealedVisibility(true)
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
  }
}
