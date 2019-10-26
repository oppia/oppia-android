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
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.NumberWithUnitsViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.ButtonInteractionListener
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
private const val END_EXPLORATION = "EndExploration"
@Suppress("unused")
private const val LEARN_AGAIN = "LearnAgain"
private const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
private const val ITEM_SELECT_INPUT = "ItemSelectionInput"
private const val TEXT_INPUT = "TextInput"
private const val FRACTION_INPUT = "FractionInput"
private const val NUMERIC_INPUT = "NumericInput"
private const val NUMERIC_WITH_UNITS = "NumberWithUnits"

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  @ExplorationHtmlParserEntityType private val entityType: String,
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val stateButtonViewModelProvider: ViewModelProvider<StateButtonViewModel>,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory
) : ButtonInteractionListener {

  private var showCellularDataDialog = true
  private var useCellularData = false
  private lateinit var explorationId: String

  // TODO(#257): Remove this once domain layer is capable to provide this information.
  private val oldStateNameList: ArrayList<String> = ArrayList()

  private lateinit var currentEphemeralState: EphemeralState
  private var currentAnswerOutcome: AnswerOutcome? = null

  private val itemList: MutableList<Any> = ArrayList()

  // TODO(#257): Remove this once domain layer is capable to provide this information.
  private var hasGeneralContinueButton: Boolean = false

  private lateinit var stateAdapter: StateAdapter

  private lateinit var binding: StateFragmentBinding

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
    stateAdapter = StateAdapter(itemList, this as ButtonInteractionListener, htmlParserFactory, entityType, explorationId)
    binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
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
        val audioFragment = AudioFragment.newInstance(explorationId, "END")
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
      currentEphemeralState = result

      addContentItem()
      if (currentEphemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
        addPreviousAnswers(currentEphemeralState.pendingState.wrongAnswerList)
      } else if (currentEphemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
        addPreviousAnswers(currentEphemeralState.completedState.answerList)
      }
      addInteractionForPendingState()
      updateDummyStateName()

      val interactionId = result.state.interaction.id
      val hasPreviousState = result.hasPreviousState
      var canContinueToNextState = false
      hasGeneralContinueButton = false

      if (result.stateTypeCase != EphemeralState.StateTypeCase.TERMINAL_STATE) {
        if (result.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE
          && !oldStateNameList.contains(result.state.name)
        ) {
          hasGeneralContinueButton = true
          canContinueToNextState = false
        } else if (result.completedState.answerList.size > 0
          && oldStateNameList.contains(result.state.name)
        ) {
          canContinueToNextState = true
          hasGeneralContinueButton = false
        }
      }

      updateNavigationButtonVisibility(
        interactionId,
        hasPreviousState,
        canContinueToNextState,
        hasGeneralContinueButton
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
    answerOutcomeLiveData.observe(fragment, Observer<AnswerOutcome> {
      currentAnswerOutcome = it

      // 'CONTINUE' button has two different types of functionality in different scenarios.
      // If the interaction-id is 'Continue', then learner can click the 'CONTINUE' button which will submit an answer
      // and move to next state. In other cases, learner submits an answer and if the answer is correct than the `SUBMIT`
      // button changes to 'CONTINUE' and in that case click on 'CONTINUE' button does not submit any answer and
      // directly moves to next state.
      // Here, after submitting an answer it checks whether the interaction-id was 'Continue', if it is continue then move
      // to next state.
      if (currentEphemeralState.state.interaction.id == CONTINUE) {
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

  private fun endExploration() {
    explorationDataController.stopPlayingExploration()
    (activity as ExplorationActivity).finish()
  }

  override fun onInteractionButtonClicked() {
    hideKeyboard()
    if (!hasGeneralContinueButton) {
      when (currentEphemeralState.state.interaction.id) {
        END_EXPLORATION -> endExploration()
        CONTINUE -> subscribeToAnswerOutcome(explorationProgressController.submitAnswer(createContinueButtonAnswer()))
        else -> subscribeToAnswerOutcome(
          explorationProgressController.submitAnswer(stateAdapter.getPendingAnswer())
        )
      }
    } else {
      moveToNextState()
    }
  }

  override fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  override fun onNextButtonClicked() {
    moveToNextState()
  }

  private fun moveToNextState() {
    checkAndUpdateOldStateNameList()
    itemList.clear()
    currentAnswerOutcome = null
    explorationProgressController.moveToNextState()
  }

  private fun createContinueButtonAnswer(): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER).build()
  }

  private fun checkAndUpdateOldStateNameList() {
    if (currentAnswerOutcome != null
      && !currentAnswerOutcome!!.sameState
      && !oldStateNameList.contains(currentEphemeralState.state.name)
    ) {
      oldStateNameList.add(currentEphemeralState.state.name)
    }
  }

  // TODO(BenHenning): Generalize adding interactions.

  private fun addInteractionForPendingState() {
    if (currentEphemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      when (currentEphemeralState.state.interaction.id) {
        MULTIPLE_CHOICE_INPUT, ITEM_SELECT_INPUT -> {
          addSelectionInteraction()
        }
        FRACTION_INPUT -> addInteraction(FractionInteractionViewModel())
        NUMERIC_INPUT -> addInteraction(NumericInputViewModel())
        NUMERIC_WITH_UNITS -> addInteraction(NumberWithUnitsViewModel())
        TEXT_INPUT -> addInteraction(TextInputViewModel())
      }
    }
  }

  private fun addSelectionInteraction() {
    val customizationArgsMap: Map<String, InteractionObject> =
      currentEphemeralState.state.interaction.customizationArgsMap
    val allKeys: Set<String> = customizationArgsMap.keys

    for (key in allKeys) {
      logger.d(TAG_STATE_FRAGMENT, key)
    }
    var maxAllowableSelectionCount = 0
    var minAllowableSelectionCount = 0
    lateinit var choiceItems: List<String>
    if (customizationArgsMap.contains("choices")) {
      if (customizationArgsMap.contains("maxAllowableSelectionCount")) {
        maxAllowableSelectionCount =
          currentEphemeralState.state.interaction.customizationArgsMap["maxAllowableSelectionCount"]!!.signedInt
        minAllowableSelectionCount =
          currentEphemeralState.state.interaction.customizationArgsMap["minAllowableSelectionCount"]!!.signedInt
      }
      choiceItems = currentEphemeralState.state.interaction.customizationArgsMap["choices"]!!.setOfHtmlString.htmlList
    } else {
      choiceItems = listOf()
    }
    itemList.add(
      SelectionInteractionViewModel(
        choiceItems, currentEphemeralState.state.interaction.id, maxAllowableSelectionCount, minAllowableSelectionCount
      )
    )
    stateAdapter.notifyDataSetChanged()
  }

  private fun addInteraction(viewModel: ViewModel) {
    itemList.add(viewModel)
    stateAdapter.notifyDataSetChanged()
  }

  private fun addContentItem() {
    val contentSubtitledHtml: SubtitledHtml = currentEphemeralState.state.content
    itemList.add(ContentViewModel(contentSubtitledHtml.contentId, contentSubtitledHtml.html))
    stateAdapter.notifyDataSetChanged()
  }

  private fun addPreviousAnswers(answersAndResponses: List<AnswerAndResponse>) {
    // TODO: add support for displaying the previous answer, too.
    for (answerAndResponse in answersAndResponses) {
      addFeedbackItem(answerAndResponse.feedback)
    }
  }

  private fun addFeedbackItem(feedback: SubtitledHtml) {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      itemList.add(FeedbackViewModel(feedback.contentId, feedback.html))
    }
    stateAdapter.notifyDataSetChanged()
  }

  private fun updateNavigationButtonVisibility(
    interactionId: String,
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean
  ) {
    getStateButtonViewModel().setPreviousButtonVisible(hasPreviousState)

    when {
      hasGeneralContinueButton -> {
        getStateButtonViewModel().clearObservableInteractionId()
        getStateButtonViewModel().setObservableInteractionId(CONTINUE)
      }
      canContinueToNextState -> {
        getStateButtonViewModel().clearObservableInteractionId()
        getStateButtonViewModel().setNextButtonVisible(canContinueToNextState)
      }
      else -> {
        getStateButtonViewModel().setObservableInteractionId(interactionId)
        // TODO(#163): This function controls whether the "Submit" button should be displayed or not.
        //  Remove this function in final implementation and control this whenever user selects some option in
        //  MultipleChoiceInput or InputSelectionInput. For now this is `true` because we do not have a mechanism to work
        //  with MultipleChoiceInput or InputSelectionInput, which will eventually be responsible for controlling this.
        getStateButtonViewModel().optionSelected(true)
      }
    }
    itemList.add(getStateButtonViewModel())
    stateAdapter.notifyDataSetChanged()
  }

  private fun getStateButtonViewModel(): StateButtonViewModel {
    return stateButtonViewModelProvider.getForFragment(fragment, StateButtonViewModel::class.java)
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }

  // TODO(#163): Remove this function, this is just for dummy testing purposes.
  private fun updateDummyStateName() {
    getStateViewModel().setStateName(currentEphemeralState.state.name)
  }
}
