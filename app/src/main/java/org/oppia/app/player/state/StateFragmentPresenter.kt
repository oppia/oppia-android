package org.oppia.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.CellularDataDialogFragment
import org.oppia.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularDialogController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.Logger
import org.oppia.util.parser.ExplorationHtmlParserEntityType
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
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory
) {
  private var showCellularDataDialog = true
  private var useCellularData = false
  private lateinit var explorationId: String
  private lateinit var currentStateName: String
  private lateinit var binding: StateFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private lateinit var viewModel: StateViewModel
  private lateinit var recyclerViewAssembler: StatePlayerRecyclerViewAssembler
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
    recyclerViewAssembler = createRecyclerViewAssembler(
      assemblerBuilderFactory.create(resourceBucketName, entityType),
      binding.congratulationsTextView
    )

    val stateRecyclerViewAdapter = recyclerViewAssembler.adapter
    binding.stateRecyclerView.apply {
      adapter = stateRecyclerViewAdapter
    }
    recyclerViewAdapter = stateRecyclerViewAdapter
    viewModel = getStateViewModel()
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = this.viewModel
    }

    binding.stateRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
//      if (bottom < oldBottom) {
//        binding.stateRecyclerView.postDelayed({
//          binding.stateRecyclerView.scrollToPosition(stateRecyclerViewAdapter.itemCount - 1)
//        }, 100)
//      }
    }
    subscribeToCurrentState()

    return binding.root
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

  fun onContinueButtonClicked() {
    hideKeyboard()
    moveToNextState()
  }

  fun onNextButtonClicked() = moveToNextState()

  fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    (activity as StopStatePlayingSessionListener).stopSession()
  }

  fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(viewModel.getPendingAnswer(recyclerViewAssembler))
  }

  fun onResponsesHeaderClicked() {
    recyclerViewAssembler.togglePreviousAnswers(viewModel.itemList)
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

  fun handleKeyboardAction() = onSubmitButtonClicked()

  private fun createRecyclerViewAssembler(
    builder: StatePlayerRecyclerViewAssembler.Builder,
    congratulationsTextView: TextView
  ): StatePlayerRecyclerViewAssembler {
    return builder.addContentSupport()
      .addFeedbackSupport()
      .addInteractionSupport()
      .addPastAnswersSupport()
      .addWrongAnswerCollapsingSupport()
      .addBackwardNavigationSupport()
      .addForwardNavigationSupport()
      .addReturnToTopicSupport()
      .addCongratulationsForCorrectAnswers(congratulationsTextView)
      .addLoadingInterstitials()
      .build()
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
    val scrollToTop = ::currentStateName.isInitialized && currentStateName != ephemeralState.state.name
    currentStateName = ephemeralState.state.name

    recyclerViewAssembler.compute(ephemeralState, explorationId, viewModel.itemList)

    if (scrollToTop) {
      (binding.stateRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 200)
    }
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
      } else if (result.labelledAsCorrectAnswer) {
        recyclerViewAssembler.showCongratulationMessageOnCorrectAnswer()
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

  private fun handleSubmitAnswer(answer: UserAnswer) {
    (binding.stateRecyclerView.layoutManager as LinearLayoutManager).scrollToPosition(recyclerViewAdapter.itemCount - 1)
    subscribeToAnswerOutcome(recyclerViewAssembler.submitAnswer(answer, viewModel.itemList, explorationId))
  }

  private fun moveToNextState() {
    explorationProgressController.moveToNextState().observe(fragment, Observer<AsyncResult<Any?>> {
      recyclerViewAssembler.collapsePreviousResponses()
    })
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }
}
