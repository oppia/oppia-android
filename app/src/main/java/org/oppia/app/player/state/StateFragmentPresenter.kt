package org.oppia.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.HelpIndex
import org.oppia.app.model.Hint
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Solution
import org.oppia.app.model.State
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.SplitScreenManager
import org.oppia.app.player.audio.AudioButtonListener
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.AudioUiManager
import org.oppia.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.app.utility.LifecycleSafeTimerFactory
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.domain.topic.StoryProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import java.util.*
import javax.inject.Inject

const val STATE_FRAGMENT_PROFILE_ID_ARGUMENT_KEY = "STATE_FRAGMENT_PROFILE_ID_ARGUMENT_KEY"
const val STATE_FRAGMENT_TOPIC_ID_ARGUMENT_KEY = "STATE_FRAGMENT_TOPIC_ID_ARGUMENT_KEY"
const val STATE_FRAGMENT_STORY_ID_ARGUMENT_KEY = "STATE_FRAGMENT_STORY_ID_ARGUMENT_KEY"
const val STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY = "STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY"
private const val TAG_AUDIO_FRAGMENT = "AUDIO_FRAGMENT"

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  @ExplorationHtmlParserEntityType private val entityType: String,
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationProgressController: ExplorationProgressController,
  private val storyProgressController: StoryProgressController,
  private val logger: ConsoleLogger,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory,
  private var lifecycleSafeTimerFactory: LifecycleSafeTimerFactory
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
  private lateinit var rhsInteractionsAdapter: RhsInteractionsAdapter
  private val splitScreenManager: SplitScreenManager = SplitScreenManager(activity)

  private val viewModel: StateViewModel by lazy {
    getStateViewModel()
  }
  private lateinit var recyclerViewAssembler: StatePlayerRecyclerViewAssembler
  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState()
  }

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
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = this.viewModel
    }

    binding.stateRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      if (bottom < oldBottom) {
        binding.stateRecyclerView.postDelayed(
          {
            binding.stateRecyclerView.scrollToPosition(stateRecyclerViewAdapter.itemCount - 1)
          },
          100
        )
      }
    }

    binding.hintsAndSolutionFragmentContainer.setOnClickListener {
      routeToHintsAndSolutionListener.routeToHintsAndSolution(
        this.explorationId,
        viewModel.newAvailableHintIndex,
        viewModel.allHintsExhausted
      )
    }

    subscribeToCurrentState()
    markExplorationAsRecentlyPlayed()
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
    explorationProgressController.moveToPreviousState()
  }

  fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    markExplorationCompleted()
    (activity as StopStatePlayingSessionListener).stopSession()
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
    handleSubmitAnswer(viewModel.getPendingAnswer(recyclerViewAssembler))
  }

  fun onResponsesHeaderClicked() {
    recyclerViewAssembler.togglePreviousAnswers(viewModel.itemList)
    recyclerViewAssembler.adapter.notifyDataSetChanged()
  }

  fun onHintAvailable(helpIndex: HelpIndex) {
    when (helpIndex.indexTypeCase) {
      HelpIndex.IndexTypeCase.HINT_INDEX, HelpIndex.IndexTypeCase.SHOW_SOLUTION -> {
        if (helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.HINT_INDEX) {
          viewModel.newAvailableHintIndex = helpIndex.hintIndex
        }
        viewModel.allHintsExhausted =
          helpIndex.indexTypeCase == HelpIndex.IndexTypeCase.SHOW_SOLUTION
        viewModel.setHintOpenedAndUnRevealedVisibility(true)
        viewModel.setHintBulbVisibility(true)
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

  fun handleAudioClick() = recyclerViewAssembler.toggleAudioPlaybackState()

  fun handleKeyboardAction() {
    hideKeyboard()
    if (viewModel.getCanSubmitAnswer().get() == true) {
      handleSubmitAnswer(viewModel.getPendingAnswer(recyclerViewAssembler))
    }
  }

  private fun createRecyclerViewAssembler(
    builder: StatePlayerRecyclerViewAssembler.Builder,
    congratulationsTextView: TextView
  ): StatePlayerRecyclerViewAssembler {
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
      .addCongratulationsForCorrectAnswers(congratulationsTextView)
      .addHintsAndSolutionsSupport()
      .addAudioVoiceoverSupport(
        explorationId, viewModel.currentStateName, viewModel.isAudioBarVisible,
        this::getAudioUiManager
      ).build()
  }

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    subscribeToHint(
      explorationProgressController.submitHintIsRevealed(
        currentState,
        saveUserChoice,
        hintIndex
      )
    )
  }

  fun revealSolution(saveUserChoice: Boolean) {
    subscribeToSolution(
      explorationProgressController.submitSolutionIsRevealed(
        currentState,
        saveUserChoice
      )
    )
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
      Observer { result ->
        processEphemeralStateResult(result)
      }
    )
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
    val shouldSplit = splitScreenManager.shouldSplit(ephemeralState.state.interaction.id)

    if (shouldSplit) {
      viewModel.shouldSplitView.set(true)
      binding.rhsStateRecyclerView?.visibility = View.VISIBLE
      val params = binding.centerGuideline?.layoutParams as ConstraintLayout.LayoutParams
      params.guidePercent = 0.5f
      binding.centerGuideline?.layoutParams = params
    } else {
      viewModel.shouldSplitView.set(false)
      binding.rhsStateRecyclerView?.visibility = View.GONE
      val params = binding.centerGuideline?.layoutParams as ConstraintLayout.LayoutParams
      params.guidePercent = 1f
      binding.centerGuideline?.layoutParams = params
    }

    val isInNewState =
      ::currentStateName.isInitialized && currentStateName != ephemeralState.state.name

    currentState = ephemeralState.state
    currentStateName = ephemeralState.state.name
    showOrHideAudioByState(ephemeralState.state)

    viewModel.itemList.clear()
    viewModel.itemList += recyclerViewAssembler.compute(
      ephemeralState,
      explorationId,
      shouldSplit
    ).first
    viewModel.rightItemList.clear()
    viewModel.rightItemList += recyclerViewAssembler.compute(
      ephemeralState,
      explorationId,
      shouldSplit
    ).second
    rhsInteractionsAdapter = RhsInteractionsAdapter(viewModel.rightItemList)
    binding.rhsStateRecyclerView?.adapter = rhsInteractionsAdapter

    if (isInNewState) {
      (binding.stateRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
        0,
        200
      )
    }
  }

  /**
   * This function listens to the result of RevealHint.
   * Whenever a hint is revealed using ExplorationProgressController.submitHintIsRevealed function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToHint(hintResultLiveData: LiveData<AsyncResult<Hint>>) {
    val hintLiveData = getHintIsRevealed(hintResultLiveData)
    hintLiveData.observe(
      fragment,
      Observer { result ->
        // If the hint was revealed remove dot and radar.
        if (result.hintIsRevealed) {
          viewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
      }
    )
  }

  /**
   * This function listens to the result of RevealSolution.
   * Whenever a hint is revealed using ExplorationProgressController.submitHintIsRevealed function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToSolution(solutionResultLiveData: LiveData<AsyncResult<Solution>>) {
    val solutionLiveData = getSolutionIsRevealed(solutionResultLiveData)
    solutionLiveData.observe(
      fragment,
      Observer { result ->
        // If the hint was revealed remove dot and radar.
        if (result.solutionIsRevealed) {
          viewModel.setHintOpenedAndUnRevealedVisibility(false)
        }
      }
    )
  }

  /**
   * This function listens to the result of submitAnswer.
   * Whenever an answer is submitted using ExplorationProgressController.submitAnswer function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToAnswerOutcome(
    answerOutcomeResultLiveData: LiveData<AsyncResult<AnswerOutcome>>
  ) {
    val answerOutcomeLiveData = getAnswerOutcome(answerOutcomeResultLiveData)
    answerOutcomeLiveData.observe(
      fragment,
      Observer { result ->
        // If the answer was submitted on behalf of the Continue interaction, automatically continue to the next state.
        if (result.state.interaction.id == "Continue") {
          recyclerViewAssembler.stopHintsFromShowing()
          viewModel.setHintBulbVisibility(false)
          moveToNextState()
        } else {
          if (result.labelledAsCorrectAnswer) {
            recyclerViewAssembler.stopHintsFromShowing()
            viewModel.setHintBulbVisibility(false)
            recyclerViewAssembler.showCongratulationMessageOnCorrectAnswer()
          } else {
            viewModel.setCanSubmitAnswer(canSubmitAnswer = false)
          }
          recyclerViewAssembler.readOutAnswerFeedback(result.feedback)
        }
      }
    )
  }

  /** Helper for [subscribeToSolution]. */
  private fun getSolutionIsRevealed(hint: LiveData<AsyncResult<Solution>>): LiveData<Solution> {
    return Transformations.map(hint, ::processSolution)
  }

  /** Helper for [subscribeToHint]. */
  private fun getHintIsRevealed(hint: LiveData<AsyncResult<Hint>>): LiveData<Hint> {
    return Transformations.map(hint, ::processHint)
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
    if (ephemeralStateResult.isFailure()) {
      logger.e(
        "StateFragment",
        "Failed to retrieve answer outcome",
        ephemeralStateResult.getErrorOrNull()!!
      )
    }
    return ephemeralStateResult.getOrDefault(AnswerOutcome.getDefaultInstance())
  }

  /** Helper for [subscribeToHint]. */
  private fun processHint(hintResult: AsyncResult<Hint>): Hint {
    if (hintResult.isFailure()) {
      logger.e(
        "StateFragment",
        "Failed to retrieve Hint",
        hintResult.getErrorOrNull()!!
      )
    }
    return hintResult.getOrDefault(Hint.getDefaultInstance())
  }

  /** Helper for [subscribeToSolution]. */
  private fun processSolution(solutionResult: AsyncResult<Solution>): Solution {
    if (solutionResult.isFailure()) {
      logger.e(
        "StateFragment",
        "Failed to retrieve Solution",
        solutionResult.getErrorOrNull()!!
      )
    }
    return solutionResult.getOrDefault(Solution.getDefaultInstance())
  }

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer))
  }

  private fun moveToNextState() {
    viewModel.setCanSubmitAnswer(canSubmitAnswer = false)
    explorationProgressController.moveToNextState().observe(
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

  private fun isAudioShowing(): Boolean = viewModel.isAudioBarVisible.get()!!

  /** Updates submit button UI as active if pendingAnswerError null else inactive. */
  fun updateSubmitButton(pendingAnswerError: String?, inputAnswerAvailable: Boolean) {
    if (inputAnswerAvailable) {
      viewModel.setCanSubmitAnswer(pendingAnswerError == null)
    } else {
      viewModel.setCanSubmitAnswer(canSubmitAnswer = false)
    }
  }

  private fun markExplorationAsRecentlyPlayed() {
    storyProgressController.recordRecentlyPlayedChapter(
      profileId,
      topicId,
      storyId,
      explorationId,
      Date().time
    )
  }

  private fun markExplorationCompleted() {
    storyProgressController.recordCompletedChapter(
      profileId,
      topicId,
      storyId,
      explorationId,
      Date().time
    )
  }
}
