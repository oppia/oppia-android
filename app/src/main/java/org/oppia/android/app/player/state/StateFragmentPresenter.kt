package org.oppia.android.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.BounceInterpolator
import android.view.animation.Interpolator
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
import org.oppia.android.app.model.SurveyQuestionName
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.player.audio.AudioButtonListener
import org.oppia.android.app.player.audio.AudioFragment
import org.oppia.android.app.player.audio.AudioUiManager
import org.oppia.android.app.player.state.ConfettiConfig.LARGE_CONFETTI_BURST
import org.oppia.android.app.player.state.ConfettiConfig.MEDIUM_CONFETTI_BURST
import org.oppia.android.app.player.state.ConfettiConfig.MINI_CONFETTI_BURST
import org.oppia.android.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.android.app.player.stopplaying.StopStatePlayingSessionWithSavedProgressListener
import org.oppia.android.app.survey.SurveyWelcomeDialogFragment
import org.oppia.android.app.survey.TAG_SURVEY_WELCOME_DIALOG
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.SplitScreenManager
import org.oppia.android.app.utility.lifecycle.LifecycleSafeTimerFactory
import org.oppia.android.databinding.StateFragmentBinding
import org.oppia.android.domain.exploration.ExplorationProgressController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.survey.SurveyGatingController
import org.oppia.android.domain.topic.StoryProgressController
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProvider
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
  private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  private val explorationProgressController: ExplorationProgressController,
  private val storyProgressController: StoryProgressController,
  private val oppiaLogger: OppiaLogger,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory,
  private val splitScreenManager: SplitScreenManager,
  private val oppiaClock: OppiaClock,
  private val stateViewModel: StateViewModel,
  private val accessibilityService: AccessibilityService,
  private val resourceHandler: AppLanguageResourceHandler,
  private val surveyGatingController: SurveyGatingController
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
  private var forceAnnouncedForHintsBar = false

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
    explorationId: String,
    userAnswerState: UserAnswerState
  ): View? {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    this.topicId = topicId
    this.storyId = storyId
    this.explorationId = explorationId
    stateViewModel.initializeProfile(profileId)

    binding = StateFragmentBinding.inflate(
      inflater,
      container,
      /* attachToRoot= */ false
    )
    recyclerViewAssembler = createRecyclerViewAssembler(
      assemblerBuilderFactory.create(resourceBucketName, entityType, profileId, userAnswerState),
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
      it.viewModel = stateViewModel
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
    stateViewModel.setHintBulbVisibility(false)
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
    val answer = stateViewModel.getPendingAnswer(recyclerViewAssembler::getPendingAnswerHandler)
    if (answer != null) {
      handleSubmitAnswer(answer)
    }
  }

  fun onResponsesHeaderClicked() {
    recyclerViewAssembler.togglePreviousAnswers(stateViewModel.itemList)
    recyclerViewAssembler.adapter.notifyDataSetChanged()
  }

  fun handleAudioClick() = recyclerViewAssembler.toggleAudioPlaybackState()

  fun handleKeyboardAction() {
    hideKeyboard()
    if (stateViewModel.getCanSubmitAnswer().get() == true) {
      val answer = stateViewModel.getPendingAnswer(recyclerViewAssembler::getPendingAnswerHandler)
      if (answer != null) {
        handleSubmitAnswer(answer)
      }
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
      .addInteractionSupport(stateViewModel.getCanSubmitAnswer())
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
        explorationId, stateViewModel.currentStateName, stateViewModel.isAudioBarVisible,
        this::getAudioUiManager
      )
      .addConceptCardSupport()
      .build()
  }

  fun revealHint(hintIndex: Int) {
    subscribeToHintSolution(explorationProgressController.submitHintIsRevealed(hintIndex))
  }

  fun viewHint(hintIndex: Int) {
    explorationProgressController.submitHintIsViewed(hintIndex)
  }

  fun revealSolution() {
    subscribeToHintSolution(explorationProgressController.submitSolutionIsRevealed())
  }

  fun viewSolution() {
    explorationProgressController.submitSolutionIsViewed()
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
      stateViewModel.isSplitView.set(true)
      stateViewModel.centerGuidelinePercentage.set(0.5f)
    } else {
      stateViewModel.isSplitView.set(false)
      stateViewModel.centerGuidelinePercentage.set(1f)
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

    stateViewModel.itemList.clear()
    stateViewModel.itemList += dataPair.first
    stateViewModel.rightItemList.clear()
    stateViewModel.rightItemList += dataPair.second

    if (isInNewState) {
      (binding.stateRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(
        0,
        200
      )
    }
  }

  /** Subscribes to the result of requesting to show a hint or solution. */
  private fun subscribeToHintSolution(resultDataProvider: DataProvider<Any?>) {
    resultDataProvider.toLiveData().observe(
      fragment,
      { result ->
        if (result is AsyncResult.Failure) {
          oppiaLogger.e("StateFragment", "Failed to retrieve hint/solution", result.error)
        } else {
          // If the hint/solution, was revealed remove dot and radar.
          setHintOpenedAndUnRevealed(false)
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
    recyclerViewAssembler.resetUserAnswerState()
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
            stateViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
          }
          recyclerViewAssembler.readOutAnswerFeedback(result.feedback)
        }
      }
    )
  }

  /** Returns the [UserAnswerState] representing the user's current pending answer. */
  fun getUserAnswerState(): UserAnswerState {
    return stateViewModel.getUserAnswerState(recyclerViewAssembler::getPendingAnswerHandler)
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

  private fun moveToNextState() {
    stateViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
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
      fragment.requireView().windowToken,
      0 // Flag value to force hide the keyboard when possible.
    )
  }

  fun setAudioBarVisibility(visibility: Boolean) = stateViewModel.setAudioBarVisibility(visibility)

  fun scrollToTop() {
    binding.stateRecyclerView.smoothScrollToPosition(0)
  }

  /** Updates submit button UI as active if pendingAnswerError null else inactive. */
  fun updateSubmitButton(pendingAnswerError: String?, inputAnswerAvailable: Boolean) {
    if (inputAnswerAvailable) {
      stateViewModel.setCanSubmitAnswer(pendingAnswerError == null)
    } else {
      stateViewModel.setCanSubmitAnswer(canSubmitAnswer = false)
    }
  }

  /** Returns the checkpoint state for the current exploration. */
  fun getExplorationCheckpointState() = explorationCheckpointState

  private fun markExplorationCompleted() {
    val markStoryCompletedLivedata = storyProgressController.recordCompletedChapter(
      profileId,
      topicId,
      storyId,
      explorationId,
      oppiaClock.getCurrentTimeMs()
    ).toLiveData()

    // Only check gating result when the previous operation has completed because gating depends on
    // result of saving the time spent in the exploration, at the end of the exploration.
    markStoryCompletedLivedata.observe(fragment, { maybeShowSurveyDialog(profileId, topicId) })
  }

  private fun showHintsAndSolutions(helpIndex: HelpIndex, isCurrentStatePendingState: Boolean) {

    if (!isCurrentStatePendingState) {
      // If current state is not the pending top state, hide the hint bulb.
      setHintOpenedAndUnRevealed(false)
      stateViewModel.setHintBulbVisibility(false)
    } else {
      when (helpIndex.indexTypeCase) {
        HelpIndex.IndexTypeCase.NEXT_AVAILABLE_HINT_INDEX -> {
          stateViewModel.setHintBulbVisibility(true)
          setHintOpenedAndUnRevealed(true)
        }
        HelpIndex.IndexTypeCase.LATEST_REVEALED_HINT_INDEX -> {
          stateViewModel.setHintBulbVisibility(true)
          setHintOpenedAndUnRevealed(false)
        }
        HelpIndex.IndexTypeCase.SHOW_SOLUTION -> {
          stateViewModel.setHintBulbVisibility(true)
          setHintOpenedAndUnRevealed(true)
        }
        HelpIndex.IndexTypeCase.EVERYTHING_REVEALED -> {
          setHintOpenedAndUnRevealed(false)
          stateViewModel.setHintBulbVisibility(true)
        }
        else -> {
          setHintOpenedAndUnRevealed(false)
          stateViewModel.setHintBulbVisibility(false)
        }
      }
    }
  }

  private fun setHintOpenedAndUnRevealed(isHintUnrevealed: Boolean) {
    stateViewModel.setHintOpenedAndUnRevealedVisibility(isHintUnrevealed)
    if (isHintUnrevealed) {

      val hintBulbAnimation = AnimationUtils.loadAnimation(
        context,
        R.anim.hint_bulb_animation
      ).also { it.interpolator = BounceUpAndDownInterpolator() }

      // The bulb should start bouncing every 30 seconds. Note that an initial delay is used for
      // cases like configuration changes, or returning from a saved checkpoint.
      lifecycleSafeTimerFactory.run {
        activity.runPeriodically(delayMillis = 5_000, periodMillis = 30_000) {
          return@runPeriodically stateViewModel.isHintOpenedAndUnRevealed.get()!!.also { playAnim ->
            if (playAnim) binding.hintBulb.startAnimation(hintBulbAnimation)
            // Make a forced announcement when the hint bar becomes visible so that the non sighted
            // users know about the availability of hints. Instead of suddenly changing the focus of
            // the app (which is a bad practice) to the hints bar upon availability, make a forced
            // announcement. The forced announcement should be called after 5 seconds after the
            // hints bar appears otherwise it might interrupt with the submit button's content
            // description during Talkback.
            if (!forceAnnouncedForHintsBar) {
              forceAnnouncedForHintsBar = true
              accessibilityService.announceForAccessibilityForView(
                binding.hintsAndSolutionFragmentContainer,
                resourceHandler.getStringInLocale(
                  R.string.state_fragment_hint_bar_forced_announcement_text
                )
              )
            }
          }
        }
      }
    } else {
      binding.hintBulb.clearAnimation()
    }
  }

  private fun maybeShowSurveyDialog(profileId: ProfileId, topicId: String) {
    surveyGatingController.maybeShowSurvey(profileId, topicId).toLiveData().observe(
      activity,
      { gatingResult ->
        when (gatingResult) {
          is AsyncResult.Pending -> {
            oppiaLogger.d("StateFragment", "A gating decision is pending")
          }
          is AsyncResult.Failure -> {
            oppiaLogger.e(
              "StateFragment",
              "Failed to retrieve gating decision",
              gatingResult.error
            )
            (activity as StopStatePlayingSessionWithSavedProgressListener)
              .deleteCurrentProgressAndStopSession(isCompletion = true)
          }
          is AsyncResult.Success -> {
            if (gatingResult.value) {
              val dialogFragment =
                SurveyWelcomeDialogFragment.newInstance(
                  profileId,
                  topicId,
                  explorationId,
                  SURVEY_QUESTIONS
                )
              val transaction = activity.supportFragmentManager.beginTransaction()
              transaction
                .add(dialogFragment, TAG_SURVEY_WELCOME_DIALOG)
                .commitNow()
            } else {
              (activity as StopStatePlayingSessionWithSavedProgressListener)
                .deleteCurrentProgressAndStopSession(isCompletion = true)
            }
          }
        }
      }
    )
  }

  /**
   * An [Interpolator] when performs a reversed, then regular bounce interpolation using
   * [BounceInterpolator].
   *
   * This interpolator maps input time from [0, 0.5] to [1.0, 0.0] and (0.5, 1.0] to (0.0, 1.0],
   * allowing a clean continuous reverse bounce animation such that the item being bounced returns
   * to its original position (which is expected to be the "final" transformation value). Note the
   * start and end of the same time values for output--interpolators in Android normally don't allow
   * this which is why modeling this animation behavior any other way is particularly challenging.
   */
  private class BounceUpAndDownInterpolator : Interpolator {
    private val bounceInterpolator by lazy { BounceInterpolator() }

    override fun getInterpolation(input: Float): Float {
      // To get the correct continuous bounce, run the reverse bounce from 100% to 0% for the first
      // 50% of time, then run the regular bounce from 0% to 100% for the remaining 50%.
      return if (input <= 0.5f) {
        bounceInterpolator.getInterpolation(1f - input * 2f)
      } else bounceInterpolator.getInterpolation(input * 2f - 1f)
    }
  }

  companion object {
    private val SURVEY_QUESTIONS = listOf(
      SurveyQuestionName.USER_TYPE,
      SurveyQuestionName.MARKET_FIT,
      SurveyQuestionName.NPS
    )
  }
}
