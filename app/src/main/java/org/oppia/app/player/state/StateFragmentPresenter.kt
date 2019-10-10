package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.InteractionObject
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
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

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) {
  private val stateViewModel = viewModelProvider.getForFragment(fragment, StateViewModel::class.java)

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

  private fun controlButtonVisibility(interactionId: String, hasPreviousState: Boolean, hasNextState: Boolean) {
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
   * Whenever the current state changes it will automatically get called and therefore their is no need to call this separately.
   * This function currently provides important information to decide which button we should display.
   */
  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(fragment, Observer<EphemeralState> { result ->
      logger.d("StateFragment", "stateName: ${result.state.name}")
      val interactionId = result.state.interaction.id
      val hasPreviousState = result.hasPreviousState
      val hasNextState = result.completedState.answerCount > 0

      controlButtonVisibility(interactionId, hasPreviousState, hasNextState)
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
   *  this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToAnswerOutcome(answerOutcomeLiveData: LiveData<AnswerOutcome>) {
    answerOutcomeLiveData.observe(fragment, Observer<AnswerOutcome> { result ->
      logger.d("StateFragment", "subscribeToAnswerOutcome: ${result.stateName}")
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
  //  meaning we have to submit answer even when user clicks on "Continue".
  fun continueButtonClicked(v: View) {
    val interactionObject = InteractionObject.newBuilder().setNormalizedString("Please continue.").build()
    val answerOutcomeLiveData = getAnswerOutcome(explorationProgressController.submitAnswer(interactionObject))
    subscribeToAnswerOutcome(answerOutcomeLiveData)

    explorationProgressController.moveToNextState()
  }

  fun submitButtonClicked(v: View) {
    // NB: This value needs to be fetched from other interactions in #150, #151, #152, #153, #154, #155.
    val learnerResponse = 0
    val interactionObject = InteractionObject.newBuilder().setNonNegativeInt(learnerResponse).build()
    val answerOutcomeLiveData = getAnswerOutcome(explorationProgressController.submitAnswer(interactionObject))
    subscribeToAnswerOutcome(answerOutcomeLiveData)
  }

  fun nextButtonClicked(v: View) {
    explorationProgressController.moveToNextState()
  }

  fun previousButtonClicked(v: View) {
    explorationProgressController.moveToPreviousState()
  }
}
