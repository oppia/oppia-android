package org.oppia.app.player.state

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
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.databinding.SubmittedAnswerItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelFactory
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel.ContinuationNavigationButtonType
import org.oppia.app.player.state.itemviewmodel.SubmittedAnswerViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.StateNavigationButtonListener
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
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
  private val context: Context,
  private val interactionViewModelFactoryMap: Map<String, @JvmSuppressWildcards InteractionViewModelFactory>
) : StateNavigationButtonListener {

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

    subscribeToCurrentState()

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StateItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder(StateItemViewModel::viewType)
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.STATE_NAVIGATION_BUTTON,
        inflateDataBinding = StateButtonItemBinding::inflate,
        setViewModel = StateButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as StateNavigationButtonViewModel }
      )
      .registerViewBinder(
        viewType = StateItemViewModel.ViewType.CONTENT,
        inflateView = { parent ->
          ContentItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<ContentItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
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
          binding.htmlContent = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
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
            UserAnswer.TextualAnswerCase.HTML_ANSWER -> binding.submittedAnswer = htmlParserFactory.create(
              entityType, explorationId).parseOppiaHtml(userAnswer.htmlAnswer, binding.submittedAnswerTextView
            )
            else -> binding.submittedAnswer = userAnswer.plainAnswer
          }
        }
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
    currentStateName = ephemeralState.state.name
    val pendingItemList = mutableListOf<StateItemViewModel>()
    addContentItem(pendingItemList, ephemeralState)
    val interaction = ephemeralState.state.interaction
    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      addPreviousAnswers(pendingItemList, interaction, ephemeralState.pendingState.wrongAnswerList)
      addInteractionForPendingState(pendingItemList, interaction)
    } else if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      addPreviousAnswers(pendingItemList, interaction, ephemeralState.completedState.answerList)
    }

    val hasPreviousState = ephemeralState.hasPreviousState
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

    updateNavigationButtonVisibility(
      pendingItemList,
      hasPreviousState,
      canContinueToNextState,
      hasGeneralContinueButton,
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    )

    viewModel.itemList.clear()
    viewModel.itemList += pendingItemList
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
      }
    })
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

  override fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    explorationDataController.stopPlayingExploration()
    activity.finish()
  }

  override fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(viewModel.getPendingAnswer())
  }

  override fun onContinueButtonClicked() {
    hideKeyboard()
    moveToNextState()
  }

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer))
  }

  override fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  override fun onNextButtonClicked() = moveToNextState()

  private fun moveToNextState() {
    explorationProgressController.moveToNextState()
  }

  private fun addInteractionForPendingState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction
  ) = addInteraction(pendingItemList, interaction)

  private fun addInteractionForCompletedState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, existingAnswer: InteractionObject
  ) = addInteraction(pendingItemList, interaction, existingAnswer = existingAnswer, isReadOnly = true)

  private fun addInteraction(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, existingAnswer:
    InteractionObject? = null, isReadOnly: Boolean = false
  ) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory(
      explorationId, interaction, fragment as InteractionAnswerReceiver, existingAnswer, isReadOnly
    )
  }

  private fun addContentItem(pendingItemList: MutableList<StateItemViewModel>, ephemeralState: EphemeralState) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    pendingItemList += ContentViewModel(contentSubtitledHtml.html)
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction,
    answersAndResponses: List<AnswerAndResponse>
  ) {
    for (answerAndResponse in answersAndResponses) {
//      addInteractionForCompletedState(pendingItemList, interaction, answerAndResponse.userAnswer)
      addSubmittedAnswer(pendingItemList, answerAndResponse.userAnswer)
      addFeedbackItem(pendingItemList, answerAndResponse.feedback)
    }
  }

  private fun addSubmittedAnswer(pendingItemList: MutableList<StateItemViewModel>, userAnswer: UserAnswer) {
    pendingItemList += SubmittedAnswerViewModel(userAnswer)
  }

  private fun addFeedbackItem(pendingItemList: MutableList<StateItemViewModel>, feedback: SubtitledHtml) {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      pendingItemList += FeedbackViewModel(feedback.html)
    }
  }

  private fun updateNavigationButtonVisibility(
    pendingItemList: MutableList<StateItemViewModel>,
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean
  ) {
    val stateNavigationButtonViewModel = StateNavigationButtonViewModel(context, this as StateNavigationButtonListener)
    stateNavigationButtonViewModel.updatePreviousButton(isEnabled = hasPreviousState)

    // Set continuation button.
    when {
      hasGeneralContinueButton -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON, isEnabled = true
        )
      }
      canContinueToNextState -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.NEXT_BUTTON, isEnabled = canContinueToNextState
        )
      }
      stateIsTerminal -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON, isEnabled = true
        )
      }
      viewModel.doesMostRecentInteractionRequireExplicitSubmission(pendingItemList) -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.SUBMIT_BUTTON, isEnabled = true
        )
      }
      else -> {
        // No continuation button needs to be set since the interaction itself will push for answer submission.
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON, isEnabled = false
        )
      }
    }
    pendingItemList += stateNavigationButtonViewModel
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }
}
