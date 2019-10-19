package org.oppia.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
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
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionReadOnlyViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputInteractionViewModel
import org.oppia.app.player.state.listener.InteractionListener
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"
private const val TAG_AUDIO_FRAGMENT = "AUDIO_FRAGMENT"

const val CONTINUE = "Continue"
const val END_EXPLORATION = "EndExploration"
const val LEARN_AGAIN = "LearnAgain"
const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
const val ITEM_SELECT_INPUT = "ItemSelectionInput"
const val TEXT_INPUT = "TextInput"
const val FRACTION_INPUT = "FractionInput"
const val NUMERIC_INPUT = "NumericInput"
const val NUMERIC_WITH_UNITS = "NumberWithUnits"

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val stateButtonViewModelProvider: ViewModelProvider<StateButtonViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory
) : InteractionListener {

  private val oldStateNameList: ArrayList<String> = ArrayList()

  private val currentEphemeralState = ObservableField<EphemeralState>(EphemeralState.getDefaultInstance())
  private var currentAnswerOutcome: AnswerOutcome? = null

  private val itemList: MutableList<Any> = ArrayList()

  private var hasGeneralContinueButton: Boolean = false

  private var showCellularDataDialog = true
  private var useCellularData = false
  private lateinit var explorationId: String
  private val entityType: String = "exploration"

  private lateinit var stateAdapter: StateAdapter

  lateinit var binding: StateFragmentBinding

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    cellularDialogController.getCellularDataPreference()
      .observe(fragment, Observer<AsyncResult<CellularDataPreference>> {
        if (it.isSuccess()) {
          val prefs = it.getOrDefault(CellularDataPreference.getDefaultInstance())
          showCellularDataDialog = !(prefs.hideDialog)
          useCellularData = prefs.useCellularData
        }
      })
    explorationId = fragment.arguments!!.getString(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)

    stateAdapter = StateAdapter(itemList, this as InteractionListener, htmlParserFactory, entityType, explorationId)

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

  private fun showHideAudioFragment(isVisible: Boolean) {
    if (isVisible) {
      if (getAudioFragment() == null) {
        fragment.childFragmentManager.beginTransaction().add(
          R.id.audio_fragment_placeholder, AudioFragment(),
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
      if (result.hasState()) {
        itemList.clear()
        currentEphemeralState.set(result)
        checkAndAddContentItem()
        checkAndAddCompletedAnswerList()
        checkAndAddWrongAnswerList()
        checkAndAddInteraction()

        val interactionId = result.state.interaction.id
        val hasPreviousState = result.hasPreviousState
        var hasNextState = false
        hasGeneralContinueButton = false

        if (!result.terminalState) {
          if (result.stateTypeCase.number == EphemeralState.COMPLETED_STATE_FIELD_NUMBER
            && !oldStateNameList.contains(currentEphemeralState.get()!!.state.name)
          ) {
            hasGeneralContinueButton = true
            hasNextState = false
          } else if (result.completedState.answerList.size > 0
            && oldStateNameList.contains(currentEphemeralState.get()!!.state.name)
          ) {
            hasNextState = true
            hasGeneralContinueButton = false
          }
        }

        updateNavigationButtonVisibility(
          interactionId,
          hasPreviousState,
          hasNextState,
          hasGeneralContinueButton
        )
      }
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
      if (it.hasFeedback()) {
        addFeedbackItem(it.feedback)
      }

      if (currentEphemeralState.get()!!.state.interaction.id == CONTINUE) {
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
    // TODO(#163): Remove these dummy answers and fetch answers from different interaction views.
    // NB: This sample data will work only with TEST_EXPLORATION_ID_5
    // 0 -> What Language
    // 2 -> Welcome!
    // XX -> What Language
    val stateWelcomeAnswer = 0
    // finnish -> Numeric input
    // suomi -> Numeric input
    // XX -> What Language
    val stateWhatLanguageAnswer: String = "finnish"
    // 121 -> Things You can do
    // < 121 -> Estimate 100
    // > 121 -> Numeric Input
    // XX -> Numeric Input
    val stateNumericInputAnswer = 121

    if (!hasGeneralContinueButton) {
      val interactionObject: InteractionObject = stateAdapter.getInteractionObject()
      when (currentEphemeralState.get()!!.state.interaction.id) {
        END_EXPLORATION -> endExploration()
        CONTINUE -> subscribeToAnswerOutcome(explorationProgressController.submitAnswer(createContinueButtonAnswer()))
        MULTIPLE_CHOICE_INPUT -> subscribeToAnswerOutcome(
          explorationProgressController.submitAnswer(
            InteractionObject.newBuilder().setNonNegativeInt(
              stateWelcomeAnswer
            ).build()
          )
        )
        FRACTION_INPUT,
        ITEM_SELECT_INPUT,
        NUMERIC_INPUT,
        NUMERIC_WITH_UNITS,
        TEXT_INPUT -> subscribeToAnswerOutcome(
          explorationProgressController.submitAnswer(interactionObject)
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

  private fun checkAndAddContentItem() {
    if (currentEphemeralState.get()!!.state.hasContent()) {
      addContentItem()
    } else {
      logger.e("StateFragment", "checkAndAddContentItem: State does not have content.")
    }
  }

  private fun addContentItem() {
    val contentViewModel = ContentViewModel()
    val contentSubtitledHtml: SubtitledHtml = currentEphemeralState.get()!!.state.content
    if (contentSubtitledHtml.contentId != "") {
      contentViewModel.contentId = contentSubtitledHtml.contentId
    } else {
      contentViewModel.contentId = "content"
    }
    contentViewModel.htmlContent = contentSubtitledHtml.html
    itemList.add(contentViewModel)
    stateAdapter.notifyDataSetChanged()
  }

  private fun checkAndAddCompletedAnswerList() {
    if (currentEphemeralState.get()!!.completedState.answerCount > 0) {
      addCompletedAnswerList()
    }
  }

  private fun addCompletedAnswerList() {
    val answerResponseList: MutableList<AnswerAndResponse> = currentEphemeralState.get()!!.completedState.answerList
    for (answerResponse: AnswerAndResponse in answerResponseList) {
      if (answerResponse.hasUserAnswer()) {
        addLearnerAnswerItem(answerResponse.userAnswer)
      }
      if (answerResponse.hasFeedback()) {
        addFeedbackItem(answerResponse.feedback)
      }
    }
  }

  private fun checkAndAddWrongAnswerList() {
    if (currentEphemeralState.get()!!.pendingState.wrongAnswerCount > 0) {
      addWrongAnswerList()
    }
  }

  private fun addWrongAnswerList() {
    val wrongAnswerResponseList: MutableList<AnswerAndResponse> =
      currentEphemeralState.get()!!.pendingState.wrongAnswerList
    for (wrongAnswerResponse: AnswerAndResponse in wrongAnswerResponseList) {
      checkAndAddLearnerAnswerItem(wrongAnswerResponse)
      checkAndAddFeedbackItem(wrongAnswerResponse)
    }
  }

  private fun checkAndAddLearnerAnswerItem(answerResponse: AnswerAndResponse) {
    if (answerResponse.hasUserAnswer()) {
      addLearnerAnswerItem(answerResponse.userAnswer)
    }
  }

  private fun addLearnerAnswerItem(answerInteractionObject: InteractionObject) {
    val htmlString = when (currentEphemeralState.get()!!.state.interaction.id) {
      NUMERIC_INPUT -> answerInteractionObject.real.toString()
      TEXT_INPUT -> answerInteractionObject.normalizedString
      MULTIPLE_CHOICE_INPUT -> answerInteractionObject.normalizedString
      CONTINUE -> answerInteractionObject.normalizedString
      else -> ""
    }
    if (htmlString.isNotEmpty()) {
      val interactionReadOnlyViewModel = InteractionReadOnlyViewModel()
      interactionReadOnlyViewModel.htmlContent = htmlString
      itemList.add(interactionReadOnlyViewModel)
      stateAdapter.notifyDataSetChanged()
    }
  }

  private fun checkAndAddFeedbackItem(feedbackResponse: AnswerAndResponse) {
    if (feedbackResponse.hasFeedback()) {
      addFeedbackItem(feedbackResponse.feedback)
    }
  }

  private fun addFeedbackItem(feedback: SubtitledHtml) {
    val feedbackViewModel = ContentViewModel()
    if (feedback.contentId != "") {
      feedbackViewModel.contentId = feedback.contentId
    } else {
      feedbackViewModel.contentId = "feedback"
    }
    val feedbackHtml: String = feedback.html
    if (feedbackHtml.isNotEmpty()) {
      feedbackViewModel.htmlContent = feedbackHtml
      itemList.add(feedbackViewModel)
      stateAdapter.notifyDataSetChanged()
    }
  }

  private fun checkAndAddInteraction() {
    if (currentEphemeralState.get()!!.stateTypeCase.number == EphemeralState.PENDING_STATE_FIELD_NUMBER) {
      when (currentEphemeralState.get()!!.state.interaction.id) {
        NUMERIC_INPUT -> {
          addNumericInputItem()
        }
        TEXT_INPUT -> {
          addTextInputItem()
        }
      }
    }
  }

  private fun addNumericInputItem() {
    val customizationArgsMap: Map<String, InteractionObject> =
      currentEphemeralState.get()!!.state.interaction.customizationArgsMap
    val numericInputInteractionViewModel = NumericInputInteractionViewModel()
    if (customizationArgsMap.containsKey("placeholder")) {
      numericInputInteractionViewModel.placeholder =
        customizationArgsMap.getValue("placeholder").normalizedString
    }
    itemList.add(numericInputInteractionViewModel)
    stateAdapter.notifyDataSetChanged()
  }

  private fun addTextInputItem() {
    val customizationArgsMap: Map<String, InteractionObject> =
      currentEphemeralState.get()!!.state.interaction.customizationArgsMap
    val textInputInteractionViewModel = TextInputInteractionViewModel()
    if (customizationArgsMap.containsKey("placeholder")) {
      textInputInteractionViewModel.placeholder =
        customizationArgsMap.getValue("placeholder").normalizedString
    }
    itemList.add(textInputInteractionViewModel)
    stateAdapter.notifyDataSetChanged()
  }

  private fun checkAndUpdateOldStateNameList() {
    if (currentAnswerOutcome != null
      && !currentAnswerOutcome!!.sameState
      && !oldStateNameList.contains(currentEphemeralState.get()!!.state.name)
    ) {
      oldStateNameList.add(currentEphemeralState.get()!!.state.name)
    }
  }

  private fun updateNavigationButtonVisibility(
    interactionId: String,
    hasPreviousState: Boolean,
    hasNextState: Boolean,
    hasGeneralContinueButton: Boolean
  ) {
    getStateButtonViewModel().setPreviousButtonVisible(hasPreviousState)

    when {
      hasGeneralContinueButton -> {
        getStateButtonViewModel().clearObservableInteractionId()
        getStateButtonViewModel().setObservableInteractionId(CONTINUE)
      }
      hasNextState -> {
        getStateButtonViewModel().clearObservableInteractionId()
        getStateButtonViewModel().setNextButtonVisible(hasNextState)
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

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(TAG_AUDIO_FRAGMENT)
  }

  private fun getStateButtonViewModel(): StateButtonViewModel {
    return stateButtonViewModelProvider.getForFragment(fragment, StateButtonViewModel::class.java)
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }
}
