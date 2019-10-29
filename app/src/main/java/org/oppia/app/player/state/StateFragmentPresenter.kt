package org.oppia.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.R
import org.oppia.app.databinding.StateFragmentBinding
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
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel.ContinuationNavigationButtonType
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.StateNavigationButtonListener
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
  private val stateNavigationButtonViewModelProvider: ViewModelProvider<StateNavigationButtonViewModel>,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory
) : StateNavigationButtonListener {

  private val itemList: MutableList<Any> = ArrayList()
  private var showCellularDataDialog = true
  private var useCellularData = false
  private lateinit var explorationId: String
  private lateinit var stateAdapter: StateAdapter
  private lateinit var currentStateName: String

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
    stateAdapter = StateAdapter(
      itemList, this as StateNavigationButtonListener, htmlParserFactory, entityType, explorationId
    )
    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.stateRecyclerView.apply {
      adapter = stateAdapter
    }
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }

    subscribeToCurrentState()

    return binding.root
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
      itemList.clear()

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

      stateAdapter.notifyDataSetChanged()
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
    handleSubmitAnswer(stateAdapter.getPendingAnswer())
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
    itemList.clear()
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
    val choiceItems = argsMap["choices"]?.setOfHtmlString?.htmlList ?: listOf()
    itemList += SelectionInteractionViewModel(
      choiceItems, interaction.id, fragment as InteractionAnswerReceiver, maxAllowableSelectionCount,
      minAllowableSelectionCount, existingAnswer, isReadOnly
    )
  }

  private fun addInteraction(viewModel: ViewModel) {
    itemList.add(viewModel)
  }

  private fun addContentItem(ephemeralState: EphemeralState) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    itemList.add(ContentViewModel(contentSubtitledHtml.contentId, contentSubtitledHtml.html))
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
      itemList.add(FeedbackViewModel(feedback.contentId, feedback.html))
    }
  }

  private fun updateNavigationButtonVisibility(
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean
  ) {
    getStateButtonViewModel().updatePreviousButton(isEnabled = hasPreviousState)

    // Set continuation button.
    when {
      hasGeneralContinueButton -> {
        getStateButtonViewModel().updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON, isEnabled = true
        )
      }
      canContinueToNextState -> {
        getStateButtonViewModel().updateContinuationButton(
          ContinuationNavigationButtonType.NEXT_BUTTON, isEnabled = canContinueToNextState
        )
      }
      stateIsTerminal -> {
        getStateButtonViewModel().updateContinuationButton(
          ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON, isEnabled = true
        )
      }
      stateAdapter.doesPendingInteractionRequireExplicitSubmission() -> {
        getStateButtonViewModel().updateContinuationButton(
          ContinuationNavigationButtonType.SUBMIT_BUTTON, isEnabled = true
        )
      }
      else -> {
        // No continuation button needs to be set since the interaction itself will push for answer submission.
        getStateButtonViewModel().updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON, isEnabled = false
        )
      }
    }
    itemList.add(getStateButtonViewModel())
  }

  private fun getStateButtonViewModel(): StateNavigationButtonViewModel {
    return stateNavigationButtonViewModelProvider.getForFragment(fragment, StateNavigationButtonViewModel::class.java)
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }
}
