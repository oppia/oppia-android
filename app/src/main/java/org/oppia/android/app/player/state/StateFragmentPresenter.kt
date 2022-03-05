package org.oppia.android.app.player.state

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
import nl.dionsegijn.konfetti.KonfettiView
import org.oppia.android.R
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AnswerOutcome
import org.oppia.android.app.model.CheckpointState
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.State
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.audio.AudioFragment
import org.oppia.android.app.player.audio.AudioUiManager
import org.oppia.android.app.player.state.ConfettiConfig.LARGE_CONFETTI_BURST
import org.oppia.android.app.player.state.ConfettiConfig.MEDIUM_CONFETTI_BURST
import org.oppia.android.app.player.state.ConfettiConfig.MINI_CONFETTI_BURST
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionWithSavedProgressListener
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment.Companion.CONCEPT_CARD_DIALOG_FRAGMENT_TAG
import org.oppia.android.app.utility.SplitScreenManager
import org.oppia.android.app.viewmodel.ViewModelProvider
import org.oppia.android.databinding.StateFragmentBinding
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.gcsresource.DefaultResourceBucketName
import org.oppia.android.util.parser.html.ExplorationHtmlParserEntityType
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

const val STATE_FRAGMENT_PROFILE_ID_ARGUMENT_KEY =
  "StateFragmentPresenter.state_fragment_profile_id"
const val STATE_FRAGMENT_TOPIC_ID_ARGUMENT_KEY = "StateFragmentPresenter.state_fragment_topic_id"
const val STATE_FRAGMENT_STORY_ID_ARGUMENT_KEY = "StateFragmentPresenter.state_fragment_story_id"
const val STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY =
  "StateFragmentPresenter.state_fragment_exploration_id"
private const val TAG_AUDIO_FRAGMENT = "AUDIO_FRAGMENT"

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  @ExplorationHtmlParserEntityType private val entityType: String,
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val context: Context,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationProgressController: ExplorationProgressController,
  private val storyProgressController: StoryProgressController,
  private val oppiaLogger: OppiaLogger,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory,
  private val splitScreenManager: SplitScreenManager,
  private val oppiaClock: OppiaClock
) {

  private val routeToHintsAndSolutionListener = activity as RouteToHintsAndSolutionListener
  private val hasConversationView = true

  private lateinit var currentState: State
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var currentStateName: String
  private lateinit var binding: StateFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private lateinit var helpIndex: HelpIndex

  private val viewModel: StateViewModel by lazy {
    getStateViewModel()
  }
  private lateinit var recyclerViewAssembler: StatePlayerRecyclerViewAssembler
  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState().toLiveData()
  }

  private var explorationCheckpointState: CheckpointState = CheckpointState.CHECKPOINT_UNSAVED

  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int,
    topicId: String,
    storyId: String,
    explorationId: String
  ): View? {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId

    binding = StateFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    recyclerViewAssembler = createRecyclerViewAssembler(
      assemblerBuilderFactory.create(resourceBucketName, entityType, profileId),
      binding.congratulationsTextView,
      binding.congratulationsTextConfettiView,
      binding.fullScreenConfettiView
    )

    val stateRecyclerViewAdapter = recyclerViewAssembler.adapter
    val rhsStateRecyclerViewAdapter = recyclerViewAssembler.rhsAdapter
    binding.stateRecyclerView.apply {
      adapter = stateRecyclerViewAdapter
    }
    binding.extraInteractionRecyclerView.apply {
      adapter = rhsStateRecyclerViewAdapter
    }
    recyclerViewAdapter = stateRecyclerViewAdapter
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = this.viewModel
    }

    binding.stateRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      if (bottom < oldBottom) {
        binding.stateRecyclerView.postDelayed(
          {
            binding.stateRecyclerView.scrollToPosition(
              stateRecyclerViewAdapter.itemCount - 1
            )
          },
          100
        )
      }
    }

    binding.hintsAndSolutionFragmentContainer.setOnClickListener {
      routeToHintsAndSolutionListener.routeToHintsAndSolution(
        this.explorationId,
        helpIndex
      )
    }

    subscribeToCurrentState()
    return binding.root
  }

  fun handleAnswerReadyForSubmission(answer: UserAnswer) {
    // An interaction has indicated that an answer is ready for submission.
    handleSubmitAnswer(answer)
  }

  fun onContinueButtonClicked() {
    viewModel.setHintBulbVisibility(false)
    hideKeyboard()
    moveToNextState()
  }

  fun onNextButtonClicked() = moveToNextState()

  fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState().toLiveData()
  }

  fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    markExplorationCompleted()
    (activity as StopStatePlayingSessionWithSavedProgressListener)
      .deleteCurrentProgressAndStopSession()
  }

  private fun showOrHideAudioByState(state: State) {
    if (state.recordedVoiceoversCount == 0) {
      (activity as AudioButtonListener).hideAudioButton()
    } else {
      (activity as AudioButtonListener).showAudioButton()
    }
  }

  fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(viewModel.getPendingAnswer(recyclerViewAssembler::getPendingAnswerHandler))
  }

  fun onResponsesHeaderClicked() {
    recyclerViewAssembler.togglePreviousAnswers(viewModel.itemList)
    recyclerViewAssembler.adapter.notifyDataSetChanged()
  }

  fun handleAudioClick() = recyclerViewAssembler.toggleAudioPlaybackState()

  fun handleKeyboardAction() {
    hideKeyboard()
    if (viewModel.getCanSubmitAnswer().get() == true) {
      handleSubmitAnswer(viewModel.getPendingAnswer(recyclerViewAssembler::getPendingAnswerHandler))
    }
  }

  fun onHintAvailable(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean) {
    this.helpIndex = helpIndex
    showHintsAndSolutions(helpIndex, isCurrentStatePendingState)
  }

  private fun createRecyclerViewAssembler(
    builder: StatePlayerRecyclerViewAssembler.Builder,
    congratulationsTextView: TextView,
    congratulationsTextConfettiView: KonfettiView,
    fullScreenConfettiView: KonfettiView
  ): StatePlayerRecyclerViewAssembler {
    val isTablet = context.resources.getBoolean(R.bool.isTablet)
    return builder
      .hasConversationView(hasConversationView)
      .addContentSupport()
      .addFeedbackSupport()
      .addInteractionSupport(viewModel.getCanSubmitAnswer())
      .addPastAnswersSupport()
      .addWrongAnswerCollapsingSupport()
      .addBackwardNavigationSupport()
      .addForwardNavigationSupport()
      .addReturnToTopicSupport()
      .addCelebrationForCorrectAnswers(
        congratulationsTextView,
        congratulationsTextConfettiView,
        MINI_CONFETTI_BURST
      )
      .addCelebrationForEndOfSession(
        fullScreenConfettiView,
        if (isTablet) LARGE_CONFETTI_BURST else MEDIUM_CONFETTI_BURST
      )
      .addHintsAndSolutionsSupport()
      .addAudioVoiceoverSupport(
        explorationId, viewModel.currentStateName, viewModel.isAudioBarVisible,
        this::getAudioUiManager
      )
      .addConceptCardSupport()
      .build()
  }

  fun revealHint(hintIndex: Int) {
    subscribeToHintSolution(explorationProgressController.submitHintIsRevealed(hintIndex).toLiveData())
  }

  fun revealSolution() {
    subscribeToHintSolution(explorationProgressController.submitSolutionIsRevealed().toLiveData())
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(TAG_AUDIO_FRAGMENT)
  }

  private fun getAudioUiManager(): AudioUiManager? {
    if (getAudioFragment() == null) {
      val audioFragment: AudioFragment = AudioFragment.newInstance(profileId.internalId)
      fragment.childFragmentManager.beginTransaction()
        .add(R.id.audio_fragment_placeholder, audioFragment, TAG_AUDIO_FRAGMENT).commitNow()
    }
    return getAudioFragment() as? AudioUiManager
  }

  private fun subscribeToCurrentState() {
    ephemeralStateLiveData.observe(
      fragment,
      { result ->
        processEphemeralStateResult(result)
      }
    )
  }

  private fun processEphemeralStateResult(result: AsyncResult<EphemeralState>) {
    when (result) {
      is AsyncResult.Failure ->
        oppiaLogger.e("StateFragment", "Failed to retrieve ephemeral state", result.error)
      is AsyncResult.Pending -> {} // Display nothing until a valid result is available.
      is AsyncResult.Success -> processEphemeralState(result.value)
    }
  }

  private fun processEphemeralState(ephemeralState: EphemeralState) {
    explorationCheckpointState = ephemeralState.checkpointState
    val shouldSplit = splitScreenManager.shouldSplitScreen(ephemeralState.state.interaction.id)
    if (shouldSplit) {
      viewModel.isSplitView.set(true)
      viewModel.centerGuidelinePercentage.set(0.5f)
    } else {
      viewModel.isSplitView.set(false)
      viewModel.centerGuidelinePercentage.set(1f)
    }

    val isInNewState =
      ::currentStateName.isInitialized && currentStateName != ephemeralState.state.name

    currentState = ephemeralState.state
    currentStateName = ephemeralState.state.name

    showOrHideAudioByState(ephemeralState.state)

    val dataPair = recyclerViewAssembler.compute(
      ephemeralState,
      explorationId,
      shouldSplit
    )

    viewModel.itemList.clear()
    viewModel.itemList += dataPair.first
    viewModel.rightItemList.clear()
    viewModel.rightItemList += dataPair.second

    if (isInNewState) {
      (binding.stateRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
        0,
        200
      )
    }
  }

  /** Subscribes to the result of requesting to show a hint or solution. */
  private fun subscribeToHintSolution(resultLiveData: LiveData<AsyncResult<Any?>>) {
    resultLiveData.observe(
      fragment,
      { result ->
        if (result is AsyncResult.Failure) {
          oppiaLogger.e("StateFragment", "Failed to retrieve hint/solution", result.error)
        } else {
          // If the hint/solution, was revealed remove dot and radar.
          viewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
      }
    )
  }

  /**
   * This function listens to the result of submitAnswer.
   * Whenever an answer is submitted using ExplorationProgressController.submitAnswer function,
   * this function will wait for the response from that function and based on which we can move to
   * next state.
   */
  private fun subscribeToAnswerOutcome(
    answerOutcomeResultLiveData: LiveData<AsyncResult<AnswerOutcome>>
  ) {
    val answerOutcomeLiveData = getAnswerOutcome(answerOutcomeResultLiveData)
    answerOutcomeLiveData.observe(
      fragment,
      Observer { result ->
        // If the answer was submitted on behalf of the Continue interaction, automatically continue
        // to the next state.
        if (result.state.interaction.id == "Continue") {
          moveToNextState()
        } else {
          if (result.labelledAsCorrectAnswer) {
            recyclerViewAssembler.showCelebrationOnCorrectAnswer(result.feedback)
          } else {
            viewModel.setCanSubmitAnswer(canSubmitAnswer = false)
          }
          recyclerViewAssembler.readOutAnswerFeedback(result.feedback)
        }
      }
    )
  }

  /** Helper for subscribeToAnswerOutcome. */
  private fun getAnswerOutcome(
    answerOutcome: LiveData<AsyncResult<AnswerOutcome>>
  ): LiveData<AnswerOutcome> {
    return Transformations.map(answerOutcome, ::processAnswerOutcome)
  }

  /** Helper for [subscribeToAnswerOutcome]. */
  private fun processAnswerOutcome(
    ephemeralStateResult: AsyncResult<AnswerOutcome>
  ): AnswerOutcome {
    return when (ephemeralStateResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e(
          "StateFragment", "Failed to retrieve answer outcome", ephemeralStateResult.error
        )
        AnswerOutcome.getDefaultInstance()
      }
      is AsyncResult.Pending -> AnswerOutcome.getDefaultInstance()
      is AsyncResult.Success -> ephemeralStateResult.value
    }
  }

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer).toLiveData())
  }

  fun dismissConceptCard() {
    fragment.childFragmentManager.findFragmentByTag(
      CONCEPT_CARD_DIALOG_FRAGMENT_TAG
    )?.let { dialogFragment ->
      fragment.childFragmentManager.beginTransaction().remove(dialogFragment).commitNow()
    }
  }

  private fun moveToNextState() {
    viewModel.setCanSubmitAnswer(canSubmitAnswer = false)
    explorationProgressController.moveToNextState().toLiveData().observe(
      fragment,
      Observer {
        recyclerViewAssembler.collapsePreviousResponses()
      }
    )
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager =
      activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
      fragment.view!!.windowToken,
      InputMethodManager.SHOW_FORCED
    )
  }

  fun setAudioBarVisibility(visibility: Boolean) =
    getStateViewModel().setAudioBarVisibility(visibility)

  fun scrollToTop() {
    binding.stateRecyclerView.smoothScrollToPosition(0)
  }

  /** Updates submit button UI as active if pendingAnswerError null else inactive. */
  fun updateSubmitButton(pendingAnswerError: String?, inputAnswerAvailable: Boolean) {
    if (inputAnswerAvailable) {
      viewModel.setCanSubmitAnswer(pendingAnswerError == null)
    } else {
      viewModel.setCanSubmitAnswer(canSubmitAnswer = false)
    }
  }

  /** Returns the checkpoint state for the current exploration. */
  fun getExplorationCheckpointState() = explorationCheckpointState

  private fun markExplorationCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId,
      topicId,
      storyId,
      explorationId,
      oppiaClock.getCurrentTimeMs()
    )
  }

  private fun showHintsAndSolutions(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean) {
    if (!isCurrentStatePendingState) {
      // If current state is not the pending top state, hide the hint bulb.
      viewModel.setHintOpenedAndUnRevealedVisibility(false)
      viewModel.setHintBulbVisibility(false)
    } else {
      when (helpIndex.indexTypeCase) {
        HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX -> {
          viewModel.setHintBulbVisibility(true)
          viewModel.setHintOpenedAndUnRevealedVisibility(true)
        }
        HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX -> {
          viewModel.setHintBulbVisibility(true)
          viewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
        HelpIndex.IndexTypeCase.SHOW_SOLUTION -> {
          viewModel.setHintBulbVisibility(true)
          viewModel.setHintOpenedAndUnRevealedVisibility(true)
        }
        HelpIndex.IndexTypeCase.EVERYTHING_REVEALED -> {
          viewModel.setHintOpenedAndUnRevealedVisibility(false)
          viewModel.setHintBulbVisibility(true)
        }
        else -> {
          viewModel.setHintOpenedAndUnRevealedVisibility(false)
          viewModel.setHintBulbVisibility(false)
        }
      }
    }
  }
}
