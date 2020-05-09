package org.oppia.app.player.state

import android.content.Context
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.app.R
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.ContinueNavigationButtonItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NextButtonItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.PreviousButtonItemBinding
import org.oppia.app.databinding.PreviousResponsesHeaderItemBinding
import org.oppia.app.databinding.ReturnToTopicButtonItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.databinding.SubmitButtonItemBinding
import org.oppia.app.databinding.SubmittedAnswerItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Hint
import org.oppia.app.model.Interaction
import org.oppia.app.model.ProfileId
import org.oppia.app.model.Solution
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.audio.AudioButtonListener
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.AudioUiManager
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelFactory
import org.oppia.app.player.state.itemviewmodel.NextButtonViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.PreviousButtonViewModel
import org.oppia.app.player.state.itemviewmodel.PreviousResponsesHeaderViewModel
import org.oppia.app.player.state.itemviewmodel.ReturnToTopicButtonViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.SubmitButtonViewModel
import org.oppia.app.player.state.itemviewmodel.SubmittedAnswerViewModel
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.app.player.state.listener.NextNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.app.player.state.listener.RouteToHintsAndSolutionListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.app.player.stopexploration.StopExplorationInterface
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.utility.LifecycleSafeTimerFactory
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.domain.topic.StoryProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
import org.oppia.util.threading.BackgroundDispatcher
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
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory,
  private val interactionViewModelFactoryMap: Map<String, @JvmSuppressWildcards InteractionViewModelFactory>,
  private var lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
  @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
) : PreviousResponsesHeaderClickListener {

  private val routeToHintsAndSolutionListener = activity as RouteToHintsAndSolutionListener

  private lateinit var currentState: State
  private var feedbackId: String? = null
  private lateinit var profileId: ProfileId
  private lateinit var topicId: String
  private lateinit var storyId: String
  private lateinit var explorationId: String
  private lateinit var currentStateName: String
  private lateinit var binding: StateFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private var newAvailableHintIndex: Int = -1
  private var numberOfWrongAnswers: Int = -1
  private var isNewWrongAnswerSubmitted: Boolean = false
  private var allHintsExhausted: Boolean = false

  private val viewModel: StateViewModel by lazy {
    getStateViewModel()
  }
  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState()
  }
  /**
   * A list of view models corresponding to past view models that are hidden by default. These are intentionally not
   * retained upon configuration changes since the user can just re-expand the list. Note that the first element of this
   * list (when initialized), will always be the previous answer's header to help locate the items in the recycler view
   * (when present).
   */
  private val previousAnswerViewModels: MutableList<StateItemViewModel> = mutableListOf()
  /**
   * Whether the previously submitted wrong answers should be expanded. This value is intentionally not retained upon
   * configuration changes since the user can just re-expand the list.
   */
  private var hasPreviousResponsesExpanded: Boolean = false

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    val internalProfileId = fragment.arguments!!.getInt(STATE_FRAGMENT_PROFILE_ID_ARGUMENT_KEY, -1)
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    topicId = fragment.arguments!!.getString(STATE_FRAGMENT_TOPIC_ID_ARGUMENT_KEY)!!
    storyId = fragment.arguments!!.getString(STATE_FRAGMENT_STORY_ID_ARGUMENT_KEY)!!
    explorationId = fragment.arguments!!.getString(STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY)!!

    binding = StateFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    val stateRecyclerViewAdapter = createRecyclerViewAdapter()
    binding.stateRecyclerView.apply {
      adapter = stateRecyclerViewAdapter
    }
    recyclerViewAdapter = stateRecyclerViewAdapter
    binding.let {
      it.lifecycleOwner = fragment
      it.viewModel = this.viewModel
    }

    if (getAudioFragment() == null) {
      fragment.childFragmentManager.beginTransaction()
        .add(R.id.audio_fragment_placeholder, AudioFragment(), TAG_AUDIO_FRAGMENT).commitNow()
    }
    binding.stateRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      if (bottom < oldBottom) {
        binding.stateRecyclerView.postDelayed({
          binding.stateRecyclerView.scrollToPosition(stateRecyclerViewAdapter.itemCount - 1)
        }, 100)
      }
    }

    binding.hintsAndSolutionFragmentContainer.setOnClickListener {
      routeToHintsAndSolutionListener.routeToHintsAndSolution(
        explorationId,
        newAvailableHintIndex,
        allHintsExhausted
      )
    }

    subscribeToCurrentState()
    markExplorationAsRecentlyPlayed()
    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StateItemViewModel> {
    return BindableAdapter.MultiTypeBuilder
      .newBuilder(StateItemViewModel::viewType)
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON,
        inflateDataBinding = ContinueNavigationButtonItemBinding::inflate,
        setViewModel = ContinueNavigationButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as ContinueNavigationButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.PREVIOUS_NAVIGATION_BUTTON,
        inflateDataBinding = PreviousButtonItemBinding::inflate,
        setViewModel = PreviousButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as PreviousButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON,
        inflateDataBinding = NextButtonItemBinding::inflate,
        setViewModel = NextButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as NextButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.RETURN_TO_TOPIC_NAVIGATION_BUTTON,
        inflateDataBinding = ReturnToTopicButtonItemBinding::inflate,
        setViewModel = ReturnToTopicButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as ReturnToTopicButtonViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON,
        inflateDataBinding = SubmitButtonItemBinding::inflate,
        setViewModel = SubmitButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as SubmitButtonViewModel }
      )
      .registerViewBinder(
        viewType = StateItemViewModel.ViewType.CONTENT,
        inflateView = { parent ->
          ContentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<ContentItemBinding>(view)!!
          binding.htmlContent =
            htmlParserFactory.create(entityType, explorationId, /* imageCenterAlign= */ true)
              .parseOppiaHtml(
                (viewModel as ContentViewModel).htmlContent.toString(), binding.contentTextView
              )
        }
      )
      .registerViewBinder(
        viewType = StateItemViewModel.ViewType.FEEDBACK,
        inflateView = { parent ->
          FeedbackItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<FeedbackItemBinding>(view)!!
          binding.htmlContent =
            htmlParserFactory.create(entityType, explorationId, /* imageCenterAlign= */ true)
              .parseOppiaHtml(
                (viewModel as FeedbackViewModel).htmlContent.toString(), binding.feedbackTextView
              )
        }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.CONTINUE_INTERACTION,
        inflateDataBinding = ContinueInteractionItemBinding::inflate,
        setViewModel = ContinueInteractionItemBinding::setViewModel,
        transformViewModel = { it as ContinueInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.SELECTION_INTERACTION,
        inflateDataBinding = SelectionInteractionItemBinding::inflate,
        setViewModel = SelectionInteractionItemBinding::setViewModel,
        transformViewModel = { it as SelectionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION,
        inflateDataBinding = FractionInteractionItemBinding::inflate,
        setViewModel = FractionInteractionItemBinding::setViewModel,
        transformViewModel = { it as FractionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION,
        inflateDataBinding = NumericInputInteractionItemBinding::inflate,
        setViewModel = NumericInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as NumericInputViewModel }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION,
        inflateDataBinding = TextInputInteractionItemBinding::inflate,
        setViewModel = TextInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as TextInputViewModel }
      )
      .registerViewBinder(
        viewType = StateItemViewModel.ViewType.SUBMITTED_ANSWER,
        inflateView = { parent ->
          SubmittedAnswerItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
          ).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<SubmittedAnswerItemBinding>(view)!!
          val userAnswer = (viewModel as SubmittedAnswerViewModel).submittedUserAnswer
          when (userAnswer.textualAnswerCase) {
            UserAnswer.TextualAnswerCase.HTML_ANSWER -> {
              val htmlParser = htmlParserFactory.create(entityType, explorationId, imageCenterAlign = true)
              binding.submittedAnswer = htmlParser.parseOppiaHtml(
                userAnswer.htmlAnswer, binding.submittedAnswerTextView
              )
            }
            else -> binding.submittedAnswer = userAnswer.plainAnswer
          }
        }
      )
      .registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER,
        inflateDataBinding = PreviousResponsesHeaderItemBinding::inflate,
        setViewModel = PreviousResponsesHeaderItemBinding::setViewModel,
        transformViewModel = { it as PreviousResponsesHeaderViewModel }
      )
      .build()
  }

  fun handleAudioClick() {
    (getAudioFragment() as AudioFragment).handleAudioClick(isAudioShowing(), feedbackId)
  }

  fun handleAnswerReadyForSubmission(answer: UserAnswer) {
    // An interaction has indicated that an answer is ready for submission.
    handleSubmitAnswer(answer)
  }

  fun revealHint(saveUserChoice: Boolean, hintIndex: Int) {
    subscribeToHint(explorationProgressController.submitHintIsRevealed(currentState, saveUserChoice, hintIndex))
  }

  fun revealSolution(saveUserChoice: Boolean) {
    subscribeToSolution(explorationProgressController.submitSolutionIsRevealed(currentState, saveUserChoice))
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(TAG_AUDIO_FRAGMENT)
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
    val hasPreviousState = ephemeralState.hasPreviousState

    currentState = ephemeralState.state

    val scrollToTop =
      ::currentStateName.isInitialized && currentStateName != ephemeralState.state.name

    currentStateName = ephemeralState.state.name

    showOrHideAudioByState(ephemeralState.state)

    previousAnswerViewModels.clear() // But retain whether the list is currently open.
    val pendingItemList = mutableListOf<StateItemViewModel>()
    addContentItem(pendingItemList, ephemeralState)
    val interaction = ephemeralState.state.interaction
    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.pendingState.wrongAnswerList)
      numberOfWrongAnswers = ephemeralState.pendingState.wrongAnswerList.size
      // Check if hints are available for this state.
      if (ephemeralState.state.interaction.hintList.size != 0) {
        // Check if user submits 1st wrong answer. The first hint is unlocked after 60s on submission of wrong answer.
        if (ephemeralState.pendingState.wrongAnswerList.size == 1) {
          lifecycleSafeTimerFactory.cancel()
          lifecycleSafeTimerFactory = LifecycleSafeTimerFactory(backgroundCoroutineDispatcher)
          if (!ephemeralState.state.interaction.hintList[0].hintIsRevealed) {
            lifecycleSafeTimerFactory.createTimer(60000).observe(activity, Observer {
              newAvailableHintIndex = 0
              viewModel.setHintOpenedAndUnRevealedVisibility(true)
              viewModel.setHintBulbVisibility(true)
            })
          }
        } else if (!isNewWrongAnswerSubmitted) {
          // Subsequent hints are unlocked at 30s intervals when no answer is submitted by the user.
          for (index in 0 until ephemeralState.state.interaction.hintList.size) {
            lifecycleSafeTimerFactory.cancel()
            lifecycleSafeTimerFactory = LifecycleSafeTimerFactory(backgroundCoroutineDispatcher)

            if (index != 0 && !ephemeralState.state.interaction.hintList[index].hintIsRevealed) {
              lifecycleSafeTimerFactory.createTimer(30000).observe(activity, Observer {
                newAvailableHintIndex = index
                viewModel.setHintOpenedAndUnRevealedVisibility(true)
                viewModel.setHintBulbVisibility(true)
              })
              break
            } else if (index == (ephemeralState.state.interaction.hintList.size - 1) && !ephemeralState.state.interaction.solution.solutionIsRevealed) {
              if (ephemeralState.state.interaction.solution.hasCorrectAnswer()) {
                lifecycleSafeTimerFactory.createTimer(30000).observe(activity, Observer {
                  allHintsExhausted = true
                  viewModel.setHintOpenedAndUnRevealedVisibility(true)
                  viewModel.setHintBulbVisibility(true)
                })
              }
              break
            }
          }
        }
      }
      addInteractionForPendingState(pendingItemList, interaction, hasPreviousState)
    } else if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.completedState.answerList)
    }

    var canContinueToNextState = false
    var hasGeneralContinueButton = false
    if (ephemeralState.stateTypeCase != EphemeralState.StateTypeCase.TERMINAL_STATE) {
      if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE
        && !ephemeralState.hasNextState
      ) {
        hasGeneralContinueButton = true
      } else if (ephemeralState.completedState.answerList.size > 0 && ephemeralState.hasNextState) {
        canContinueToNextState = true
      }
    }

    val audioManager = getAudioFragment() as AudioUiManager
    audioManager.setStateAndExplorationId(ephemeralState.state, explorationId)
    if (viewModel.currentStateName == null || viewModel.currentStateName != currentStateName) {
      feedbackId = null
      viewModel.currentStateName = currentStateName
      if (isAudioShowing()) {
        audioManager.loadMainContentAudio(!canContinueToNextState)
      }
    }

    maybeAddNavigationButtons(
      pendingItemList,
      hasPreviousState,
      canContinueToNextState,
      hasGeneralContinueButton,
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    )

    viewModel.itemList.clear()
    viewModel.itemList += pendingItemList

    if (scrollToTop) {
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
    hintLiveData.observe(fragment, Observer<Hint> { result ->
      // If the hint was revealed remove dot and radar.
      if (result.hintIsRevealed) {
        viewModel.setHintOpenedAndUnRevealedVisibility(false)
      }
    })
  }

  /**
   * This function listens to the result of RevealSolution.
   * Whenever a hint is revealed using ExplorationProgressController.submitHintIsRevealed function,
   * this function will wait for the response from that function and based on which we can move to next state.
   */
  private fun subscribeToSolution(solutionResultLiveData: LiveData<AsyncResult<Solution>>) {
    val solutionLiveData = getSolutionIsRevealed(solutionResultLiveData)
    solutionLiveData.observe(fragment, Observer<Solution> { result ->
      // If the hint was revealed remove dot and radar.
      if (result.solutionIsRevealed) {
        viewModel.setHintOpenedAndUnRevealedVisibility(false)
      }
    })
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
        lifecycleSafeTimerFactory.cancel()
        viewModel.setHintBulbVisibility(false)
        moveToNextState()
      } else {
        if (result.labelledAsCorrectAnswer) {
          lifecycleSafeTimerFactory.cancel()
          viewModel.setHintBulbVisibility(false)
          showCongratulationMessageOnCorrectAnswer()
        } else {
          numberOfWrongAnswers += 1
          // Show new hint in 10sec on submitting new wrong answer. Cancel and Reinitialize timer.
          if (numberOfWrongAnswers > 1) {
            isNewWrongAnswerSubmitted = true
            lifecycleSafeTimerFactory.cancel()
            lifecycleSafeTimerFactory = LifecycleSafeTimerFactory(backgroundCoroutineDispatcher)
            if (currentState.interaction.hintList.size != 0) {
              for (index in 0 until currentState.interaction.hintList.size) {
                if (index == 0 && !currentState.interaction.hintList[0].hintIsRevealed) {
                  lifecycleSafeTimerFactory.createTimer(10000).observe(activity, Observer {
                    newAvailableHintIndex = 0
                    viewModel.setHintOpenedAndUnRevealedVisibility(true)
                    viewModel.setHintBulbVisibility(true)
                    isNewWrongAnswerSubmitted = false
                  })
                  break
                }
                if (index != 0 && !currentState.interaction.hintList[index].hintIsRevealed) {
                  lifecycleSafeTimerFactory.createTimer(10000).observe(activity, Observer {
                    newAvailableHintIndex = index
                    viewModel.setHintOpenedAndUnRevealedVisibility(true)
                    viewModel.setHintBulbVisibility(true)
                    isNewWrongAnswerSubmitted = false
                  })
                  break
                } else if (index == (currentState.interaction.hintList.size - 1) && !currentState.interaction.solution.solutionIsRevealed) {
                  if (currentState.interaction.solution.hasCorrectAnswer()) {
                    lifecycleSafeTimerFactory.createTimer(10000).observe(activity, Observer {
                      allHintsExhausted = true
                      viewModel.setHintOpenedAndUnRevealedVisibility(true)
                      viewModel.setHintBulbVisibility(true)
                      isNewWrongAnswerSubmitted = false
                    })
                  }
                  break
                }
              }
            }
          }
        }
        feedbackId = result.feedback.contentId
        if (isAudioShowing()) {
          (getAudioFragment() as AudioUiManager).loadFeedbackAudio(feedbackId!!, true)
        }
      }
    })
  }

  private fun showCongratulationMessageOnCorrectAnswer() {
    binding.congratulationTextView.visibility = View.VISIBLE

    val fadeIn = AlphaAnimation(0f, 1f)
    fadeIn.interpolator = DecelerateInterpolator()
    fadeIn.duration = 2000

    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.startOffset = 1000
    fadeOut.duration = 1000

    val animation = AnimationSet(false)
    animation.addAnimation(fadeIn)
    animation.addAnimation(fadeOut)
    binding.congratulationTextView.animation = animation

    Handler().postDelayed({
      binding.congratulationTextView.clearAnimation()
      binding.congratulationTextView.visibility = View.INVISIBLE
    }, 2000)

    lifecycleSafeTimerFactory.cancel()
    viewModel.setHintBulbVisibility(false)
  }

  /** Helper for [subscribeToSolution]. */
  private fun getSolutionIsRevealed(hint: LiveData<AsyncResult<Solution>>): LiveData<Solution> {
    return Transformations.map(hint, ::processSolution)
  }

  /** Helper for [subscribeToHint]. */
  private fun getHintIsRevealed(hint: LiveData<AsyncResult<Hint>>): LiveData<Hint> {
    return Transformations.map(hint, ::processHint)
  }

  /** Helper for [subscribeToAnswerOutcome]. */
  private fun getAnswerOutcome(answerOutcome: LiveData<AsyncResult<AnswerOutcome>>): LiveData<AnswerOutcome> {
    return Transformations.map(answerOutcome, ::processAnswerOutcome)
  }

  /** Helper for [subscribeToAnswerOutcome]. */
  private fun processAnswerOutcome(ephemeralStateResult: AsyncResult<AnswerOutcome>): AnswerOutcome {
    if (ephemeralStateResult.isFailure()) {
      logger.e(
        "StateFragment",
        "Failed to retrieve answer outcome",
        ephemeralStateResult.getErrorOrNull()!!
      )
    }
    return ephemeralStateResult.getOrDefault(AnswerOutcome.getDefaultInstance())
  }

  fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    markExplorationCompleted()
    (activity as StopExplorationInterface).stopExploration()
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

  private fun showOrHideAudioByState(state: State) {
    if (state.recordedVoiceoversCount == 0) {
      (activity as AudioButtonListener).hideAudioButton()
    } else {
      (activity as AudioButtonListener).showAudioButton()
    }
  }

  fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(viewModel.getPendingAnswer())
  }

  fun handleKeyboardAction() {
    hideKeyboard()
    if (viewModel.getCanSubmitAnswer().get() == true) {
      handleSubmitAnswer(viewModel.getPendingAnswer())
    }
  }

  fun onContinueButtonClicked() {
    viewModel.setHintBulbVisibility(false)
    hideKeyboard()
    moveToNextState()
  }

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer))
  }

  fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  fun onNextButtonClicked() = moveToNextState()

  override fun onResponsesHeaderClicked() {
    togglePreviousAnswers()
  }

  private fun moveToNextState() {
    explorationProgressController.moveToNextState().observe(fragment, Observer<AsyncResult<Any?>> {
      hasPreviousResponsesExpanded = false
    })
  }

  private fun addInteractionForPendingState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, hasPreviousButton: Boolean
  ) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory(
      explorationId, interaction, fragment as InteractionAnswerReceiver, fragment as InteractionAnswerErrorReceiver,
      hasPreviousButton
    )
  }

  private fun addContentItem(
    pendingItemList: MutableList<StateItemViewModel>,
    ephemeralState: EphemeralState
  ) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    pendingItemList += ContentViewModel(contentSubtitledHtml.html)
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>, answersAndResponses: List<AnswerAndResponse>
  ) {
    if (answersAndResponses.size > 1) {
      PreviousResponsesHeaderViewModel(
        answersAndResponses.size - 1, ObservableBoolean(hasPreviousResponsesExpanded), this
      ).let { viewModel ->
        pendingItemList += viewModel
        previousAnswerViewModels += viewModel
      }
      // Only add previous answers if current responses are expanded.
      for (answerAndResponse in answersAndResponses.take(answersAndResponses.size - 1)) {
        createSubmittedAnswer(answerAndResponse.userAnswer).let { viewModel ->
          if (hasPreviousResponsesExpanded) {
            pendingItemList += viewModel
          }
          previousAnswerViewModels += viewModel
        }
        createFeedbackItem(answerAndResponse.feedback)?.let { viewModel ->
          if (hasPreviousResponsesExpanded) {
            pendingItemList += viewModel
          }
          previousAnswerViewModels += viewModel
        }
      }
    }
    answersAndResponses.lastOrNull()?.let { answerAndResponse ->
      pendingItemList += createSubmittedAnswer(answerAndResponse.userAnswer)
      createFeedbackItem(answerAndResponse.feedback)?.let(pendingItemList::add)
    }
  }

  /**
   * Toggles whether the previous answers should be shown based on the current state stored in
   * [PreviousResponsesHeaderViewModel].
   */
  private fun togglePreviousAnswers() {
    val headerModel = previousAnswerViewModels.first() as PreviousResponsesHeaderViewModel
    val expandPreviousAnswers = !headerModel.isExpanded.get()
    val headerIndex = viewModel.itemList.indexOf(headerModel)
    val previousAnswersAndFeedbacks =
      previousAnswerViewModels.takeLast(previousAnswerViewModels.size - 1)
    if (expandPreviousAnswers) {
      // Add the pending view models to the recycler view to expand them.
      viewModel.itemList.addAll(headerIndex + 1, previousAnswersAndFeedbacks)
    } else {
      // Remove the pending view models to collapse the list.
      viewModel.itemList.removeAll(previousAnswersAndFeedbacks)
    }
    // Ensure the header matches the updated state.
    headerModel.isExpanded.set(expandPreviousAnswers)
    hasPreviousResponsesExpanded = expandPreviousAnswers
    recyclerViewAdapter.notifyDataSetChanged()
  }

  private fun createSubmittedAnswer(userAnswer: UserAnswer): SubmittedAnswerViewModel {
    return SubmittedAnswerViewModel(userAnswer)
  }

  private fun createFeedbackItem(feedback: SubtitledHtml): FeedbackViewModel? {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      return FeedbackViewModel(feedback.html)
    }
    return null
  }

  private fun maybeAddNavigationButtons(
    pendingItemList: MutableList<StateItemViewModel>,
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean) {
    when {
      hasGeneralContinueButton -> ContinueNavigationButtonViewModel(
        hasPreviousState, fragment as PreviousNavigationButtonListener, fragment as ContinueNavigationButtonListener
      )
      canContinueToNextState -> NextButtonViewModel(
        hasPreviousState, fragment as PreviousNavigationButtonListener, fragment as NextNavigationButtonListener
      )
      stateIsTerminal -> ReturnToTopicButtonViewModel(
        hasPreviousState, fragment as PreviousNavigationButtonListener,
        fragment as ReturnToTopicNavigationButtonListener
      )
      viewModel.doesMostRecentInteractionRequireExplicitSubmission(pendingItemList) -> SubmitButtonViewModel(
        viewModel.getCanSubmitAnswer(),
        hasPreviousState,
        fragment as PreviousNavigationButtonListener,
        fragment as SubmitNavigationButtonListener
      )
      // Otherwise, just show the previous button since the interaction itself will push the answer submission.
      hasPreviousState && !viewModel.isMostRecentInteractionAutoNavigating(pendingItemList) -> PreviousButtonViewModel(
        fragment as PreviousNavigationButtonListener
      )
      // Otherwise, there's no navigation button that should be shown since the current interaction handles this.
      else -> null
    }?.let(pendingItemList::add)
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager =
      activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(
      fragment.view!!.windowToken,
      InputMethodManager.SHOW_FORCED
    )
  }

  fun setAudioBarVisibility(visibility: Boolean) = getStateViewModel().setAudioBarVisibility(visibility)

  fun scrollToTop() {
    binding.stateRecyclerView.smoothScrollToPosition(0)
  }

  private fun isAudioShowing(): Boolean = viewModel.isAudioBarVisible.get()!!

  /** Updates submit button UI as active if pendingAnswerError null else inactive. */
  fun updateSubmitButton(pendingAnswerError: String?) {
    viewModel.setCanSubmitAnswer(pendingAnswerError == null)
  }

  private fun markExplorationAsRecentlyPlayed() {
    storyProgressController.recordRecentlyPlayedChapter(profileId, topicId, storyId, explorationId, Date().time)
  }

  private fun markExplorationCompleted() {
    storyProgressController.recordCompletedChapter(profileId, topicId, storyId, explorationId, Date().time)
  }
}
