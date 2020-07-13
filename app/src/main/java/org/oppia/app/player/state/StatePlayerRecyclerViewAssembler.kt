package org.oppia.app.player.state

import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineDispatcher
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.ContinueNavigationButtonItemBinding
import org.oppia.app.databinding.DragDropInteractionItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NextButtonItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.PreviousButtonItemBinding
import org.oppia.app.databinding.PreviousResponsesHeaderItemBinding
import org.oppia.app.databinding.QuestionPlayerFeedbackItemBinding
import org.oppia.app.databinding.QuestionPlayerSelectionInteractionItemBinding
import org.oppia.app.databinding.QuestionPlayerSubmittedAnswerItemBinding
import org.oppia.app.databinding.ReplayButtonItemBinding
import org.oppia.app.databinding.ReturnToTopicButtonItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.SubmitButtonItemBinding
import org.oppia.app.databinding.SubmittedAnswerItemBinding
import org.oppia.app.databinding.SubmittedAnswerListItemBinding
import org.oppia.app.databinding.SubmittedHtmlAnswerItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.HelpIndex
import org.oppia.app.model.HelpIndex.IndexTypeCase.INDEXTYPE_NOT_SET
import org.oppia.app.model.Interaction
import org.oppia.app.model.PendingState
import org.oppia.app.model.State
import org.oppia.app.model.StringList
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.audio.AudioUiManager
import org.oppia.app.player.state.StatePlayerRecyclerViewAssembler.Builder.Factory
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.DragAndDropSortInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelFactory
import org.oppia.app.player.state.itemviewmodel.NextButtonViewModel
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.PlayerRecyclerViewAssemblerViewModel
import org.oppia.app.player.state.itemviewmodel.PreviousButtonViewModel
import org.oppia.app.player.state.itemviewmodel.PreviousResponsesHeaderViewModel
import org.oppia.app.player.state.itemviewmodel.ReplayButtonViewModel
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
import org.oppia.app.player.state.listener.ReplayButtonListener
import org.oppia.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.app.player.state.listener.ShowHintAvailabilityListener
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.topic.questionplayer.QuestionPlayerFragment
import org.oppia.app.utility.LifecycleSafeTimerFactory
import org.oppia.util.parser.HtmlParser
import org.oppia.util.threading.BackgroundDispatcher
import javax.inject.Inject

private const val DELAY_SHOW_INITIAL_HINT_MS = 60_000L
private const val DELAY_SHOW_ADDITIONAL_HINTS_MS = 30_000L
private const val DELAY_SHOW_ADDITIONAL_HINTS_FROM_WRONG_ANSWER_MS = 10_000L
private typealias AudioUiManagerRetriever = () -> AudioUiManager?

/**
 * An assembler for generating the list of view models to bind to the state player recycler view.
 * This class also handles some non-recycler view feature management, such as the congratulations
 * message for a correct answer.
 *
 * One instance of this class should exist per fragment hosting the underlying recycler view. It's
 * expected that this class be reconstructed on configuration changes, as it nor its contents are
 * directly parcelable. The state loss from recreating this class will have the expected behavior so
 * long as the next assembler has the same feature set as the one being destroyed.
 *
 * This class should only be interacted on the main thread.
 *
 * Note that the fragment hosting this assembler is expected to implement the following interfaces:
 * - [InteractionAnswerReceiver] if interaction support is enabled
 * - [SubmitNavigationButtonListener] if interaction support is enabled
 * - [PreviousResponsesHeaderClickListener] if previous response collapsing is enabled
 * - [PreviousNavigationButtonListener] if previous state navigation is enabled
 * - [ContinueNavigationButtonListener] if next state navigation is enabled
 * - [NextNavigationButtonListener] if next state navigation is enabled
 * - [ReplayButtonListener] if replay support is enabled
 * - [ReturnToTopicNavigationButtonListener] if the return to topic button is enabled
 */
class StatePlayerRecyclerViewAssembler private constructor(
  val adapter: BindableAdapter<StateItemViewModel>,
  private val playerFeatureSet: PlayerFeatureSet,
  private val fragment: Fragment,
  private val congratulationsTextView: TextView?,
  private val canSubmitAnswer: ObservableField<Boolean>?,
  private val audioActivityId: String?,
  private val currentStateName: ObservableField<String>?,
  private val isAudioPlaybackEnabled: ObservableField<Boolean>?,
  private val audioUiManagerRetriever: AudioUiManagerRetriever?,
  private val interactionViewModelFactoryMap: Map<
    String, @JvmSuppressWildcards InteractionViewModelFactory>,
  backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  /**
   * A list of view models corresponding to past view models that are hidden by default. These are
   * intentionally not retained upon configuration changes since the user can just re-expand the
   * list. Note that the first element of this list (when initialized), will always be the previous
   * answers header to help locate the items in the recycler view (when present).
   */
  private val previousAnswerViewModels: MutableList<StateItemViewModel> = mutableListOf()

  /**
   * Whether the previously submitted wrong answers should be expanded. This value is intentionally
   * not retained upon configuration changes since the user can just re-expand the list.
   */
  private var hasPreviousResponsesExpanded: Boolean = false

  val isCorrectAnswer = ObservableField<Boolean>(false)

  private val lifecycleSafeTimerFactory = LifecycleSafeTimerFactory(backgroundCoroutineDispatcher)

  private val hintHandler = HintHandler(lifecycleSafeTimerFactory, fragment)

  /** The most recent content ID read by the audio system. */
  private var audioPlaybackContentId: String? = null

  /**
   * An ever-present [PreviousNavigationButtonListener] that can exist even if backward navigation
   * is disabled. This listener no-ops if backward navigation is enabled. This serves to allows the
   * host fragment to not need to implement [PreviousNavigationButtonListener] if backward
   * navigation is disabled.
   */
  private val previousNavigationButtonListener = object : PreviousNavigationButtonListener {
    override fun onPreviousButtonClicked() {
      if (playerFeatureSet.backwardNavigation) {
        (fragment as PreviousNavigationButtonListener).onPreviousButtonClicked()
      }
    }
  }

  /**
   * Computes a list of view models corresponding to the specified [EphemeralState] and the
   * configuration of this assembler, as well as the GCS entity ID that should be associated with
   * rich-text rendering for this state.
   */
  fun compute(ephemeralState: EphemeralState, gcsEntityId: String): List<StateItemViewModel> {
    val hasPreviousState = ephemeralState.hasPreviousState

    previousAnswerViewModels.clear()
    val pendingItemList = mutableListOf<StateItemViewModel>()
    if (playerFeatureSet.contentSupport) {
      addContentItem(pendingItemList, ephemeralState, gcsEntityId)
    }
    val interaction = ephemeralState.state.interaction
    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.pendingState.wrongAnswerList, gcsEntityId)
      if (playerFeatureSet.hintsAndSolutionsSupport) {
        hintHandler.maybeScheduleShowHint(ephemeralState.state, ephemeralState.pendingState)
      }
      if (playerFeatureSet.interactionSupport) {
        addInteractionForPendingState(pendingItemList, interaction, hasPreviousState, gcsEntityId)
      }
    } else if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.completedState.answerList, gcsEntityId)
    }

    var canContinueToNextState = false
    var hasGeneralContinueButton = false
    if (ephemeralState.stateTypeCase != EphemeralState.StateTypeCase.TERMINAL_STATE) {
      if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE &&
        !ephemeralState.hasNextState
      ) {
        hasGeneralContinueButton = true
      } else if (ephemeralState.completedState.answerList.size > 0 && ephemeralState.hasNextState) {
        canContinueToNextState = true
      }
    }

    if (playerFeatureSet.supportAudioVoiceovers) {
      val processedStateName = ephemeralState.state.name
      val audioManager = getAudioUiManager()
      val activityId = checkNotNull(audioActivityId) {
        "Expected the audio activity ID to be set when voiceovers are enabled"
      }
      audioManager?.setStateAndExplorationId(ephemeralState.state, activityId)

      if (currentStateName?.get() != processedStateName) {
        audioPlaybackContentId = null
        currentStateName?.set(processedStateName)
        if (isAudioPlaybackEnabled()) {
          audioManager?.loadMainContentAudio(!canContinueToNextState)
        }
      }
    }

    maybeAddNavigationButtons(
      pendingItemList,
      hasPreviousState,
      canContinueToNextState,
      hasGeneralContinueButton,
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    )

    return pendingItemList
  }

  private fun addInteractionForPendingState(
    pendingItemList: MutableList<StateItemViewModel>,
    interaction: Interaction,
    hasPreviousButton: Boolean,
    gcsEntityId: String
  ) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory(
      gcsEntityId,
      interaction,
      fragment as InteractionAnswerReceiver,
      fragment as InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton
    )
  }

  private fun addContentItem(
    pendingItemList: MutableList<StateItemViewModel>,
    ephemeralState: EphemeralState,
    gcsEntityId: String
  ) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    pendingItemList += ContentViewModel(contentSubtitledHtml.html, gcsEntityId)
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>,
    answersAndResponses: List<AnswerAndResponse>,
    gcsEntityId: String
  ) {
    if (answersAndResponses.size > 1) {
      if (playerFeatureSet.wrongAnswerCollapsing) {
        PreviousResponsesHeaderViewModel(
          answersAndResponses.size - 1, ObservableBoolean(hasPreviousResponsesExpanded),
          fragment as PreviousResponsesHeaderClickListener
        ).let { viewModel ->
          pendingItemList += viewModel
          previousAnswerViewModels += viewModel
        }
      }
      // Only add previous answers if current responses are expanded, or if collapsing is disabled.
      val showPreviousAnswers = !playerFeatureSet.wrongAnswerCollapsing ||
        hasPreviousResponsesExpanded
      for (answerAndResponse in answersAndResponses.take(answersAndResponses.size - 1)) {
        if (playerFeatureSet.pastAnswerSupport) {
          createSubmittedAnswer(answerAndResponse.userAnswer, gcsEntityId).let { viewModel ->
            if (showPreviousAnswers) {
              pendingItemList += viewModel
            }
            previousAnswerViewModels += viewModel
          }
        }
        if (playerFeatureSet.feedbackSupport) {
          createFeedbackItem(answerAndResponse.feedback, gcsEntityId)?.let { viewModel ->
            if (showPreviousAnswers) {
              pendingItemList += viewModel
            }
            previousAnswerViewModels += viewModel
          }
        }
      }
    }
    answersAndResponses.lastOrNull()?.let { answerAndResponse ->
      if (playerFeatureSet.pastAnswerSupport) {
        pendingItemList += createSubmittedAnswer(answerAndResponse.userAnswer, gcsEntityId)
      }
      if (playerFeatureSet.feedbackSupport) {
        createFeedbackItem(answerAndResponse.feedback, gcsEntityId)?.let(pendingItemList::add)
      }
    }
  }

  /**
   * Toggles whether the previous answers should be shown based on the current state stored in
   * [PreviousResponsesHeaderViewModel] by transforming the current observable list of view models.
   *
   * This does not notify the underlying recycler view.
   */
  fun togglePreviousAnswers(itemList: ObservableList<StateItemViewModel>) {
    check(playerFeatureSet.wrongAnswerCollapsing) {
      "Cannot toggle previous answers for assembler that doesn't support wrong answer collapsing"
    }
    val headerModel = previousAnswerViewModels.first() as PreviousResponsesHeaderViewModel
    val expandPreviousAnswers = !headerModel.isExpanded.get()
    val headerIndex = itemList.indexOf(headerModel)
    val previousAnswersAndFeedbacks =
      previousAnswerViewModels.takeLast(previousAnswerViewModels.size - 1)
    if (expandPreviousAnswers) {
      // Add the pending view models to the recycler view to expand them.
      itemList.addAll(headerIndex + 1, previousAnswersAndFeedbacks)
    } else {
      // Remove the pending view models to collapse the list.
      itemList.removeAll(previousAnswersAndFeedbacks)
    }
    // Ensure the header matches the updated state.
    headerModel.isExpanded.set(expandPreviousAnswers)
    hasPreviousResponsesExpanded = expandPreviousAnswers
  }

  /**
   * Ensures that the previous responses, if any, are no longer expanded. This does not recompute
   * the recycler view adapter data--that requires another call to [compute]. If this is meant to
   * have an immediate UI effect, [togglePreviousAnswers] should be used, instead.
   */
  fun collapsePreviousResponses() {
    check(playerFeatureSet.wrongAnswerCollapsing) {
      "Cannot collapse previous answers for assembler that doesn't support wrong answer collapsing"
    }
    hasPreviousResponsesExpanded = false
  }

  /** Shows a congratulations message due to the learner having submitted a correct answer. */
  fun showCongratulationMessageOnCorrectAnswer() {
    check(playerFeatureSet.showCongratulationsOnCorrectAnswer) {
      "Cannot show congratulations message for assembler that doesn't support it"
    }
    val textView = checkNotNull(congratulationsTextView) {
      "Expected non-null reference to congratulations text view"
    }
    textView.visibility = View.VISIBLE

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
    textView.animation = animation

    lifecycleSafeTimerFactory.createTimer(2000).observe(
      fragment,
      Observer {
        textView.clearAnimation()
        textView.visibility = View.INVISIBLE
      }
    )
  }

  /**
   * Stops any pending hints from showing, e.g. due to the state being completed. This should only
   * be called if hints & solutions support has been enabled.
   */
  fun stopHintsFromShowing() = hintHandler.reset()

  /**
   * Toggles whether current audio playback is enabled. This should only be called if voiceover
   * support has been enabled.
   */
  fun toggleAudioPlaybackState() {
    if (playerFeatureSet.supportAudioVoiceovers) {
      val audioUiManager = getAudioUiManager()
      if (!isAudioPlaybackEnabled()) {
        audioUiManager?.enableAudioPlayback(audioPlaybackContentId)
      } else {
        audioUiManager?.disableAudioPlayback()
      }
    }
  }

  /**
   * Possibly reads out the specified feedback, interrupting any existing audio that's playing.
   * This should only be called if voiceover support has been enabled.
   */
  fun readOutAnswerFeedback(feedback: SubtitledHtml) {
    if (playerFeatureSet.supportAudioVoiceovers && isAudioPlaybackEnabled()) {
      audioPlaybackContentId = feedback.contentId
      getAudioUiManager()?.loadFeedbackAudio(feedback.contentId, allowAutoPlay = true)
    }
  }

  private fun isAudioPlaybackEnabled(): Boolean {
    return isAudioPlaybackEnabled?.get() == true
  }

  /**
   * Returns the currently [AudioUiManager], if defined. Callers should not cache this value, and
   * are expected to only call this if audio voiceover support is enabled.
   */
  private fun getAudioUiManager(): AudioUiManager? {
    val audioUiManagerRetriever = checkNotNull(this.audioUiManagerRetriever) {
      "Expected audio UI manager retriever to be defined when audio voiceover support is enabled"
    }
    return audioUiManagerRetriever()
  }

  private fun createSubmittedAnswer(
    userAnswer: UserAnswer,
    gcsEntityId: String
  ): SubmittedAnswerViewModel {
    val submittedAnswerViewModel = SubmittedAnswerViewModel(userAnswer, gcsEntityId)
    submittedAnswerViewModel.isCorrectAnswer.set(isCorrectAnswer.get())
    return submittedAnswerViewModel
  }

  private fun createFeedbackItem(feedback: SubtitledHtml, gcsEntityId: String): FeedbackViewModel? {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      return FeedbackViewModel(feedback.html, gcsEntityId)
    }
    return null
  }

  private fun maybeAddNavigationButtons(
    pendingItemList: MutableList<StateItemViewModel>,
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean
  ) {
    val hasPreviousButton = playerFeatureSet.backwardNavigation && hasPreviousState
    when {
      hasGeneralContinueButton && playerFeatureSet.forwardNavigation -> {
        pendingItemList += ContinueNavigationButtonViewModel(
          hasPreviousButton,
          previousNavigationButtonListener,
          fragment as ContinueNavigationButtonListener
        )
      }
      canContinueToNextState && playerFeatureSet.forwardNavigation -> {
        pendingItemList += NextButtonViewModel(
          hasPreviousButton,
          previousNavigationButtonListener,
          fragment as NextNavigationButtonListener
        )
      }
      stateIsTerminal -> {
        if (playerFeatureSet.replaySupport) {
          pendingItemList += ReplayButtonViewModel(fragment as ReplayButtonListener)
        }
        if (playerFeatureSet.returnToTopicNavigation) {
          pendingItemList += ReturnToTopicButtonViewModel(
            hasPreviousButton, previousNavigationButtonListener,
            fragment as ReturnToTopicNavigationButtonListener
          )
        }
      }
      doesMostRecentInteractionRequireExplicitSubmission(pendingItemList) &&
        playerFeatureSet.interactionSupport -> {
        val canSubmitAnswer = checkNotNull(this.canSubmitAnswer) {
          "Expected non-null submit answer observable for submit button when interaction support " +
            "is enabled"
        }
        pendingItemList += SubmitButtonViewModel(
          canSubmitAnswer,
          hasPreviousButton,
          previousNavigationButtonListener,
          fragment as SubmitNavigationButtonListener
        )
      }
      // Otherwise, just show the previous button since the interaction itself will push the answer
      // submission.
      hasPreviousButton && !isMostRecentInteractionAutoNavigating(pendingItemList) -> {
        pendingItemList += PreviousButtonViewModel(
          previousNavigationButtonListener
        )
      }

      // Otherwise, there's no navigation button that should be shown since the current interaction
      // handles this or navigation in this context is disabled.
    }
  }

  /**
   * Returns whether there is currently a pending interaction that requires an additional user
   * action to submit the answer.
   */
  private fun doesMostRecentInteractionRequireExplicitSubmission(
    itemList: List<StateItemViewModel>
  ): Boolean {
    return getPendingAnswerHandler(itemList)?.isExplicitAnswerSubmissionRequired() ?: true
  }

  /**
   * Returns whether there is currently a pending interaction that also acts like a navigation
   * button.
   */
  private fun isMostRecentInteractionAutoNavigating(itemList: List<StateItemViewModel>): Boolean {
    return getPendingAnswerHandler(itemList)?.isAutoNavigating() ?: false
  }

  /**
   * Returns the latest [InteractionAnswerHandler] representing the current pending one, or null if
   * there is none.
   */
  fun getPendingAnswerHandler(itemList: List<StateItemViewModel>): InteractionAnswerHandler? {
    // In the future, it may be ideal to make this more robust by actually tracking the handler
    // corresponding to the pending interaction.
    return itemList.findLast { it is InteractionAnswerHandler } as? InteractionAnswerHandler
  }

  /**
   * Builder to construct new [StatePlayerRecyclerViewAssembler]s in a way that allows granular
   * control over the features enabled by the assembler. Instances of this class should be created
   * using its injectable [Factory].
   */
  class Builder private constructor(
    private val htmlParserFactory: HtmlParser.Factory,
    private val resourceBucketName: String,
    private val entityType: String,
    private val fragment: Fragment,
    private val interactionViewModelFactoryMap: Map<String, InteractionViewModelFactory>,
    private val backgroundCoroutineDispatcher: CoroutineDispatcher
  ) {
    private val adapterBuilder = BindableAdapter.MultiTypeBuilder.newBuilder(
      StateItemViewModel::viewType
    )

    /**
     * Tracks features individually enabled for the assembler. No features are enabled by default.
     */
    private val featureSets = mutableSetOf(PlayerFeatureSet())
    private var congratulationsTextView: TextView? = null
    private var playerRecyclerViewAssemblerViewModel: PlayerRecyclerViewAssemblerViewModel? = null
    private var canSubmitAnswer: ObservableField<Boolean>? = null
    private var audioActivityId: String? = null
    private var currentStateName: ObservableField<String>? = null
    private var isAudioPlaybackEnabled: ObservableField<Boolean>? = null
    private var audioUiManagerRetriever: AudioUiManagerRetriever? = null

    /** Adds support for displaying state content to the learner. */
    fun addContentSupport(): Builder {
      adapterBuilder.registerViewBinder(
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
          val contentViewModel = viewModel as ContentViewModel
          contentViewModel.hasBlueBackground.set(playerRecyclerViewAssemblerViewModel!!.hasBlueBackground)
          contentViewModel.isCenterAligned.set(playerRecyclerViewAssemblerViewModel!!.isCenterAligned)
          binding.viewModel = contentViewModel
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName,
              entityType,
              contentViewModel.gcsEntityId,
              imageCenterAlign = true
            ).parseOppiaHtml(
              contentViewModel.htmlContent.toString(), binding.contentTextView
            )
        }
      )
      featureSets += PlayerFeatureSet(contentSupport = true)
      return this
    }

    /** Adds support for displaying feedback to the user when they submit an answer. */
    fun addFeedbackSupport(): Builder {
      if (fragment is QuestionPlayerFragment) {
        adapterBuilder.registerViewBinder(
          viewType = StateItemViewModel.ViewType.FEEDBACK,
          inflateView = { parent ->
            QuestionPlayerFeedbackItemBinding.inflate(
              LayoutInflater.from(parent.context),
              parent,
              /* attachToParent= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding = DataBindingUtil.findBinding<QuestionPlayerFeedbackItemBinding>(view)!!
            val feedbackViewModel = viewModel as FeedbackViewModel
            binding.htmlContent =
              htmlParserFactory.create(
                resourceBucketName,
                entityType,
                feedbackViewModel.gcsEntityId,
                imageCenterAlign = true
              ).parseOppiaHtml(
                feedbackViewModel.htmlContent.toString(),
                binding.questionPlayerFeedbackTextView
              )
          }
        )
      } else {
        adapterBuilder.registerViewBinder(
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
            val feedbackViewModel = viewModel as FeedbackViewModel
            binding.htmlContent =
              htmlParserFactory.create(
                resourceBucketName,
                entityType,
                feedbackViewModel.gcsEntityId,
                imageCenterAlign = true
              ).parseOppiaHtml(
                feedbackViewModel.htmlContent.toString(),
                binding.feedbackTextView
              )
          }
        )
      }
      featureSets += PlayerFeatureSet(feedbackSupport = true)
      return this
    }

    /**
     * Adds support for rendering interactions and submitting answers to them. The 'continue'
     * interaction is not included since that's considered a navigation interaction.
     *
     * @param canSubmitAnswer an observable boolean that will control whether the interaction
     *     'submit' button is enabled (which can be controlled by interactions in the event that
     *     there's an error which should prevent answer submission).
     */
    fun addInteractionSupport(canSubmitAnswer: ObservableField<Boolean>): Builder {
      if (fragment is QuestionPlayerFragment) {
        adapterBuilder.registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.SELECTION_INTERACTION,
          inflateDataBinding = QuestionPlayerSelectionInteractionItemBinding::inflate,
          setViewModel = QuestionPlayerSelectionInteractionItemBinding::setViewModel,
          transformViewModel = { it as SelectionInteractionViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION,
          inflateDataBinding = FractionInteractionItemBinding::inflate,
          setViewModel = FractionInteractionItemBinding::setViewModel,
          transformViewModel = { it as FractionInteractionViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION,
          inflateDataBinding = NumericInputInteractionItemBinding::inflate,
          setViewModel = NumericInputInteractionItemBinding::setViewModel,
          transformViewModel = { it as NumericInputViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.DRAG_DROP_SORT_INTERACTION,
          inflateDataBinding = DragDropInteractionItemBinding::inflate,
          setViewModel = DragDropInteractionItemBinding::setViewModel,
          transformViewModel = { it as DragAndDropSortInteractionViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION,
          inflateDataBinding = TextInputInteractionItemBinding::inflate,
          setViewModel = TextInputInteractionItemBinding::setViewModel,
          transformViewModel = { it as TextInputViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON,
          inflateDataBinding = SubmitButtonItemBinding::inflate,
          setViewModel = SubmitButtonItemBinding::setButtonViewModel,
          transformViewModel = { it as SubmitButtonViewModel }
        )
      } else {
        adapterBuilder.registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.SELECTION_INTERACTION,
          inflateDataBinding = SelectionInteractionItemBinding::inflate,
          setViewModel = SelectionInteractionItemBinding::setViewModel,
          transformViewModel = { it as SelectionInteractionViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.FRACTION_INPUT_INTERACTION,
          inflateDataBinding = FractionInteractionItemBinding::inflate,
          setViewModel = FractionInteractionItemBinding::setViewModel,
          transformViewModel = { it as FractionInteractionViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.DRAG_DROP_SORT_INTERACTION,
          inflateDataBinding = DragDropInteractionItemBinding::inflate,
          setViewModel = DragDropInteractionItemBinding::setViewModel,
          transformViewModel = { it as DragAndDropSortInteractionViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.NUMERIC_INPUT_INTERACTION,
          inflateDataBinding = NumericInputInteractionItemBinding::inflate,
          setViewModel = NumericInputInteractionItemBinding::setViewModel,
          transformViewModel = { it as NumericInputViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.TEXT_INPUT_INTERACTION,
          inflateDataBinding = TextInputInteractionItemBinding::inflate,
          setViewModel = TextInputInteractionItemBinding::setViewModel,
          transformViewModel = { it as TextInputViewModel }
        ).registerViewDataBinder(
          viewType = StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON,
          inflateDataBinding = SubmitButtonItemBinding::inflate,
          setViewModel = SubmitButtonItemBinding::setButtonViewModel,
          transformViewModel = { it as SubmitButtonViewModel }
        )
      }
      this.canSubmitAnswer = canSubmitAnswer
      featureSets += PlayerFeatureSet(interactionSupport = true)
      return this
    }

    /** Adds support for displaying previously submitted answers. */
    fun addPastAnswersSupport(): Builder {
      if (fragment is QuestionPlayerFragment) {
        adapterBuilder.registerViewBinder(
          viewType = StateItemViewModel.ViewType.SUBMITTED_ANSWER,
          inflateView = { parent ->
            QuestionPlayerSubmittedAnswerItemBinding.inflate(
              LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding =
              DataBindingUtil.findBinding<QuestionPlayerSubmittedAnswerItemBinding>(view)!!
            val submittedAnswerViewModel = viewModel as SubmittedAnswerViewModel
            binding.viewModel = submittedAnswerViewModel
            val userAnswer = submittedAnswerViewModel.submittedUserAnswer
            when (userAnswer.textualAnswerCase) {
              UserAnswer.TextualAnswerCase.HTML_ANSWER -> {
                showSingleAnswer(binding)
                val htmlParser = htmlParserFactory.create(
                  resourceBucketName,
                  entityType,
                  submittedAnswerViewModel.gcsEntityId,
                  imageCenterAlign = false
                )
                binding.submittedAnswer = htmlParser.parseOppiaHtml(
                  userAnswer.htmlAnswer, binding.questionPlayerSubmittedAnswerTextView
                )
              }
              UserAnswer.TextualAnswerCase.LIST_OF_HTML_ANSWERS -> {
                showListOfAnswers(binding)
                binding.submittedListAnswer = userAnswer.listOfHtmlAnswers
                binding.questionPlayerSubmittedAnswerRecyclerView.adapter =
                  createListAnswerAdapter(submittedAnswerViewModel.gcsEntityId)
              }
              else -> {
                showSingleAnswer(binding)
                binding.submittedAnswer = userAnswer.plainAnswer
              }
            }
          }
        )
      } else {
        adapterBuilder.registerViewBinder(
          viewType = StateItemViewModel.ViewType.SUBMITTED_ANSWER,
          inflateView = { parent ->
            SubmittedAnswerItemBinding.inflate(
              LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding = DataBindingUtil.findBinding<SubmittedAnswerItemBinding>(view)!!
            val submittedAnswerViewModel = viewModel as SubmittedAnswerViewModel
            val userAnswer = submittedAnswerViewModel.submittedUserAnswer
            when (userAnswer.textualAnswerCase) {
              UserAnswer.TextualAnswerCase.HTML_ANSWER -> {
                showSingleAnswer(binding)
                val htmlParser = htmlParserFactory.create(
                  resourceBucketName,
                  entityType,
                  submittedAnswerViewModel.gcsEntityId,
                  imageCenterAlign = false
                )
                binding.submittedAnswer = htmlParser.parseOppiaHtml(
                  userAnswer.htmlAnswer,
                  binding.submittedAnswerTextView
                )
              }
              UserAnswer.TextualAnswerCase.LIST_OF_HTML_ANSWERS -> {
                showListOfAnswers(binding)
                binding.submittedListAnswer = userAnswer.listOfHtmlAnswers
                binding.submittedAnswerRecyclerView.adapter =
                  createListAnswerAdapter(submittedAnswerViewModel.gcsEntityId)
              }
              else -> {
                showSingleAnswer(binding)
                binding.submittedAnswer = userAnswer.plainAnswer
              }
            }
          }
        )
      }
      featureSets += PlayerFeatureSet(pastAnswerSupport = true)
      return this
    }

    private fun createListAnswerAdapter(gcsEntityId: String): BindableAdapter<StringList> {
      return BindableAdapter.SingleTypeBuilder
        .newBuilder<StringList>()
        .registerViewBinder(
          inflateView = { parent ->
            SubmittedAnswerListItemBinding.inflate(
              LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding = DataBindingUtil.findBinding<SubmittedAnswerListItemBinding>(view)!!
            binding.answerItem = viewModel
            binding.submittedHtmlAnswerRecyclerView.adapter = createNestedAdapter(gcsEntityId)
          }
        )
        .build()
    }

    private fun createNestedAdapter(gcsEntityId: String): BindableAdapter<String> {
      return BindableAdapter.SingleTypeBuilder
        .newBuilder<String>()
        .registerViewBinder(
          inflateView = { parent ->
            SubmittedHtmlAnswerItemBinding.inflate(
              LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding = DataBindingUtil.findBinding<SubmittedHtmlAnswerItemBinding>(view)!!
            binding.htmlContent =
              htmlParserFactory.create(
                resourceBucketName,
                entityType,
                gcsEntityId,
                /* imageCenterAlign= */ false
              ).parseOppiaHtml(
                viewModel, binding.submittedAnswerContentTextView
              )
          }
        )
        .build()
    }

    private fun showSingleAnswer(binding: ViewDataBinding) {
      when (binding) {
        is QuestionPlayerSubmittedAnswerItemBinding -> {
          binding.questionPlayerSubmittedAnswerRecyclerView.visibility = View.GONE
          binding.questionPlayerSubmittedAnswerTextView.visibility = View.VISIBLE
        }
        is SubmittedAnswerItemBinding -> {
          binding.submittedAnswerRecyclerView.visibility = View.GONE
          binding.submittedAnswerTextView.visibility = View.VISIBLE
        }
      }
    }

    private fun showListOfAnswers(binding: ViewDataBinding) {
      when (binding) {
        is QuestionPlayerSubmittedAnswerItemBinding -> {
          binding.questionPlayerSubmittedAnswerRecyclerView.visibility = View.VISIBLE
          binding.questionPlayerSubmittedAnswerTextView.visibility = View.GONE
        }
        is SubmittedAnswerItemBinding -> {
          binding.submittedAnswerRecyclerView.visibility = View.VISIBLE
          binding.submittedAnswerTextView.visibility = View.GONE
        }
      }
    }

    /**
     * Adds support for automatically collapsing past wrong answers. This feature is not enabled
     * without [addPastAnswersSupport] also being enabled.
     */
    fun addWrongAnswerCollapsingSupport(): Builder {
      adapterBuilder.registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.PREVIOUS_RESPONSES_HEADER,
        inflateDataBinding = PreviousResponsesHeaderItemBinding::inflate,
        setViewModel = PreviousResponsesHeaderItemBinding::setViewModel,
        transformViewModel = { it as PreviousResponsesHeaderViewModel }
      )
      featureSets += PlayerFeatureSet(wrongAnswerCollapsing = true)
      return this
    }

    /** Adds support for navigating to previously completed states. */
    fun addBackwardNavigationSupport(): Builder {
      adapterBuilder.registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.PREVIOUS_NAVIGATION_BUTTON,
        inflateDataBinding = PreviousButtonItemBinding::inflate,
        setViewModel = PreviousButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as PreviousButtonViewModel }
      )
      featureSets += PlayerFeatureSet(backwardNavigation = true)
      return this
    }

    /**
     * Adds support for navigating to next states. Note that this also enables the 'Continue'
     * interaction.
     */
    fun addForwardNavigationSupport(): Builder {
      adapterBuilder.registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.CONTINUE_INTERACTION,
        inflateDataBinding = ContinueInteractionItemBinding::inflate,
        setViewModel = ContinueInteractionItemBinding::setViewModel,
        transformViewModel = { it as ContinueInteractionViewModel }
      ).registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.CONTINUE_NAVIGATION_BUTTON,
        inflateDataBinding = ContinueNavigationButtonItemBinding::inflate,
        setViewModel = ContinueNavigationButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as ContinueNavigationButtonViewModel }
      ).registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.NEXT_NAVIGATION_BUTTON,
        inflateDataBinding = NextButtonItemBinding::inflate,
        setViewModel = NextButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as NextButtonViewModel }
      )
      featureSets += PlayerFeatureSet(forwardNavigation = true)
      return this
    }

    /**
     * Adds support for displaying a button that allows the learner to replay the lesson experience.
     */
    fun addReplayButtonSupport(): Builder {
      adapterBuilder.registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.REPLAY_NAVIGATION_BUTTON,
        inflateDataBinding = ReplayButtonItemBinding::inflate,
        setViewModel = ReplayButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as ReplayButtonViewModel }
      )
      featureSets += PlayerFeatureSet(replaySupport = true)
      return this
    }

    /**
     * Adds support for displaying a 'return to topic' button at the end of the lesson experience.
     */
    fun addReturnToTopicSupport(): Builder {
      adapterBuilder.registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.RETURN_TO_TOPIC_NAVIGATION_BUTTON,
        inflateDataBinding = ReturnToTopicButtonItemBinding::inflate,
        setViewModel = ReturnToTopicButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as ReturnToTopicButtonViewModel }
      )
      featureSets += PlayerFeatureSet(returnToTopicNavigation = true)
      return this
    }

    /**
     * Adds support for displaying a congratulations answer when the learner submits a correct
     * answer.
     */
    fun addCongratulationsForCorrectAnswers(congratulationsTextView: TextView): Builder {
      this.congratulationsTextView = congratulationsTextView
      featureSets += PlayerFeatureSet(showCongratulationsOnCorrectAnswer = true)
      return this
    }

    /**
     * Adds support for displaying with proper alignment and background.
     */
    fun addUISupport(playerRecyclerViewAssemblerViewModel: PlayerRecyclerViewAssemblerViewModel): Builder {
      this.playerRecyclerViewAssemblerViewModel = playerRecyclerViewAssemblerViewModel
      featureSets += PlayerFeatureSet(showCongratulationsOnCorrectAnswer = true)
      return this
    }

    /** Adds support for showing hints & possibly a solution when the learner gets stuck. */
    fun addHintsAndSolutionsSupport(): Builder {
      featureSets += PlayerFeatureSet(hintsAndSolutionsSupport = true)
      return this
    }

    /**
     * Adds support for audio voiceovers, when available, which the learner can use to read out both
     * card contents and feedback responses.
     *
     * @param audioActivityId the specified activity (e.g. exploration) ID
     * @param currentStateName observable field for tracking the name of the currently loaded state.
     *     The value of this field should be retained across configuration changes.
     * @param isAudioPlaybackEnabled observable field tracking whether audio playback is enabled.
     *     The value of this field should be retained across configuration changes.
     * @param audioUiManagerRetriever a synchronous, UI-thread-only retriever of the current
     *     [AudioUiManager], if any is present. This assembler guarantees it will never cache the
     *     manager itself, and will always re-fetch it when performing operations. Callers are
     *     responsible for ensuring consistency across multiple managers. The retriever itself will
     *     be cached, so any implicit references held by it will survive for the lifetime of the
     *     assembler.
     */
    fun addAudioVoiceoverSupport(
      audioActivityId: String,
      currentStateName: ObservableField<String>,
      isAudioPlaybackEnabled: ObservableField<Boolean>,
      audioUiManagerRetriever: () -> AudioUiManager?
    ): Builder {
      this.audioActivityId = audioActivityId
      this.currentStateName = currentStateName
      this.isAudioPlaybackEnabled = isAudioPlaybackEnabled
      this.audioUiManagerRetriever = audioUiManagerRetriever
      featureSets += PlayerFeatureSet(supportAudioVoiceovers = true)
      return this
    }

    /**
     * Returns a new [StatePlayerRecyclerViewAssembler] based on the builder-specified
     * configuration.
     */
    fun build(): StatePlayerRecyclerViewAssembler {
      val playerFeatureSet = featureSets.reduce(PlayerFeatureSet::union)
      return StatePlayerRecyclerViewAssembler(
        adapterBuilder.build(), playerFeatureSet, fragment, congratulationsTextView,
        canSubmitAnswer, audioActivityId, currentStateName, isAudioPlaybackEnabled,
        audioUiManagerRetriever, interactionViewModelFactoryMap, backgroundCoroutineDispatcher
      )
    }

    /** Fragment injectable factory to create new [Builder]s. */
    class Factory @Inject constructor(
      private val htmlParserFactory: HtmlParser.Factory,
      private val fragment: Fragment,
      private val interactionViewModelFactoryMap: Map<
        String, @JvmSuppressWildcards InteractionViewModelFactory>,
      @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
    ) {
      /**
       * Returns a new [Builder] for the specified GCS resource bucket information for loading
       * assets.
       */
      fun create(resourceBucketName: String, entityType: String): Builder {
        return Builder(
          htmlParserFactory,
          resourceBucketName,
          entityType,
          fragment,
          interactionViewModelFactoryMap,
          backgroundCoroutineDispatcher
        )
      }
    }
  }

  /** Feature tracker for the assembler to help dictate how binding should behave. */
  private data class PlayerFeatureSet(
    val contentSupport: Boolean = false,
    val feedbackSupport: Boolean = false,
    val interactionSupport: Boolean = false,
    val pastAnswerSupport: Boolean = false,
    val wrongAnswerCollapsing: Boolean = false,
    val backwardNavigation: Boolean = false,
    val forwardNavigation: Boolean = false,
    val replaySupport: Boolean = false,
    val returnToTopicNavigation: Boolean = false,
    val showCongratulationsOnCorrectAnswer: Boolean = false,
    val hintsAndSolutionsSupport: Boolean = false,
    val supportAudioVoiceovers: Boolean = false
  ) {
    /**
     * Returns a union of this feature set with other one. Loosely based on
     * https://stackoverflow.com/a/49605849.
     */
    fun union(other: PlayerFeatureSet): PlayerFeatureSet {
      return PlayerFeatureSet(
        contentSupport = contentSupport || other.contentSupport,
        feedbackSupport = feedbackSupport || other.feedbackSupport,
        interactionSupport = interactionSupport || other.interactionSupport,
        pastAnswerSupport = pastAnswerSupport || other.pastAnswerSupport,
        wrongAnswerCollapsing = wrongAnswerCollapsing || other.wrongAnswerCollapsing,
        backwardNavigation = backwardNavigation || other.backwardNavigation,
        forwardNavigation = forwardNavigation || other.forwardNavigation,
        replaySupport = replaySupport || other.replaySupport,
        returnToTopicNavigation = returnToTopicNavigation || other.returnToTopicNavigation,
        showCongratulationsOnCorrectAnswer = showCongratulationsOnCorrectAnswer ||
          other.showCongratulationsOnCorrectAnswer,
        hintsAndSolutionsSupport = hintsAndSolutionsSupport || other.hintsAndSolutionsSupport,
        supportAudioVoiceovers = supportAudioVoiceovers || other.supportAudioVoiceovers
      )
    }
  }

  // TODO(#1273): Add thorough testing for hints & solutions.
  /**
   * Handler for showing hints to the learner after a period of time in the event they submit a
   * wrong answer.
   *
   * # Flow chart for when hints are shown
   *
   *            Submit 1st              Submit wrong
   *            wrong answer            answer
   *              +---+                   +---+
   *              |   |                   |   |
   *              |   v                   |   v
   *            +-+---+----+            +-+---+-----+           +----------+
   *     Initial| No       | Wait 60s   |           | View hint | Hint     |
   *     state  | hint     +----------->+ Hint      +---------->+ consumed |
   *     +----->+ released | or, submit | available | Wait 30s  |          |
   *            |          | 2nd wrong  |           +<----------+          |
   *            +----------+ answer     +----+------+           +----+-----+
   *                                         ^                       |
   *                                         |Wait 10s               |
   *                                         |                       |
   *                                    +----+------+                |
   *                               +--->+ No        | Submit wrong   |
   *                   Submit wrong|    | hint      | answer         |
   *                   answer      |    | available +<---------------+
   *                               +----+           |
   *                                    +-----------+
   *
   * # Logic for selecting a hint
   *
   * Hints are selected based on the availability of hints to show, and any previous hints that have
   * been shown. A new hint will only be made available if its previous hint has been viewed by the
   * learner. Hints are always shown in order. If all hints have been exhausted and viewed by the
   * user, then the 'hint available' state in the diagram above will trigger the solution to be
   * made available to view, if a solution is present. Once the solution is viewed by the learner,
   * they will reach a terminal state for hints and no additional hints or solutions will be made
   * available.
   */
  private class HintHandler(
    private val lifecycleSafeTimerFactory: LifecycleSafeTimerFactory,
    private val fragment: Fragment
  ) {
    private var trackedWrongAnswerCount = 0
    private var previousHelpIndex: HelpIndex = HelpIndex.getDefaultInstance()
    private var hintSequenceNumber = 0

    /** Resets this handler to prepare it for a new state, cancelling any pending hints. */
    fun reset() {
      trackedWrongAnswerCount = 0
      previousHelpIndex = HelpIndex.getDefaultInstance()
      // Cancel any potential pending hints by advancing the sequence number. Note that this isn't
      // reset to 0 to ensure that all previous hint tasks are cancelled, and new tasks can be
      // scheduled without overlapping with past sequence numbers.
      hintSequenceNumber++
    }

    /**
     * Handles potentially new wrong answers that were submnitted, and if so schedules a hint to be
     * shown to the user if hints are available.
     */
    fun maybeScheduleShowHint(state: State, pendingState: PendingState) {
      if (state.interaction.hintList.isEmpty()) {
        // If this state has no hints to show, do nothing.
        return
      }

      // Start showing hints after a wrong answer is submitted or if the user appears stuck (e.g.
      // doesn't answer after some duration). Note that if there's already a timer to show a hint,
      // it will be reset for each subsequent answer.
      val nextUnrevealedHintIndex = getNextHintIndexToReveal(state)
      val isFirstHint = previousHelpIndex.indexTypeCase == INDEXTYPE_NOT_SET
      val wrongAnswerCount = pendingState.wrongAnswerList.size
      if (wrongAnswerCount == trackedWrongAnswerCount) {
        // If no answers have been submitted, schedule a task to automatically help after a fixed
        // amount of time. This will automatically reset if something changes other than answers
        // (e.g. revealing a hint), which may trigger more help to become available.
        if (isFirstHint) {
          // The learner needs to wait longer for the initial hint to show since they need some time
          // to read through and consider the question.
          scheduleShowHint(DELAY_SHOW_INITIAL_HINT_MS, nextUnrevealedHintIndex)
        } else {
          scheduleShowHint(DELAY_SHOW_ADDITIONAL_HINTS_MS, nextUnrevealedHintIndex)
        }
      } else {
        // See if the learner's new wrong answer justifies showing a hint.
        if (isFirstHint) {
          if (wrongAnswerCount > 1) {
            // If more than one answer has been submitted and no hint has yet been shown, show a
            // hint immediately since the learner is probably stuck.
            showHintImmediately(nextUnrevealedHintIndex)
          }
        } else {
          // Otherwise, always schedule to show a hint on a new wrong answer for subsequent hints.
          scheduleShowHint(
            DELAY_SHOW_ADDITIONAL_HINTS_FROM_WRONG_ANSWER_MS,
            nextUnrevealedHintIndex
          )
        }
        trackedWrongAnswerCount = wrongAnswerCount
      }
    }

    /**
     * Returns the [HelpIndex] of the next hint or solution that hasn't yet been revealed, or
     * default if there is none.
     */
    private fun getNextHintIndexToReveal(state: State): HelpIndex {
      // Return the index of the first unrevealed hint, or the length of the list if all have been
      // revealed.
      val hintList = state.interaction.hintList
      val solution = state.interaction.solution

      val hasHelp = hintList.isNotEmpty() || solution.hasCorrectAnswer()
      val lastUnrevealedHintIndex = hintList.indices.filterNot { idx ->
        hintList[idx].hintIsRevealed
      }.firstOrNull()

      return if (!hasHelp) {
        HelpIndex.getDefaultInstance()
      } else if (lastUnrevealedHintIndex != null) {
        HelpIndex.newBuilder().setHintIndex(lastUnrevealedHintIndex).build()
      } else if (solution.hasCorrectAnswer() && !solution.solutionIsRevealed) {
        HelpIndex.newBuilder().setShowSolution(true).build()
      } else {
        HelpIndex.newBuilder().setEverythingRevealed(true).build()
      }
    }

    /**
     * Schedules to allow the hint of the specified index to be shown after the specified delay,
     * cancelling any previously pending hints initiated by calls to this method.
     */
    private fun scheduleShowHint(delayMs: Long, helpIndexToShow: HelpIndex) {
      val targetSequenceNumber = ++hintSequenceNumber
      lifecycleSafeTimerFactory.createTimer(delayMs).observe(
        fragment,
        Observer {
          showHint(targetSequenceNumber, helpIndexToShow)
        }
      )
    }

    /**
     * Immediately indicates the specified hint is ready to be shown, cancelling any previously
     * pending hints initiated by calls to [scheduleShowHint].
     */
    private fun showHintImmediately(helpIndexToShow: HelpIndex) {
      showHint(++hintSequenceNumber, helpIndexToShow)
    }

    private fun showHint(targetSequenceNumber: Int, helpIndexToShow: HelpIndex) {
      // Only finish this timer if no other hints were scheduled and no cancellations occurred.
      if (targetSequenceNumber == hintSequenceNumber) {
        if (previousHelpIndex != helpIndexToShow) {
          // Only indicate the hint is available if its index is actually new (including if it
          // becomes null such as in the case of the solution becoming available).
          (fragment as ShowHintAvailabilityListener).onHintAvailable(helpIndexToShow)
          previousHelpIndex = helpIndexToShow
        }
      }
    }
  }
}
