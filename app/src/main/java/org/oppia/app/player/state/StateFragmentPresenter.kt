package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject

private const val CONTINUE = "Continue"
private const val END_EXPLORATION = "EndExploration"
private const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
private const val ITEM_SELECT_INPUT = "ItemSelectionInput"
private const val TEXT_INPUT = "TextInput"
private const val FRACTION_INPUT = "FractionInput"
private const val NUMERIC_INPUT = "NumericInput"
private const val NUMERIC_WITH_UNITS = "NumberWithUnits"
private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

// TODO(#163): Remove all loggers.

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val cellularDialogController: CellularDialogController,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val fragment: Fragment,
  private val logger: Logger,
  private val viewModelProvider: ViewModelProvider<StateViewModel>
) {
  private val stateViewModel = viewModelProvider.getForFragment(fragment, StateViewModel::class.java)

  private val currentEphemeralState = ObservableField<EphemeralState>()

  private var showCellularDataDialog = true
  private var useCellularData = false

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    cellularDialogController.getCellularDataPreference()
      .observe(fragment, Observer<AsyncResult<CellularDataPreference>> {
        if (it.isSuccess()) {
          val prefs = it.getOrDefault(CellularDataPreference.getDefaultInstance())
          showCellularDataDialog = !(prefs.hideDialog)
          useCellularData = prefs.useCellularData
        }
      })

    val binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.let {
      it.stateFragment = fragment as StateFragment
      it.presenter = this
      it.viewModel = stateViewModel
    }

    subscribeToCurrentState()

    return binding.root
  }

  fun handleAudioClick() {
    if (showCellularDataDialog) {
      setAudioFragmentVisible(false)
      showCellularDataDialogFragment()
    } else {
      setAudioFragmentVisible(useCellularData)
    }
  }

  fun handleEnableAudio(saveUserChoice: Boolean) {
    setAudioFragmentVisible(true)
    if (saveUserChoice)
      cellularDialogController.setAlwaysUseCellularDataPreference()
  }

  fun handleDisableAudio(saveUserChoice: Boolean) {
    if (saveUserChoice)
      cellularDialogController.setNeverUseCellularDataPreference()
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularDataDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  fun setAudioFragmentVisible(isVisible: Boolean) {
    stateViewModel.setAudioFragmentVisible(isVisible)
  }

  private fun updateNavigationButtonVisibility(interactionId: String, hasPreviousState: Boolean, hasNextState: Boolean) {
    logger.d("StateFragment", "interactionId: $interactionId")
    logger.d("StateFragment", "hasPreviousState: $hasPreviousState")
    logger.d("StateFragment", "hasNextState: $hasNextState")
    stateViewModel.hideAllButtons()
    stateViewModel.setPreviousButtonVisible(hasPreviousState)
    if (!hasNextState) {
      when (interactionId) {
        CONTINUE -> stateViewModel.setContinueButtonVisible(true)
        END_EXPLORATION -> stateViewModel.setEndExplorationButtonVisible(true)
        FRACTION_INPUT -> stateViewModel.setSubmitButtonVisible(true)
        ITEM_SELECT_INPUT -> stateViewModel.setSubmitButtonVisible(true)
        MULTIPLE_CHOICE_INPUT -> stateViewModel.setSubmitButtonVisible(true)
        NUMERIC_INPUT -> stateViewModel.setSubmitButtonVisible(true)
        NUMERIC_WITH_UNITS -> stateViewModel.setSubmitButtonVisible(true)
        TEXT_INPUT -> stateViewModel.setSubmitButtonVisible(true)
      }
    } else {
      stateViewModel.setNextButtonVisible(hasNextState)
    }
  }

  /**
   * This function subscribes to current state of the exploration.
   * Whenever the current state changes it will automatically get called and therefore there is no need to call this separately.
   * this function currently provides important information to decide which button we should display.
   */
  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment, Observer<EphemeralState> { it ->
      currentEphemeralState.set(it)
      logger.d("StateFragment", "stateName: ${it.state.name}")
      val interactionId = it.state.interaction.id
      val hasPreviousState = it.hasPreviousState
      val hasNextState = it.completedState.answerCount > 0
      updateNavigationButtonVisibility(interactionId, hasPreviousState, hasNextState)
    })
  }

  /** Helper for subscribeToCurrentState. */
  private val ephemeralStateLiveData: LiveData<EphemeralState> by lazy {
    getEphemeralState()
  }

  /** Helper for subscribeToCurrentState. */
  private fun getEphemeralState(): LiveData<EphemeralState> {
    return Transformations.map(explorationProgressController.getCurrentState(), ::processCurrentState)
  }

  /** Helper for subscribeToCurrentState. */
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
      explorationProgressController.moveToNextState()
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

  // NB: Any interaction will lead to answer submission,
  //  meaning we have to submit answer before we can move to next state.
  fun continueButtonClicked(v: View) {
    submitContinueButtonAnswer()
  }

  fun submitButtonClicked(v: View) {
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

    when (currentEphemeralState.get()!!.state.interaction.id) {
      FRACTION_INPUT -> submitFractionInputAnswer(Fraction.getDefaultInstance())
      ITEM_SELECT_INPUT -> submitMultipleChoiceAnswer(0)
      MULTIPLE_CHOICE_INPUT -> submitMultipleChoiceAnswer(stateWelcomeAnswer)
      NUMERIC_INPUT -> submitNumericInputAnswer(stateNumericInputAnswer.toDouble())
      NUMERIC_WITH_UNITS -> submitNumericWithUnitsInputAnswer(NumberWithUnits.getDefaultInstance())
      TEXT_INPUT -> submitTextInputAnswer(stateWhatLanguageAnswer)
    }
  }

  fun nextButtonClicked(v: View) {
    moveToNextState()
  }

  fun previousButtonClicked(v: View) {
    moveToPreviousState()
  }

  fun endExplorationButtonClicked(v: View) {
    endExploration()
  }

  fun learnAgainButtonClicked(v: View) {
  }

  private fun submitContinueButtonAnswer() {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(createContinueButtonAnswer()))
  }

  private fun submitFractionInputAnswer(fractionAnswer: Fraction) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(createFractionInputAnswer(fractionAnswer)))
  }

  private fun submitMultipleChoiceAnswer(choiceIndex: Int) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(createMultipleChoiceAnswer(choiceIndex)))
  }

  private fun submitNumericInputAnswer(numericAnswer: Double) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(createNumericInputAnswer(numericAnswer)))
  }

  private fun submitNumericWithUnitsInputAnswer(numberWithUnits: NumberWithUnits) {
    subscribeToAnswerOutcome(
      explorationProgressController.submitAnswer(
        createNumericWithUnitsInputAnswer(
          numberWithUnits
        )
      )
    )
  }

  private fun submitTextInputAnswer(textAnswer: String) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(createTextInputAnswer(textAnswer)))
  }

  private fun createContinueButtonAnswer() = createTextInputAnswer(DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER)

  private fun createFractionInputAnswer(fractionAnswer: Fraction): InteractionObject {
    return InteractionObject.newBuilder().setFraction(fractionAnswer).build()
  }

  private fun createMultipleChoiceAnswer(choiceIndex: Int): InteractionObject {
    return InteractionObject.newBuilder().setNonNegativeInt(choiceIndex).build()
  }

  private fun createNumericInputAnswer(numericAnswer: Double): InteractionObject {
    return InteractionObject.newBuilder().setReal(numericAnswer).build()
  }

  private fun createNumericWithUnitsInputAnswer(numberWithUnits: NumberWithUnits): InteractionObject {
    return InteractionObject.newBuilder().setNumberWithUnits(numberWithUnits).build()
  }

  private fun createTextInputAnswer(textAnswer: String): InteractionObject {
    return InteractionObject.newBuilder().setNormalizedString(textAnswer).build()
  }

  private fun moveToNextState() {
    explorationProgressController.moveToNextState()
  }

  private fun moveToPreviousState() {
    explorationProgressController.moveToPreviousState()
  }

  private fun endExploration() {
    explorationDataController.stopPlayingExploration()
    (activity as ExplorationActivity).finish()
  }
}
