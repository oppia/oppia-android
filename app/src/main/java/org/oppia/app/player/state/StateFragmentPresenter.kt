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
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Fraction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.NumberWithUnits
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.exploration.EXPLORATION_ACTIVITY_TOPIC_ID_ARGUMENT_KEY
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateButtonViewModel
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

private const val CONTINUE = "Continue"
private const val END_EXPLORATION = "EndExploration"
private const val LEARN_AGAIN = "LearnAgain"
private const val MULTIPLE_CHOICE_INPUT = "MultipleChoiceInput"
private const val ITEM_SELECT_INPUT = "ItemSelectionInput"
private const val TEXT_INPUT = "TextInput"
private const val FRACTION_INPUT = "FractionInput"
private const val NUMERIC_INPUT = "NumericInput"
private const val NUMERIC_WITH_UNITS = "NumberWithUnits"

// For context:
// https://github.com/oppia/oppia/blob/37285a/extensions/interactions/Continue/directives/oppia-interactive-continue.directive.ts.
private const val DEFAULT_CONTINUE_INTERACTION_TEXT_ANSWER = "Please continue."

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val cellularDialogController: CellularDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val numericInputInteractionViewModelProvider: ViewModelProvider<NumericInputInteractionViewModel>,
  private val stateButtonViewModelProvider: ViewModelProvider<StateButtonViewModel>,
  private val contentViewModelProvider: ViewModelProvider<ContentViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger
) : InteractionListener {

  private val currentEphemeralState = ObservableField<EphemeralState>()

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
      itemList.clear()
      currentEphemeralState.set(result)
      if (result.state.hasContent()) {
        if (result.state.content.contentId != "") {
          getContentViewModel().contentId = result.state.content.contentId
        } else {
          getContentViewModel().contentId = "content"
        }
        getContentViewModel().htmlContent = result.state.content.html
        itemList.add(getContentViewModel())
        stateAdapter.notifyDataSetChanged()
      }

      val interactionId = result.state.interaction.id
      val hasPreviousState = result.hasPreviousState
      val hasNextState = result.completedState.answerCount > 0

      when (interactionId) {
        NUMERIC_INPUT -> {
          getNumericInputInteractionViewModel().placeholder =
            result.state.interaction.customizationArgsMap["placeholder"]!!.normalizedString
          itemList.add(getNumericInputInteractionViewModel())
          stateAdapter.notifyDataSetChanged()
        }
        TEXT_INPUT -> {
          getNumericInputInteractionViewModel().placeholder =
            result.state.interaction.customizationArgsMap["placeholder"]!!.normalizedString
          itemList.add(getNumericInputInteractionViewModel())
          stateAdapter.notifyDataSetChanged()
        }
      }

      updateNavigationButtonVisibility(interactionId, hasPreviousState, hasNextState)
    })
  }

  private fun updateNavigationButtonVisibility(
    interactionId: String,
    hasPreviousState: Boolean,
    hasNextState: Boolean
  ) {
    logger.d("StateFragment", "interactionId: $interactionId")
    logger.d("StateFragment", "hasPreviousState: $hasPreviousState")
    logger.d("StateFragment", "hasNextState: $hasNextState")
    getStateButtonViewModel().setPreviousButtonVisible(hasPreviousState)
    if (!hasNextState) {
      getStateButtonViewModel().setObservableInteractionId(interactionId)
      // TODO(#163): This function controls whether the "Submit" button should be displayed or not.
      //  Remove this function in final implementation and control this whenever user selects some option in
      //  MultipleChoiceInput or InputSelectionInput. For now this is `true` because we do not have a mechanism to work
      //  with MultipleChoiceInput or InputSelectionInput, which will eventually be responsible for controlling this.
      getStateButtonViewModel().optionSelected(true)
    } else {
      getStateButtonViewModel().clearObservableInteractionId()
      getStateButtonViewModel().setNextButtonVisible(hasNextState)
    }
    itemList.add(getStateButtonViewModel())
    stateAdapter.notifyDataSetChanged()
  }

  private fun getStateButtonViewModel(): StateButtonViewModel {
    return stateButtonViewModelProvider.getForFragment(fragment, StateButtonViewModel::class.java)
  }

  private fun getContentViewModel(): ContentViewModel {
    return contentViewModelProvider.getForFragment(fragment, ContentViewModel::class.java)
  }

  private fun getNumericInputInteractionViewModel(): NumericInputInteractionViewModel {
    return numericInputInteractionViewModelProvider.getForFragment(
      fragment,
      NumericInputInteractionViewModel::class.java
    )
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

  private fun endExploration() {
    explorationDataController.stopPlayingExploration()
    (activity as ExplorationActivity).finish()
  }

  override fun onInteractionButtonClicked() {
    Log.d("StateFragment", "interactionButtonClicked")
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
      CONTINUE -> submitContinueButtonAnswer()
      END_EXPLORATION -> endExploration()
      FRACTION_INPUT -> submitFractionInputAnswer(Fraction.getDefaultInstance())
      ITEM_SELECT_INPUT -> submitMultipleChoiceAnswer(0)
      MULTIPLE_CHOICE_INPUT -> submitMultipleChoiceAnswer(stateWelcomeAnswer)
      NUMERIC_INPUT -> submitNumericInputAnswer(stateNumericInputAnswer.toDouble())
      NUMERIC_WITH_UNITS -> submitNumericWithUnitsInputAnswer(NumberWithUnits.getDefaultInstance())
      TEXT_INPUT -> submitTextInputAnswer(stateWhatLanguageAnswer)
    }
  }

  override fun onPreviousButtonClicked() {
    Log.d("StateFragment", "previousButtonClicked")
    explorationProgressController.moveToPreviousState()
  }

  override fun onNextButtonClicked() {
    Log.d("StateFragment", "nextButtonClicked")
    explorationProgressController.moveToNextState()
  }

}
