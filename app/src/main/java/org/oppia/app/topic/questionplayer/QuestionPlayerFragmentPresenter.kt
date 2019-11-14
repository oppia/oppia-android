package org.oppia.app.topic.questionplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.QuestionButtonItemBinding
import org.oppia.app.databinding.QuestionPlayerFragmentBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.AnsweredQuestionOutcome
import org.oppia.app.model.EphemeralQuestion
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelFactory
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.topic.questionplayer.QuestionNavigationButtonViewModel.ContinuationNavigationButtonType
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.question.QuestionAssessmentProgressController
import org.oppia.domain.question.QuestionTrainingController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** The presenter for [QuestionPlayerFragment]. */
@FragmentScope
class QuestionPlayerFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val context: Context,
  private val viewModelProvider: ViewModelProvider<QuestionPlayerViewModel>,
  private val htmlParserFactory: HtmlParser.Factory,
  private val questionTrainingController: QuestionTrainingController,
  private val questionAssessmentProgressController: QuestionAssessmentProgressController,
  private val logger: Logger,
  private val interactionViewModelFactoryMap: Map<String, @JvmSuppressWildcards InteractionViewModelFactory>
) : QuestionNavigationButtonListener {
  private val questionViewModel by lazy {
    getQuestionPlayerViewModel()
  }
  private val ephermeralQuestionLiveData: LiveData<AsyncResult<EphemeralQuestion>> by lazy {
    questionAssessmentProgressController.getCurrentQuestion()
  }
  private var firstTry = true
  private var numCorrect = 0

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val binding = QuestionPlayerFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.apply {
      lifecycleOwner = fragment
      viewModel = questionViewModel
    }
    binding.questionRecyclerView.apply {
      adapter = createRecyclerViewAdapter()
    }
    subscribeToCurrentQuestion()
    return binding.root
  }

  private fun subscribeToCurrentQuestion() {
    ephermeralQuestionLiveData.observe(fragment, Observer {
      processEphemeralQuestionResult(it)
    })
  }

  private fun processEphemeralQuestionResult(result: AsyncResult<EphemeralQuestion>) {
    if (result.isFailure()) {
      logger.e("QuestionPlayerFragment", "Failed to retrieve ephemeral question", result.getErrorOrNull()!!)
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }
    val ephemeralQuestion = result.getOrThrow()
    processProgress(ephemeralQuestion.currentQuestionIndex + 1, ephemeralQuestion.totalQuestionCount)
    processEphemeralState(ephemeralQuestion.ephemeralState)
  }

  private fun processProgress(currentQuestion: Int, numQuestions: Int) {
    questionViewModel.currentQuestion.set(currentQuestion)
    questionViewModel.numQuestions.set(numQuestions)
    questionViewModel.progressPercentage.set(((currentQuestion.toDouble() / numQuestions) * 100).toInt())
  }

  private fun processEphemeralState(ephemeralState: EphemeralState) {
    val pendingItemList = mutableListOf<StateItemViewModel>()
    addContentItem(pendingItemList, ephemeralState)
    val interaction = ephemeralState.state.interaction
    var hasGeneralContinueButton = false

    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      addPreviousAnswers(pendingItemList, interaction, ephemeralState.pendingState.wrongAnswerList)
      addInteractionForPendingState(pendingItemList, interaction)
    } else if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      addPreviousAnswers(pendingItemList, interaction, ephemeralState.completedState.answerList)
    }

    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      hasGeneralContinueButton = true
    }

    updateNavigationButtonVisibility(
      pendingItemList,
      hasGeneralContinueButton,
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    )

    questionViewModel.itemList.clear()
    questionViewModel.itemList += pendingItemList
  }

  private fun addContentItem(pendingItemList: MutableList<StateItemViewModel>, ephemeralState: EphemeralState) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    pendingItemList += ContentViewModel(contentSubtitledHtml.html)
  }

  private fun addInteractionForPendingState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction
  ) = addInteraction(pendingItemList, interaction)

  private fun addInteractionForCompletedState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, existingAnswer: InteractionObject
  ) = addInteraction(pendingItemList, interaction, existingAnswer = existingAnswer, isReadOnly = true)

  private fun addInteraction(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, existingAnswer:
    InteractionObject? = null, isReadOnly: Boolean = false) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory(
      "explorationId", interaction, fragment as InteractionAnswerReceiver, existingAnswer, isReadOnly
    )
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction,
    answersAndResponses: List<AnswerAndResponse>
  ) {
    // TODO: add support for displaying the previous answer, too.
    for (answerAndResponse in answersAndResponses) {
      addInteractionForCompletedState(pendingItemList, interaction, answerAndResponse.userAnswer)
      addFeedbackItem(pendingItemList, answerAndResponse.feedback)
    }
  }

  private fun addFeedbackItem(pendingItemList: MutableList<StateItemViewModel>, feedback: SubtitledHtml) {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      pendingItemList += FeedbackViewModel(feedback.html)
    }
  }
  private fun updateNavigationButtonVisibility(
    pendingItemList: MutableList<StateItemViewModel>,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean
  ) {
    val questionNavigationButtonViewModel = QuestionNavigationButtonViewModel(context, this as QuestionNavigationButtonListener)
    // Set continuation button.
    when {
      hasGeneralContinueButton -> {
        questionNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON, isEnabled = true
        )
      }
      stateIsTerminal -> {
        questionNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON,
          isEnabled = true
        )
      }
      questionViewModel.doesMostRecentInteractionRequireExplicitSubmission(pendingItemList) -> {
        questionNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.SUBMIT_BUTTON,
          isEnabled = true
        )
      }
      else -> {
        // No continuation button needs to be set since the interaction itself will push for answer submission.
        questionNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON,
          isEnabled = false
        )
      }
    }
    pendingItemList += questionNavigationButtonViewModel
  }

  private fun handleSubmitAnswer(answer: InteractionObject) {
    subscribeToAnswerOutcome(questionAssessmentProgressController.submitAnswer(answer))
  }

  /**
   * This function listens to the result of submitAnswer.
   * Whenever an answer is submitted using QuestionAssessmentProgressController.submitAnswer function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToAnswerOutcome(answerOutcomeResultLiveData: LiveData<AsyncResult<AnsweredQuestionOutcome>>) {
    val answerOutcomeLiveData = Transformations.map(answerOutcomeResultLiveData, ::processAnsweredQuestionOutcome)
    answerOutcomeLiveData.observe(fragment, Observer<AnsweredQuestionOutcome> { result ->
      if (firstTry && result.isCorrectAnswer) {
        numCorrect++
      }
      firstTry = false
    })
  }

  /** Helper for subscribeToAnswerOutcome. */
  private fun processAnsweredQuestionOutcome(answeredQuestionOutcomeResult: AsyncResult<AnsweredQuestionOutcome>): AnsweredQuestionOutcome {
    if (answeredQuestionOutcomeResult.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve answer outcome", answeredQuestionOutcomeResult.getErrorOrNull()!!)
    }
    return answeredQuestionOutcomeResult.getOrDefault(AnsweredQuestionOutcome.getDefaultInstance())
  }

  fun handleAnswerReadyForSubmission(answer: InteractionObject) {
    // An interaction has indicated that an answer is ready for submission.
    handleSubmitAnswer(answer)
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StateItemViewModel> {
    return BindableAdapter.Builder
      .newBuilder<StateItemViewModel>()
      .registerViewTypeComputer { viewModel ->
        when (viewModel) {
          is QuestionNavigationButtonViewModel -> ViewType.VIEW_TYPE_QUESTION_NAVIGATION_BUTTON.ordinal
          is ContentViewModel -> ViewType.VIEW_TYPE_CONTENT.ordinal
          is FeedbackViewModel -> ViewType.VIEW_TYPE_FEEDBACK.ordinal
          is SelectionInteractionViewModel -> ViewType.VIEW_TYPE_SELECTION_INTERACTION.ordinal
          is FractionInteractionViewModel -> ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION.ordinal
          is NumericInputViewModel -> ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION.ordinal
          is TextInputViewModel -> ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_QUESTION_NAVIGATION_BUTTON.ordinal,
        inflateDataBinding = QuestionButtonItemBinding::inflate,
        setViewModel = QuestionButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as QuestionNavigationButtonViewModel }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_CONTENT.ordinal,
        inflateView = { parent ->
          ContentItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<ContentItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create("", "").parseOppiaHtml(
            (viewModel as ContentViewModel).htmlContent.toString(), binding.contentTextView
          )
        }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_FEEDBACK.ordinal,
        inflateView = { parent ->
          FeedbackItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<FeedbackItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create("", "").parseOppiaHtml(
            (viewModel as FeedbackViewModel).htmlContent.toString(), binding.feedbackTextView
          )
        }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_SELECTION_INTERACTION.ordinal,
        inflateDataBinding = SelectionInteractionItemBinding::inflate,
        setViewModel = SelectionInteractionItemBinding::setViewModel,
        transformViewModel = { it as SelectionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION.ordinal,
        inflateDataBinding = FractionInteractionItemBinding::inflate,
        setViewModel = FractionInteractionItemBinding::setViewModel,
        transformViewModel = { it as FractionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION.ordinal,
        inflateDataBinding = NumericInputInteractionItemBinding::inflate,
        setViewModel = NumericInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as NumericInputViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal,
        inflateDataBinding = TextInputInteractionItemBinding::inflate,
        setViewModel = TextInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as TextInputViewModel }
      )
      .build()
  }

  private enum class ViewType {
    VIEW_TYPE_CONTENT,
    VIEW_TYPE_FEEDBACK,
    VIEW_TYPE_QUESTION_NAVIGATION_BUTTON,
    VIEW_TYPE_SELECTION_INTERACTION,
    VIEW_TYPE_FRACTION_INPUT_INTERACTION,
    VIEW_TYPE_NUMERIC_INPUT_INTERACTION,
    VIEW_TYPE_TEXT_INPUT_INTERACTION
  }

  private fun getQuestionPlayerViewModel(): QuestionPlayerViewModel {
    return viewModelProvider.getForFragment(fragment, QuestionPlayerViewModel::class.java)
  }

  override fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(questionViewModel.getPendingAnswer())
  }

  override fun onContinueButtonClicked() {
    firstTry = true
    hideKeyboard()
    moveToNextState()
  }

  override fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    questionTrainingController.stopQuestionTrainingSession()
    activity.finish()
  }

  private fun moveToNextState() {
    questionAssessmentProgressController.moveToNextQuestion()
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }
}
