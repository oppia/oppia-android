package org.oppia.app.player.state

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.ContinueNavigationButtonItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NextButtonItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.PreviousButtonItemBinding
import org.oppia.app.databinding.PreviousResponsesHeaderItemBinding
import org.oppia.app.databinding.ReturnToTopicButtonItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.databinding.SubmitButtonItemBinding
import org.oppia.app.databinding.SubmittedAnswerItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Interaction
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelFactory
import org.oppia.app.player.state.itemviewmodel.NextButtonViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.PreviousButtonViewModel
import org.oppia.app.player.state.itemviewmodel.PreviousResponsesHeaderViewModel
import org.oppia.app.player.state.itemviewmodel.ReturnToTopicButtonViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.SubmitButtonViewModel
import org.oppia.app.player.state.itemviewmodel.SubmittedAnswerViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.app.player.state.listener.NextNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.app.player.stopexploration.StopExplorationInterface
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.Logger
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

const val STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY = "STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY"
private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"
private const val TAG_AUDIO_FRAGMENT = "AUDIO_FRAGMENT"

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  @ExplorationHtmlParserEntityType private val entityType: String,
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory,
  private val interactionViewModelFactoryMap: Map<String, @JvmSuppressWildcards InteractionViewModelFactory>,
  @DefaultResourceBucketName private val resourceBucketName: String
) : PreviousResponsesHeaderClickListener {

  private var showCellularDataDialog = true
  private var useCellularData = false
  private lateinit var explorationId: String
  private lateinit var currentStateName: String
  private lateinit var binding: StateFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private lateinit var viewModel: StateViewModel
  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState()
  }
  /**
   * A list of view models corresponding to past view models that are hidden by default. These are intentionally not
   * retained upon configuration changes since the user can just re-expand the list. Note that the first element of this
   * list (when initialized), will always be the previous answers header to help locate the items in the recycler view
   * (when present).
   */
  private val previousAnswerViewModels: MutableList<StateItemViewModel> = mutableListOf()
  /**
   * Whether the previously submitted wrong answers should be expanded. This value is intentionally not retained upon
   * configuration changes since the user can just re-expand the list.
   */
  private var hasPreviousResponsesExpanded: Boolean = false

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    cellularDialogController.getCellularDataPreference()
      .observe(fragment, Observer<AsyncResult<CellularDataPreference>> {
        if (it.isSuccess()) {
          val prefs = it.getOrDefault(CellularDataPreference.getDefaultInstance())
          showCellularDataDialog = !(prefs.hideDialog)
          useCellularData = prefs.useCellularData
        }
      })
    explorationId = fragment.arguments!!.getString(STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY)!!

    binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val stateRecyclerViewAdapter = createRecyclerViewAdapter()
    binding.stateRecyclerView.apply {
      adapter = stateRecyclerViewAdapter
    }
    recyclerViewAdapter = stateRecyclerViewAdapter
    viewModel = getStateViewModel()
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = this.viewModel
    }

    binding.stateRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      if (bottom < oldBottom) {
        binding.stateRecyclerView.postDelayed({
          binding.stateRecyclerView.scrollToPosition(stateRecyclerViewAdapter.itemCount - 1)
        }, 100)
      }
    }
    subscribeToCurrentState()

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StateItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder(StateItemViewModel::viewType)
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON,
        inflateDataBinding = ContinueNavigationButtonItemBinding::inflate,
        setViewModel = ContinueNavigationButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as ContinueNavigationButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.PREVIOUS_NAVIGATION_BUTTON,
        inflateDataBinding = PreviousButtonItemBinding::inflate,
        setViewModel = PreviousButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as PreviousButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON,
        inflateDataBinding = NextButtonItemBinding::inflate,
        setViewModel = NextButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as NextButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.RETURN_TO_TOPIC_NAVIGATION_BUTTON,
        inflateDataBinding = ReturnToTopicButtonItemBinding::inflate,
        setViewModel = ReturnToTopicButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as ReturnToTopicButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON,
        inflateDataBinding = SubmitButtonItemBinding::inflate,
        setViewModel = SubmitButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as SubmitButtonViewModel }
      )
      .registerViewBinder(
        viewType = StateItemViewModel.ViewType.CONTENT,
        inflateView = { parent ->
          ContentItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<ContentItemBinding>(view)!!
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName, entityType, explorationId, /* imageCenterAlign= */ true
            ).parseOppiaHtml(
              (viewModel as ContentViewModel).htmlContent.toString(), binding.contentTextView
            )
        }
      )
      .registerViewBinder(
        viewType = StateItemViewModel.ViewType.FEEDBACK,
        inflateView = { parent ->
          FeedbackItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<FeedbackItemBinding>(view)!!
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName, entityType, explorationId, /* imageCenterAlign= */ true
            ).parseOppiaHtml(
              (viewModel as FeedbackViewModel).htmlContent.toString(), binding.feedbackTextView
            )
        }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.CONTINUE_INTERACTION,
        inflateDataBinding = ContinueInteractionItemBinding::inflate,
        setViewModel = ContinueInteractionItemBinding::setViewModel,
        transformViewModel = { it as ContinueInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.SELECTION_INTERACTION,
        inflateDataBinding = SelectionInteractionItemBinding::inflate,
        setViewModel = SelectionInteractionItemBinding::setViewModel,
        transformViewModel = { it as SelectionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION,
        inflateDataBinding = FractionInteractionItemBinding::inflate,
        setViewModel = FractionInteractionItemBinding::setViewModel,
        transformViewModel = { it as FractionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION,
        inflateDataBinding = NumericInputInteractionItemBinding::inflate,
        setViewModel = NumericInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as NumericInputViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION,
        inflateDataBinding = TextInputInteractionItemBinding::inflate,
        setViewModel = TextInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as TextInputViewModel }
      )
      .registerViewBinder(
        viewType = StateItemViewModel.ViewType.SUBMITTED_ANSWER,
        inflateView = { parent ->
          SubmittedAnswerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<SubmittedAnswerItemBinding>(view)!!
          val userAnswer = (viewModel as SubmittedAnswerViewModel).submittedUserAnswer
          when (userAnswer.textualAnswerCase) {
            UserAnswer.TextualAnswerCase.HTML_ANSWER -> {
              val htmlParser = htmlParserFactory.create(
                resourceBucketName, entityType, explorationId, imageCenterAlign = false
              )
              binding.submittedAnswer = htmlParser.parseOppiaHtml(
                userAnswer.htmlAnswer, binding.submittedAnswerTextView
              )
            }
            else -> binding.submittedAnswer = userAnswer.plainAnswer
          }
        }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER,
        inflateDataBinding = PreviousResponsesHeaderItemBinding::inflate,
        setViewModel = PreviousResponsesHeaderItemBinding::setViewModel,
        transformViewModel = { it as PreviousResponsesHeaderViewModel }
      )
      .build()
  }

  fun handleAudioClick() {
    if (showCellularDataDialog) {
      showHideAudioFragment(false)
      showCellularDataDialogFragment()
    } else {
      if (useCellularData) {
        showHideAudioFragment(getAudioFragment() == null)
      } else {
        showHideAudioFragment(false)
      }
    }
  }

  fun handleEnableAudio(saveUserChoice: Boolean) {
    showHideAudioFragment(true)
    if (saveUserChoice) {
      cellularDialogController.setAlwaysUseCellularDataPreference()
    }
  }

  fun handleDisableAudio(saveUserChoice: Boolean) {
    if (saveUserChoice) {
      cellularDialogController.setNeverUseCellularDataPreference()
    }
  }

  fun handleAnswerReadyForSubmission(answer: UserAnswer) {
    // An interaction has indicated that an answer is ready for submission.
    handleSubmitAnswer(answer)
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(TAG_AUDIO_FRAGMENT)
  }

  private fun showHideAudioFragment(isVisible: Boolean) {
    getStateViewModel().setAudioBarVisibility(isVisible)
    if (isVisible) {
      if (getAudioFragment() == null) {
        val audioFragment = AudioFragment.newInstance(explorationId, currentStateName)
        fragment.childFragmentManager.beginTransaction().add(
          R.id.audio_fragment_placeholder, audioFragment,
          TAG_AUDIO_FRAGMENT
        ).commitNow()
      }

      val currentYOffset = binding.stateRecyclerView.computeVerticalScrollOffset()
      if (currentYOffset == 0) {
        binding.stateRecyclerView.smoothScrollToPosition(0)
      }
    } else {
      if (getAudioFragment() != null) {
        fragment.childFragmentManager.beginTransaction().remove(getAudioFragment()!!).commitNow()
      }
    }
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment, Observer<AsyncResult<EphemeralState>> { result ->
      processEphemeralStateResult(result)
    })
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralState>) {
    if (result.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state", result.getErrorOrNull()!!)
      return
    } else if (result.isPending()) {
      // Display nothing until a valid result is available.
      return
    }

    val ephemeralState = result.getOrThrow()
    val hasPreviousState = ephemeralState.hasPreviousState

    val scrollToTop = ::currentStateName.isInitialized && currentStateName != ephemeralState.state.name

    currentStateName = ephemeralState.state.name
    previousAnswerViewModels.clear() // But retain whether the list is currently open.
    val pendingItemList = mutableListOf<StateItemViewModel>()
    addContentItem(pendingItemList, ephemeralState)
    val interaction = ephemeralState.state.interaction
    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.pendingState.wrongAnswerList)
      addInteractionForPendingState(pendingItemList, interaction, hasPreviousState)
    } else if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.completedState.answerList)
    }

    var canContinueToNextState = false
    var hasGeneralContinueButton = false
    if (ephemeralState.stateTypeCase != EphemeralState.StateTypeCase.TERMINAL_STATE) {
      if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE
        && !ephemeralState.hasNextState
      ) {
        hasGeneralContinueButton = true
      } else if (ephemeralState.completedState.answerList.size > 0 && ephemeralState.hasNextState) {
        canContinueToNextState = true
      }
    }

    maybeAddNavigationButtons(
      pendingItemList,
      hasPreviousState,
      canContinueToNextState,
      hasGeneralContinueButton,
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    )

    viewModel.itemList.clear()
    viewModel.itemList += pendingItemList

    if (scrollToTop) {
      (binding.stateRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 200)
    }
  }

  /**
   * This function listens to the result of submitAnswer.
   * Whenever an answer is submitted using ExplorationProgressController.submitAnswer function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToAnswerOutcome(answerOutcomeResultLiveData: LiveData<AsyncResult<AnswerOutcome>>) {
    val answerOutcomeLiveData = getAnswerOutcome(answerOutcomeResultLiveData)
    answerOutcomeLiveData.observe(fragment, Observer<AnswerOutcome> { result ->
      // If the answer was submitted on behalf of the Continue interaction, automatically continue to the next state.
      if (result.state.interaction.id == "Continue") {
         moveToNextState()
      } else if (result.labelledAsCorrectAnswer) {
        showCongratulationMessageOnCorrectAnswer()
      }
    })
  }

  private fun showCongratulationMessageOnCorrectAnswer() {
    binding.congratulationTextView.visibility = View.VISIBLE

    val fadeIn = AlphaAnimation(0f, 1f)
    fadeIn.interpolator = DecelerateInterpolator()
    fadeIn.duration = 2000

    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.startOffset = 1000
    fadeOut.duration = 1000

    val animation = AnimationSet(false)
    animation.addAnimation(fadeIn)
    animation.addAnimation(fadeOut)
    binding.congratulationTextView.animation = animation

    Handler().postDelayed({
      binding.congratulationTextView.clearAnimation()
      binding.congratulationTextView.visibility = View.INVISIBLE
    }, 2000)
  }

  /** Helper for subscribeToAnswerOutcome. */
  private fun getAnswerOutcome(answerOutcome: LiveData<AsyncResult<AnswerOutcome>>): LiveData<AnswerOutcome> {
    return Transformations.map(answerOutcome, ::processAnswerOutcome)
  }

  /** Helper for subscribeToAnswerOutcome. */
  private fun processAnswerOutcome(ephemeralStateResult: AsyncResult<AnswerOutcome>): AnswerOutcome {
    if (ephemeralStateResult.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve answer outcome", ephemeralStateResult.getErrorOrNull()!!)
    }
    return ephemeralStateResult.getOrDefault(AnswerOutcome.getDefaultInstance())
  }

  fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    (activity as StopExplorationInterface).stopExploration()
  }

  fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(viewModel.getPendingAnswer())
  }

  fun handleKeyboardAction() {
    hideKeyboard()
    handleSubmitAnswer(viewModel.getPendingAnswer())
  }

  fun onContinueButtonClicked() {
    hideKeyboard()
    moveToNextState()
  }

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer))
  }

  fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  fun onNextButtonClicked() = moveToNextState()

  override fun onResponsesHeaderClicked() {
    togglePreviousAnswers()
  }

  private fun moveToNextState() {
    explorationProgressController.moveToNextState().observe(fragment, Observer<AsyncResult<Any?>> {
      hasPreviousResponsesExpanded = false
    })
  }

  private fun addInteractionForPendingState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, hasPreviousButton: Boolean
  ) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory(
      explorationId, interaction, fragment as InteractionAnswerReceiver, hasPreviousButton
    )
  }

  private fun addContentItem(pendingItemList: MutableList<StateItemViewModel>, ephemeralState: EphemeralState) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    pendingItemList += ContentViewModel(contentSubtitledHtml.html)
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>, answersAndResponses: List<AnswerAndResponse>
  ) {
    if (answersAndResponses.size > 1) {
      PreviousResponsesHeaderViewModel(
        answersAndResponses.size - 1, ObservableBoolean(hasPreviousResponsesExpanded), this
      ).let { viewModel ->
        pendingItemList += viewModel
        previousAnswerViewModels += viewModel
      }
      // Only add previous answers if current responses are expanded.
      for (answerAndResponse in answersAndResponses.take(answersAndResponses.size - 1)) {
        createSubmittedAnswer(answerAndResponse.userAnswer).let { viewModel ->
          if (hasPreviousResponsesExpanded) {
            pendingItemList += viewModel
          }
          previousAnswerViewModels += viewModel
        }
        createFeedbackItem(answerAndResponse.feedback)?.let { viewModel ->
          if (hasPreviousResponsesExpanded) {
            pendingItemList += viewModel
          }
          previousAnswerViewModels += viewModel
        }
      }
    }
    answersAndResponses.lastOrNull()?.let { answerAndResponse ->
      pendingItemList += createSubmittedAnswer(answerAndResponse.userAnswer)
      createFeedbackItem(answerAndResponse.feedback)?.let(pendingItemList::add)
    }
  }

  /**
   * Toggles whether the previous answers should be shown based on the current state stored in
   * [PreviousResponsesHeaderViewModel].
   */
  private fun togglePreviousAnswers() {
    val headerModel = previousAnswerViewModels.first() as PreviousResponsesHeaderViewModel
    val expandPreviousAnswers = !headerModel.isExpanded.get()
    val headerIndex = viewModel.itemList.indexOf(headerModel)
    val previousAnswersAndFeedbacks = previousAnswerViewModels.takeLast(previousAnswerViewModels.size - 1)
    if (expandPreviousAnswers) {
      // Add the pending view models to the recycler view to expand them.
      viewModel.itemList.addAll(headerIndex + 1, previousAnswersAndFeedbacks)
    } else {
      // Remove the pending view models to collapse the list.
      viewModel.itemList.removeAll(previousAnswersAndFeedbacks)
    }
    // Ensure the header matches the updated state.
    headerModel.isExpanded.set(expandPreviousAnswers)
    hasPreviousResponsesExpanded = expandPreviousAnswers
    recyclerViewAdapter.notifyDataSetChanged()
  }

  private fun createSubmittedAnswer(userAnswer: UserAnswer): SubmittedAnswerViewModel {
    return SubmittedAnswerViewModel(userAnswer)
  }

  private fun createFeedbackItem(feedback: SubtitledHtml): FeedbackViewModel? {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      return FeedbackViewModel(feedback.html)
    }
    return null
  }

  private fun maybeAddNavigationButtons(
    pendingItemList: MutableList<StateItemViewModel>,
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean) {
    when {
      hasGeneralContinueButton -> ContinueNavigationButtonViewModel(
        hasPreviousState, fragment as PreviousNavigationButtonListener, fragment as ContinueNavigationButtonListener
      )
      canContinueToNextState -> NextButtonViewModel(
        hasPreviousState, fragment as PreviousNavigationButtonListener, fragment as NextNavigationButtonListener
      )
      stateIsTerminal -> ReturnToTopicButtonViewModel(
        hasPreviousState, fragment as PreviousNavigationButtonListener,
        fragment as ReturnToTopicNavigationButtonListener
      )
      viewModel.doesMostRecentInteractionRequireExplicitSubmission(pendingItemList) -> SubmitButtonViewModel(
        hasPreviousState, fragment as PreviousNavigationButtonListener, fragment as SubmitNavigationButtonListener
      )
      // Otherwise, just show the previous button since the interaction itself will push the answer submission.
      hasPreviousState && !viewModel.isMostRecentInteractionAutoNavigating(pendingItemList) -> PreviousButtonViewModel(
        fragment as PreviousNavigationButtonListener
      )
      // Otherwise, there's no navigation button that should be shown since the current interaction handles this.
      else -> null
    }?.let(pendingItemList::add)
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }
}
