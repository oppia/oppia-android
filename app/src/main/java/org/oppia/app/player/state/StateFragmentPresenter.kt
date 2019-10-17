package org.oppia.app.player.state

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
  private val logger: Logger
) : InteractionListener {

  private val completedStateNameList: ArrayList<String> = ArrayList()

  private val currentEphemeralState = ObservableField<EphemeralState>(EphemeralState.getDefaultInstance())
  private var currentAnswerOutcome: AnswerOutcome? = null

  private val itemList: MutableList<Any> = ArrayList()

  private var showCellularDataDialog = true
  private var useCellularData = false
  private var explorationId: String? = null

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

    stateAdapter = StateAdapter(itemList, this as InteractionListener)

    binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.stateRecyclerView.apply {
      adapter = stateAdapter
    }
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.viewModel = getStateViewModel()
    }
    explorationId = fragment.arguments!!.getString(EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY)

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
      
      logger.d(
        "StateFragment",
        "subscribeToCurrentState: completedState.answerCount: " + result.completedState.answerCount
      )
      logger.d("StateFragment", "subscribeToCurrentState: hasPreviousState: " + result.hasPreviousState)
      logger.d(
        "StateFragment",
        "subscribeToCurrentState: pendingState.wrongAnswerCount: " + result.pendingState.wrongAnswerCount
      )
      logger.d("StateFragment", "subscribeToCurrentState: state.name: " + result.state.name)
      logger.d("StateFragment", "subscribeToCurrentState: terminalState: " + result.terminalState)
      logger.d("StateFragment", "subscribeToCurrentState: isInitialized: " + result.isInitialized)
      logger.d("StateFragment", "subscribeToCurrentState: hasState: " + result.hasState())
      logger.d("StateFragment", "subscribeToCurrentState: getStateTypeCase: " + result.stateTypeCase.number)

      logger.d("StateFragment", "subscribeToCurrentState: ***********************************************************************************************")
      logger.d("StateFragment", "subscribeToCurrentState: ***********************************************************************************************")

      itemList.clear()
      currentEphemeralState.set(result)
      if (result.state.hasContent()) {
        val contentViewModel = ContentViewModel()
        if (result.state.content.contentId != "") {
          contentViewModel.contentId = result.state.content.contentId
        } else {
          contentViewModel.contentId = "content"
        }
        contentViewModel.htmlContent = result.state.content.html
        itemList.add(contentViewModel)
        stateAdapter.notifyDataSetChanged()
      }

      val answerResponseList: MutableList<AnswerAndResponse> = result.completedState.answerList
      for (answerResponse: AnswerAndResponse in answerResponseList) {
        if (answerResponse.hasUserAnswer()) {
          addLearnerAnswerItem(answerResponse.userAnswer)
        }
        if (answerResponse.hasFeedback()) {
          addFeedbackItem(answerResponse.feedback)
        }
      }

      val interactionId = result.state.interaction.id
      val hasPreviousState = result.hasPreviousState
      val hasNextState = answerResponseList.size > 0

      val customizationArgsMap: Map<String, InteractionObject> = result.state.interaction.customizationArgsMap

      if ((currentAnswerOutcome == null || currentAnswerOutcome!!.sameState) && !completedStateNameList.contains(
          currentEphemeralState.get()!!.state.name
        )
      ) {
        when (interactionId) {
          NUMERIC_INPUT -> {
            val numericInputInteractionViewModel = NumericInputInteractionViewModel()
            if (customizationArgsMap.containsKey("placeholder")) {
              numericInputInteractionViewModel.placeholder =
                customizationArgsMap.getValue("placeholder").normalizedString
            }
            itemList.add(numericInputInteractionViewModel)
            stateAdapter.notifyDataSetChanged()
          }
          TEXT_INPUT -> {
            val textInputInteractionViewModel = TextInputInteractionViewModel()
            if (customizationArgsMap.containsKey("placeholder")) {
              textInputInteractionViewModel.placeholder =
                customizationArgsMap.getValue("placeholder").normalizedString
            }
            itemList.add(textInputInteractionViewModel)
            stateAdapter.notifyDataSetChanged()
          }
        }
      }

      updateNavigationButtonVisibility(
        interactionId,
        hasPreviousState,
        hasNextState
      )
    })
  }

  private fun updateNavigationButtonVisibility(
    interactionId: String,
    hasPreviousState: Boolean,
    hasNextState: Boolean
  ) {
    logger.d("StateFragment", "interactionId: $interactionId")
    getStateButtonViewModel().setPreviousButtonVisible(hasPreviousState)
    if (!hasNextState) {
      getStateButtonViewModel().setObservableInteractionId(interactionId)
      // TODO(#163): This function controls whether the "Submit" button should be displayed or not.
      //  Remove this function in final implementation and control this whenever user selects some option in
      //  MultipleChoiceInput or InputSelectionInput. For now this is `true` because we do not have a mechanism to work
      //  with MultipleChoiceInput or InputSelectionInput, which will eventually be responsible for controlling this.
      getStateButtonViewModel().optionSelected(true)
    } else {
      if (currentAnswerOutcome != null && !currentAnswerOutcome!!.sameState) {
        getStateButtonViewModel().clearObservableInteractionId()
        getStateButtonViewModel().setObservableInteractionId(CONTINUE)
      } else {
        getStateButtonViewModel().clearObservableInteractionId()
        getStateButtonViewModel().setNextButtonVisible(hasNextState)
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
      Log.d("StateFragment", "hasFeedback: " + it.hasFeedback())
      if (it.hasFeedback()) {
        addFeedbackItem(it.feedback)
      }

      if (currentAnswerOutcome != null && !currentAnswerOutcome!!.sameState && !completedStateNameList.contains(
          currentEphemeralState.get()!!.state.name
        )
      ) {
        completedStateNameList.add(currentEphemeralState.get()!!.state.name)
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

    if (currentAnswerOutcome == null || currentAnswerOutcome!!.sameState) {
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

  private fun moveToNextState() {
    itemList.clear()
    currentAnswerOutcome = null
    explorationProgressController.moveToNextState()
  }

  override fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  override fun onNextButtonClicked() {
    moveToNextState()
  }

  private fun createContinueButtonAnswer(): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER).build()
  }

  private fun addLearnerAnswerItem(answerInteractionObject: InteractionObject) {
    Log.d("StateFragment", "addLearnerAnswerItem")
    val interactionReadOnlyViewModel = InteractionReadOnlyViewModel()
    var htmlString = ""
    when (currentEphemeralState.get()!!.state.interaction.id) {
      NUMERIC_INPUT -> {
        htmlString = answerInteractionObject.real.toString()
      }
      TEXT_INPUT -> {
        htmlString = answerInteractionObject.normalizedString
      }
      MULTIPLE_CHOICE_INPUT -> {
        htmlString = answerInteractionObject.normalizedString
      }
      CONTINUE -> {
        htmlString = answerInteractionObject.normalizedString
      }
    }
    interactionReadOnlyViewModel.htmlContent = htmlString
    if (htmlString.isNotEmpty()) {
      itemList.add(interactionReadOnlyViewModel)
      stateAdapter.notifyDataSetChanged()
    }
  }

  private fun addFeedbackItem(feedback: SubtitledHtml) {
    Log.d("StateFragment", "addFeedbackItem")
    val feedbackViewModel = ContentViewModel()
    var feedbackContent = ""
    if (feedback.contentId != "") {
      feedbackViewModel.contentId = feedback.contentId
    } else {
      feedbackViewModel.contentId = "feedback"
    }
    feedbackContent = feedback.html
    if (feedbackContent.isNotEmpty()) {
      feedbackViewModel.htmlContent = feedbackContent
      itemList.add(feedbackViewModel)
      stateAdapter.notifyDataSetChanged()
    }
  }
}
