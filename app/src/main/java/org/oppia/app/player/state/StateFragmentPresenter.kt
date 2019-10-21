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
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.InteractionObjectOrBuilder
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
import org.oppia.app.player.state.listener.ButtonInteractionListener
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
  private val stateButtonViewModelProvider: ViewModelProvider<StateButtonViewModel>,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) : ButtonInteractionListener {

  private var showCellularDataDialog = true
  private var useCellularData = false
  private var explorationId: String? = null

  private val oldStateNameList: ArrayList<String> = ArrayList()

  private val currentEphemeralState = ObservableField<EphemeralState>(EphemeralState.getDefaultInstance())
  private var currentAnswerOutcome: AnswerOutcome? = null

  private val itemList: MutableList<Any> = ArrayList()

  private var hasGeneralContinueButton: Boolean = false

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

    stateAdapter = StateAdapter(itemList, this as ButtonInteractionListener)

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

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(TAG_AUDIO_FRAGMENT)
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

        updateDummyStateName()

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
      val interactionObject: InteractionObject = getDummyInteractionObject()
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

  private fun getStateButtonViewModel(): StateButtonViewModel {
    return stateButtonViewModelProvider.getForFragment(fragment, StateButtonViewModel::class.java)
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }

  // TODO(#163): Remove this function, this is just for dummy testing purposes.
  private fun updateDummyStateName(){
    getStateViewModel().setStateName(currentEphemeralState.get()!!.state.name)
  }

  // TODO(#163): Remove this function and fetch this InteractionObject from [StateAdapter].
  private fun getDummyInteractionObject(): InteractionObject {
    val interactionObjectBuilder: InteractionObject.Builder = InteractionObject.newBuilder()
    when (currentEphemeralState.get()!!.state.name) {
      "Welcome!" -> interactionObjectBuilder.nonNegativeInt = 0
      "What language" -> interactionObjectBuilder.normalizedString = "finnish"
      "Things you can do" -> createContinueButtonAnswer()
      "Numeric input" -> interactionObjectBuilder.real = 121.0
      else -> InteractionObject.getDefaultInstance()
    }
    return interactionObjectBuilder.build()
  }
}
