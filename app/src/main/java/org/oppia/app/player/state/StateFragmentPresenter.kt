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
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NumberWithUnitsInputInteractionItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.NumberWithUnitsViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionContentViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel.ContinuationNavigationButtonType
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
private const val TAG_STATE_FRAGMENT = "STATE_FRAGMENT"

private const val CONTINUE = "Continue"
private const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
private const val ITEM_SELECT_INPUT = "ItemSelectionInput"
private const val TEXT_INPUT = "TextInput"
private const val FRACTION_INPUT = "FractionInput"
private const val NUMERIC_INPUT = "NumericInput"
private const val NUMERIC_WITH_UNITS = "NumberWithUnits"

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
  private val context: Context
) : StateNavigationButtonListener {

  private var showCellularDataDialog = true
  private var useCellularData = false
  private lateinit var explorationId: String
  private lateinit var currentStateName: String
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private lateinit var viewModel: StateViewModel

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
    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
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
    return BindableAdapter.Builder
      .newBuilder<StateItemViewModel>()
      .registerViewTypeComputer { viewModel ->
        when (viewModel) {
          is StateNavigationButtonViewModel -> ViewType.VIEW_TYPE_STATE_NAVIGATION_BUTTON.ordinal
          is ContentViewModel -> ViewType.VIEW_TYPE_CONTENT.ordinal
          is FeedbackViewModel -> ViewType.VIEW_TYPE_FEEDBACK.ordinal
          is ContinueInteractionViewModel -> ViewType.VIEW_TYPE_CONTINUE_INTERACTION.ordinal
          is SelectionInteractionViewModel -> ViewType.VIEW_TYPE_SELECTION_INTERACTION.ordinal
          is FractionInteractionViewModel -> ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION.ordinal
          is NumericInputViewModel -> ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION.ordinal
          is NumberWithUnitsViewModel -> ViewType.VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION.ordinal
          is TextInputViewModel -> ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_STATE_NAVIGATION_BUTTON.ordinal,
        inflateDataBinding = StateButtonItemBinding::inflate,
        setViewModel = StateButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as StateNavigationButtonViewModel }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_CONTENT.ordinal,
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
        viewType = ViewType.VIEW_TYPE_FEEDBACK.ordinal,
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
        viewType = ViewType.VIEW_TYPE_CONTINUE_INTERACTION.ordinal,
        inflateDataBinding = ContinueInteractionItemBinding::inflate,
        setViewModel = ContinueInteractionItemBinding::setViewModel,
        transformViewModel = { it as ContinueInteractionViewModel }
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
        viewType = ViewType.VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION.ordinal,
        inflateDataBinding = NumberWithUnitsInputInteractionItemBinding::inflate,
        setViewModel = NumberWithUnitsInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as NumberWithUnitsViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal,
        inflateDataBinding = TextInputInteractionItemBinding::inflate,
        setViewModel = TextInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as TextInputViewModel }
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

  fun handleAnswerReadyForSubmission(answer: InteractionObject) {
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
    if (isVisible) {
      if (getAudioFragment() == null) {
        val audioFragment = AudioFragment.newInstance(explorationId, currentStateName)
        fragment.childFragmentManager.beginTransaction().add(
          R.id.audio_fragment_placeholder, audioFragment,
          TAG_AUDIO_FRAGMENT
        ).commitNow()
      }
    } else {
      if (getAudioFragment() != null) {
        fragment.childFragmentManager.beginTransaction().remove(getAudioFragment()!!).commitNow()
      }
    }
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment, Observer<EphemeralState> { result ->
      viewModel.itemList.clear()

      addContentItem(result)
      val interaction = result.state.interaction
      if (result.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
        addPreviousAnswers(interaction, result.pendingState.wrongAnswerList)
        addInteractionForPendingState(interaction)
      } else if (result.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
        addPreviousAnswers(interaction, result.completedState.answerList)
      }

      val hasPreviousState = result.hasPreviousState
      var canContinueToNextState = false
      var hasGeneralContinueButton = false

      if (result.stateTypeCase != EphemeralState.StateTypeCase.TERMINAL_STATE) {
        if (result.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE && !result.hasNextState) {
          hasGeneralContinueButton = true
        } else if (result.completedState.answerList.size > 0 && result.hasNextState) {
          canContinueToNextState = true
        }
      }

      updateNavigationButtonVisibility(
        hasPreviousState,
        canContinueToNextState,
        hasGeneralContinueButton,
        result.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
      )
    })
  }

  private val ephemeralStateLiveData: LiveData<EphemeralState> by lazy {
    getEphemeralState()
  }

  private fun getEphemeralState(): LiveData<EphemeralState> {
    return Transformations.map(explorationProgressController.getCurrentState(), ::processCurrentState)
  }

  private fun processCurrentState(ephemeralStateResult: AsyncResult<EphemeralState>): EphemeralState {
    if (ephemeralStateResult.isFailure()) {
      logger.e("StateFragment", "Failed to retrieve ephemeral state", ephemeralStateResult.getErrorOrNull()!!)
    }
    return ephemeralStateResult.getOrDefault(EphemeralState.getDefaultInstance())
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
      if (result.state.interaction.id == CONTINUE) {
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

  private fun handleSubmitAnswer(answer: InteractionObject) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer))
  }

  override fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  override fun onNextButtonClicked() = moveToNextState()

  private fun moveToNextState() {
    viewModel.itemList.clear()
    explorationProgressController.moveToNextState()
  }

  // TODO(BenHenning): Generalize adding interactions.

  private fun addInteractionForPendingState(interaction: Interaction) {
    addInteraction(interaction)
  }

  private fun addInteractionForCompletedState(interaction: Interaction, existingAnswer: InteractionObject) {
    addInteraction(interaction, existingAnswer = existingAnswer, isReadOnly = true)
  }

  private fun addInteraction(
    interaction: Interaction, existingAnswer: InteractionObject? = null, isReadOnly: Boolean = false) {
    when (interaction.id) {
      CONTINUE -> addInteraction(
        ContinueInteractionViewModel(fragment as InteractionAnswerReceiver, existingAnswer, isReadOnly)
      )
      MULTIPLE_CHOICE_INPUT, ITEM_SELECT_INPUT -> addSelectionInteraction(interaction, existingAnswer, isReadOnly)
      FRACTION_INPUT -> addInteraction(FractionInteractionViewModel(existingAnswer, isReadOnly))
      NUMERIC_INPUT -> addInteraction(NumericInputViewModel(existingAnswer, isReadOnly))
      NUMERIC_WITH_UNITS -> addInteraction(NumberWithUnitsViewModel(existingAnswer, isReadOnly))
      TEXT_INPUT -> addInteraction(TextInputViewModel(existingAnswer, isReadOnly))
    }
  }

  private fun addSelectionInteraction(
    interaction: Interaction, existingAnswer: InteractionObject?, isReadOnly: Boolean
  ) {
    val argsMap = interaction.customizationArgsMap
    argsMap.keys.forEach { logger.d(TAG_STATE_FRAGMENT, it) }

    // Assume that at least 1 answer always needs to be submitted, and that the max can't be less than the min for cases
    // when either of the counts are not specified.
    val minAllowableSelectionCount = argsMap["minAllowableSelectionCount"]?.signedInt ?: 1
    val maxAllowableSelectionCount = argsMap["maxAllowableSelectionCount"]?.signedInt ?: minAllowableSelectionCount
    val choiceItemStrings = argsMap["choices"]?.setOfHtmlString?.htmlList ?: listOf()
    viewModel.itemList += SelectionInteractionViewModel(
      choiceItemStrings, explorationId, interaction.id, fragment as InteractionAnswerReceiver,
      maxAllowableSelectionCount, minAllowableSelectionCount, existingAnswer, isReadOnly
    )
  }

  private fun addInteraction(stateItemViewModel: StateItemViewModel) {
    viewModel.itemList += stateItemViewModel
  }

  private fun addContentItem(ephemeralState: EphemeralState) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    viewModel.itemList += ContentViewModel(contentSubtitledHtml.html)
  }

  private fun addPreviousAnswers(interaction: Interaction, answersAndResponses: List<AnswerAndResponse>) {
    // TODO: add support for displaying the previous answer, too.
    for (answerAndResponse in answersAndResponses) {
      addInteractionForCompletedState(interaction, answerAndResponse.userAnswer)
      addFeedbackItem(answerAndResponse.feedback)
    }
  }

  private fun addFeedbackItem(feedback: SubtitledHtml) {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      viewModel.itemList  += FeedbackViewModel(feedback.html)
    }
  }

  private fun updateNavigationButtonVisibility(
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
      viewModel.doesPendingInteractionRequireExplicitSubmission() -> {
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
    viewModel.itemList += stateNavigationButtonViewModel
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }

  private enum class ViewType {
    VIEW_TYPE_CONTENT,
    VIEW_TYPE_FEEDBACK,
    VIEW_TYPE_STATE_NAVIGATION_BUTTON,
    VIEW_TYPE_CONTINUE_INTERACTION,
    VIEW_TYPE_SELECTION_INTERACTION,
    VIEW_TYPE_FRACTION_INPUT_INTERACTION,
    VIEW_TYPE_NUMERIC_INPUT_INTERACTION,
    VIEW_TYPE_NUMBER_WITH_UNITS_INPUT_INTERACTION,
    VIEW_TYPE_TEXT_INPUT_INTERACTION
  }
}
