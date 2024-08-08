package org.oppia.android.app.player.state

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineDispatcher
import nl.dionsegijn.konfetti.KonfettiView
import org.oppia.android.R
import org.oppia.android.app.model.AnswerAndResponse
import org.oppia.android.app.model.EphemeralState
import org.oppia.android.app.model.EphemeralState.StateTypeCase
import org.oppia.android.app.model.HelpIndex
import org.oppia.android.app.model.Interaction
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.StringList
import org.oppia.android.app.model.SubtitledHtml
import org.oppia.android.app.model.UserAnswer
import org.oppia.android.app.model.UserAnswerState
import org.oppia.android.app.model.WrittenTranslationContext
import org.oppia.android.app.player.audio.AudioUiManager
import org.oppia.android.app.player.state.StatePlayerRecyclerViewAssembler.Builder.Factory
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerErrorOrAvailabilityCheckReceiver
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerHandler
import org.oppia.android.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.android.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.android.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.ContinueNavigationButtonViewModel
import org.oppia.android.app.player.state.itemviewmodel.DragAndDropSortInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.android.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.ImageRegionSelectionInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.MathExpressionInteractionsViewModel
import org.oppia.android.app.player.state.itemviewmodel.NextButtonViewModel
import org.oppia.android.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.android.app.player.state.itemviewmodel.PreviousButtonViewModel
import org.oppia.android.app.player.state.itemviewmodel.PreviousResponsesHeaderViewModel
import org.oppia.android.app.player.state.itemviewmodel.RatioExpressionInputInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.ReplayButtonViewModel
import org.oppia.android.app.player.state.itemviewmodel.ReturnToTopicButtonViewModel
import org.oppia.android.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.android.app.player.state.itemviewmodel.StateItemViewModel.InteractionItemFactory
import org.oppia.android.app.player.state.itemviewmodel.SubmitButtonViewModel
import org.oppia.android.app.player.state.itemviewmodel.SubmittedAnswerViewModel
import org.oppia.android.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.android.app.player.state.listener.ContinueNavigationButtonListener
import org.oppia.android.app.player.state.listener.NextNavigationButtonListener
import org.oppia.android.app.player.state.listener.PreviousNavigationButtonListener
import org.oppia.android.app.player.state.listener.PreviousResponsesHeaderClickListener
import org.oppia.android.app.player.state.listener.ReplayButtonListener
import org.oppia.android.app.player.state.listener.ReturnToTopicNavigationButtonListener
import org.oppia.android.app.player.state.listener.ShowHintAvailabilityListener
import org.oppia.android.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.android.app.recyclerview.BindableAdapter
import org.oppia.android.app.topic.conceptcard.ConceptCardFragment
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.utility.lifecycle.LifecycleSafeTimerFactory
import org.oppia.android.databinding.ContentItemBinding
import org.oppia.android.databinding.ContinueInteractionItemBinding
import org.oppia.android.databinding.ContinueNavigationButtonItemBinding
import org.oppia.android.databinding.DragDropInteractionItemBinding
import org.oppia.android.databinding.FeedbackItemBinding
import org.oppia.android.databinding.FractionInteractionItemBinding
import org.oppia.android.databinding.ImageRegionSelectionInteractionItemBinding
import org.oppia.android.databinding.MathExpressionInteractionsItemBinding
import org.oppia.android.databinding.NextButtonItemBinding
import org.oppia.android.databinding.NumericInputInteractionItemBinding
import org.oppia.android.databinding.PreviousButtonItemBinding
import org.oppia.android.databinding.PreviousResponsesHeaderItemBinding
import org.oppia.android.databinding.RatioInputInteractionItemBinding
import org.oppia.android.databinding.ReplayButtonItemBinding
import org.oppia.android.databinding.ReturnToTopicButtonItemBinding
import org.oppia.android.databinding.SelectionInteractionItemBinding
import org.oppia.android.databinding.SubmitButtonItemBinding
import org.oppia.android.databinding.SubmittedAnswerItemBinding
import org.oppia.android.databinding.SubmittedAnswerListItemBinding
import org.oppia.android.databinding.SubmittedHtmlAnswerItemBinding
import org.oppia.android.databinding.TextInputInteractionItemBinding
import org.oppia.android.domain.translation.TranslationController
import org.oppia.android.util.accessibility.AccessibilityService
import org.oppia.android.util.parser.html.HtmlParser
import org.oppia.android.util.threading.BackgroundDispatcher
import javax.inject.Inject

private typealias AudioUiManagerRetriever = () -> AudioUiManager?

private const val CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS: Long = 600
private const val CONGRATULATIONS_TEXT_VIEW_VISIBLE_MILLIS: Long = 800

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
  private val accessibilityService: AccessibilityService,
  val adapter: BindableAdapter<StateItemViewModel>,
  val rhsAdapter: BindableAdapter<StateItemViewModel>,
  private val playerFeatureSet: PlayerFeatureSet,
  private val fragment: Fragment,
  private val profileId: ProfileId,
  private val context: Context,
  private val congratulationsTextView: TextView?,
  private val congratulationsTextConfettiView: KonfettiView?,
  private val congratulationsTextConfettiConfig: ConfettiConfig?,
  private val fullScreenConfettiView: KonfettiView?,
  private val endOfSessionConfettiConfig: ConfettiConfig?,
  private val canSubmitAnswer: ObservableField<Boolean>?,
  private val audioActivityId: String?,
  private val currentStateName: ObservableField<String>?,
  private val isAudioPlaybackEnabled: ObservableField<Boolean>?,
  private val audioUiManagerRetriever: AudioUiManagerRetriever?,
  private val interactionViewModelFactoryMap: Map<String, InteractionItemFactory>,
  backgroundCoroutineDispatcher: CoroutineDispatcher,
  private val hasConversationView: Boolean,
  private val resourceHandler: AppLanguageResourceHandler,
  private val translationController: TranslationController,
  private var userAnswerState: UserAnswerState
) : HtmlParser.CustomOppiaTagActionListener {
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

  private val lifecycleSafeTimerFactory = LifecycleSafeTimerFactory(backgroundCoroutineDispatcher)

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

  private val isSplitView = ObservableField<Boolean>(false)

  override fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment.bringToFrontOrCreateIfNew(skillId, profileId, fragment.childFragmentManager)
  }

  /**
   * Computes a list of view models corresponding to the specified [EphemeralState] and the
   * configuration of this assembler, as well as the GCS entity ID that should be associated with
   * rich-text rendering for this state.
   */
  fun compute(
    ephemeralState: EphemeralState,
    gcsEntityId: String,
    isSplitView: Boolean
  ): Pair<List<StateItemViewModel>, List<StateItemViewModel>> {
    this.isSplitView.set(isSplitView)

    val hasPreviousState = ephemeralState.hasPreviousState
    previousAnswerViewModels.clear()
    val conversationPendingItemList = mutableListOf<StateItemViewModel>()
    val extraInteractionPendingItemList = mutableListOf<StateItemViewModel>()
    if (playerFeatureSet.contentSupport) {
      addContentItem(conversationPendingItemList, ephemeralState, gcsEntityId)
    }
    val interaction = ephemeralState.state.interaction

    if (ephemeralState.stateTypeCase == StateTypeCase.PENDING_STATE) {
      if (playerFeatureSet.hintsAndSolutionsSupport) {
        (fragment as ShowHintAvailabilityListener).onHintAvailable(
          ephemeralState.pendingState.helpIndex,
          isCurrentStatePendingState = true
        )
      }
      addPreviousAnswers(
        conversationPendingItemList,
        extraInteractionPendingItemList,
        ephemeralState.pendingState.wrongAnswerList,
        isLastAnswerCorrect = false,
        gcsEntityId,
        ephemeralState.writtenTranslationContext
      )
      if (playerFeatureSet.interactionSupport) {
        val interactionItemList =
          if (isSplitView) extraInteractionPendingItemList else conversationPendingItemList
        val timeToStartNoticeAnimationMs = if (interaction.id == "Continue") {
          ephemeralState.continueButtonAnimationTimestampMs.takeIf {
            ephemeralState.showContinueButtonAnimation
          }
        } else null
        addInteractionForPendingState(
          interactionItemList,
          interaction,
          hasPreviousState,
          gcsEntityId,
          ephemeralState.writtenTranslationContext,
          timeToStartNoticeAnimationMs
        )
      }
    } else if (ephemeralState.stateTypeCase == StateTypeCase.COMPLETED_STATE) {
      // Ensure any lingering hints are properly cleared.
      if (playerFeatureSet.hintsAndSolutionsSupport) {
        (fragment as ShowHintAvailabilityListener).onHintAvailable(
          HelpIndex.getDefaultInstance(),
          isCurrentStatePendingState = false
        )
      }

      // Ensure the answer is marked in situations where that's guaranteed (e.g. completed state)
      // so that the UI always has the correct answer indication, even after configuration changes.
      addPreviousAnswers(
        conversationPendingItemList,
        extraInteractionPendingItemList,
        ephemeralState.completedState.answerList,
        isLastAnswerCorrect = true,
        gcsEntityId,
        ephemeralState.writtenTranslationContext
      )
    }

    val isTerminalState = ephemeralState.stateTypeCase == StateTypeCase.TERMINAL_STATE
    var canContinueToNextState = false
    var hasGeneralContinueButton = false
    if (!isTerminalState) {
      if (ephemeralState.stateTypeCase == StateTypeCase.COMPLETED_STATE &&
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

    if (isTerminalState && playerFeatureSet.showCelebrationAtEndOfSession) {
      maybeShowCelebrationForEndOfSession()
    }

    maybeAddNavigationButtons(
      conversationPendingItemList,
      extraInteractionPendingItemList,
      hasPreviousState,
      canContinueToNextState,
      hasGeneralContinueButton,
      isTerminalState,
      shouldAnimateContinueButton = ephemeralState.showContinueButtonAnimation,
      continueButtonAnimationTimestampMs = ephemeralState.continueButtonAnimationTimestampMs
    )
    return Pair(conversationPendingItemList, extraInteractionPendingItemList)
  }

  private fun addInteractionForPendingState(
    pendingItemList: MutableList<StateItemViewModel>,
    interaction: Interaction,
    hasPreviousButton: Boolean,
    gcsEntityId: String,
    writtenTranslationContext: WrittenTranslationContext,
    timeToStartNoticeAnimationMs: Long?
  ) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory.create(
      gcsEntityId,
      hasConversationView,
      interaction,
      fragment as InteractionAnswerReceiver,
      fragment as InteractionAnswerErrorOrAvailabilityCheckReceiver,
      hasPreviousButton,
      isSplitView.get()!!,
      writtenTranslationContext,
      timeToStartNoticeAnimationMs,
      userAnswerState
    )
  }

  /** Reset userAnswerState once the user submits an answer. */
  fun resetUserAnswerState() {
    userAnswerState = UserAnswerState.getDefaultInstance()
  }

  private fun addContentItem(
    pendingItemList: MutableList<StateItemViewModel>,
    ephemeralState: EphemeralState,
    gcsEntityId: String
  ) {
    val contentSubtitledHtml =
      translationController.extractString(
        ephemeralState.state.content, ephemeralState.writtenTranslationContext
      )
    if (contentSubtitledHtml.isNotEmpty()) {
      pendingItemList += ContentViewModel(
        contentSubtitledHtml,
        gcsEntityId,
        hasConversationView,
        isSplitView.get()!!,
        playerFeatureSet.conceptCardSupport
      )
    }
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>,
    rightPendingItemList: MutableList<StateItemViewModel>,
    answersAndResponses: List<AnswerAndResponse>,
    isLastAnswerCorrect: Boolean,
    gcsEntityId: String,
    writtenTranslationContext: WrittenTranslationContext
  ) {
    if (answersAndResponses.size > 1) {
      if (playerFeatureSet.wrongAnswerCollapsing) {
        PreviousResponsesHeaderViewModel(
          answersAndResponses.size - 1,
          hasConversationView,
          ObservableBoolean(hasPreviousResponsesExpanded),
          fragment as PreviousResponsesHeaderClickListener,
          isSplitView.get()!!,
          resourceHandler
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
          // Earlier answers can't be correct (since otherwise new answers wouldn't be able to be
          // submitted), hence the assumption that these aren't.
          createSubmittedAnswer(
            answerAndResponse.userAnswer,
            gcsEntityId,
            /* isAnswerCorrect= */ false
          )?.let { viewModel ->
            if (showPreviousAnswers) {
              pendingItemList += viewModel
            }
            previousAnswerViewModels += viewModel
          }
        }
        if (playerFeatureSet.feedbackSupport) {
          createFeedbackItem(
            answerAndResponse.feedback,
            gcsEntityId,
            writtenTranslationContext
          )?.let { viewModel ->
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
        if (isLastAnswerCorrect && isSplitView.get()!!) {
          createSubmittedAnswer(
            answerAndResponse.userAnswer,
            gcsEntityId,
            isAnswerCorrect = true
          )?.let(rightPendingItemList::add)
        } else {
          createSubmittedAnswer(
            answerAndResponse.userAnswer,
            gcsEntityId,
            isLastAnswerCorrect || answerAndResponse.isCorrectAnswer
          )?.let(pendingItemList::add)
        }
      }
      if (playerFeatureSet.feedbackSupport) {
        createFeedbackItem(answerAndResponse.feedback, gcsEntityId, writtenTranslationContext)?.let(
          pendingItemList::add
        )
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

  /**
   * Shows a celebratory animation with a congratulations message and confetti when the learner submits
   * a correct answer.
   *
   * @param feedback Oppia's feedback to the learner's most recent answer. If the feedback is empty,
   *     talkback confirms correct answers by a separate announcement.
   */
  fun showCelebrationOnCorrectAnswer(feedback: SubtitledHtml) {
    check(playerFeatureSet.showCelebrationOnCorrectAnswer) {
      "Cannot show congratulations message for assembler that doesn't support it"
    }
    val textView = checkNotNull(congratulationsTextView) {
      "Expected non-null reference to congratulations text view"
    }
    val confettiView = checkNotNull(congratulationsTextConfettiView) {
      "Expected non-null reference to congratulations text confetti view"
    }
    val confettiConfig = checkNotNull(congratulationsTextConfettiConfig) {
      "Expected non-null reference to confetti animation configuration"
    }
    createBannerConfetti(confettiView, confettiConfig)
    animateCongratulationsTextView(textView)

    if (feedback.html.isBlank()) {
      accessibilityService.announceForAccessibilityForView(
        textView,
        resourceHandler.getStringInLocale(R.string.correct)
      )
    }
  }

  /** Shows confetti when the learner reaches the end of an exploration session. */
  private fun maybeShowCelebrationForEndOfSession() {
    check(playerFeatureSet.showCelebrationAtEndOfSession) {
      "Cannot show end of session confetti for assembler that doesn't support it"
    }
    val confettiView = checkNotNull(fullScreenConfettiView) {
      "Expected non-null reference to full screen confetti view"
    }
    val confettiConfig = checkNotNull(endOfSessionConfettiConfig) {
      "Expected non-null reference to confetti animation configuration"
    }
    if (!confettiView.isActive()) {
      // If learners toggle back and forth from the end of the exploration we only show the confetti one
      // instance at a time.
      createEndOfSessionConfetti(confettiView, confettiConfig)
    }
  }

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
    gcsEntityId: String,
    isAnswerCorrect: Boolean
  ): SubmittedAnswerViewModel? {
    return userAnswer.takeIf { it.hasAnswerToDisplayToUser() }?.let {
      SubmittedAnswerViewModel(
        userAnswer,
        gcsEntityId,
        hasConversationView,
        isSplitView.get()!!,
        playerFeatureSet.conceptCardSupport,
        resourceHandler
      ).also { submittedAnswerViewModel ->
        submittedAnswerViewModel.setIsCorrectAnswer(isAnswerCorrect)
        submittedAnswerViewModel.isExtraInteractionAnswerCorrect.set(isAnswerCorrect)
      }
    }
  }

  private fun createFeedbackItem(
    feedback: SubtitledHtml,
    gcsEntityId: String,
    writtenTranslationContext: WrittenTranslationContext
  ): FeedbackViewModel? {
    // Only show feedback if there's some to show.
    val feedbackHtml = translationController.extractString(feedback, writtenTranslationContext)
    if (feedbackHtml.isNotEmpty()) {
      return FeedbackViewModel(
        feedbackHtml,
        gcsEntityId,
        hasConversationView,
        isSplitView.get()!!,
        playerFeatureSet.conceptCardSupport
      )
    }
    return null
  }

  private fun maybeAddNavigationButtons(
    conversationPendingItemList: MutableList<StateItemViewModel>,
    extraInteractionPendingItemList: MutableList<StateItemViewModel>,
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean,
    shouldAnimateContinueButton: Boolean,
    continueButtonAnimationTimestampMs: Long
  ) {
    val hasPreviousButton = playerFeatureSet.backwardNavigation && hasPreviousState
    when {
      hasGeneralContinueButton && playerFeatureSet.forwardNavigation -> {
        addContinueNavigation(
          conversationPendingItemList,
          extraInteractionPendingItemList,
          hasPreviousButton,
          shouldAnimateContinueButton,
          continueButtonAnimationTimestampMs
        )
      }
      canContinueToNextState && playerFeatureSet.forwardNavigation -> {
        addNextButtonNavigation(
          conversationPendingItemList,
          extraInteractionPendingItemList,
          hasPreviousButton
        )
      }
      stateIsTerminal -> {
        if (playerFeatureSet.replaySupport) {
          addReplyButton(conversationPendingItemList, extraInteractionPendingItemList)
        }
        if (playerFeatureSet.returnToTopicNavigation) {
          addReturnTopTopicNavigation(
            conversationPendingItemList,
            extraInteractionPendingItemList,
            hasPreviousButton
          )
        }
      }
      doesMostRecentInteractionRequireExplicitSubmission(conversationPendingItemList) &&
        playerFeatureSet.interactionSupport -> {
        addSubmitButton(
          conversationPendingItemList,
          extraInteractionPendingItemList,
          hasPreviousButton
        )
      }
      // Otherwise, just show the previous button since the interaction itself will push the answer
      // submission.
      !isMostRecentInteractionAutoNavigating(conversationPendingItemList) -> {
        addPreviousButtonNavigation(hasPreviousButton, conversationPendingItemList)
      }
      // Otherwise, there's no navigation button that should be shown since the current interaction
      // handles this or navigation in this context is disabled.
    }
  }

  private fun addSubmitButton(
    conversationPendingItemList: MutableList<StateItemViewModel>,
    extraInteractionPendingItemList: MutableList<StateItemViewModel>,
    hasPreviousButton: Boolean
  ) {
    val canSubmitAnswer = checkNotNull(this.canSubmitAnswer) {
      "Expected non-null submit answer observable for submit button when interaction support " +
        "is enabled"
    }
    val targetList =
      if (isSplitView.get()!!) extraInteractionPendingItemList else conversationPendingItemList
    val hasPrevious = if (isSplitView.get()!!) false else hasPreviousButton
    targetList += SubmitButtonViewModel(
      canSubmitAnswer,
      hasConversationView,
      hasPrevious,
      previousNavigationButtonListener,
      fragment as SubmitNavigationButtonListener,
      isSplitView.get()!!
    )
    if (isSplitView.get()!!) {
      // "previous button" should appear in the conversation recycler view only
      addPreviousButtonNavigation(hasPreviousButton, conversationPendingItemList)
    }
  }

  private fun addReturnTopTopicNavigation(
    conversationPendingItemList: MutableList<StateItemViewModel>,
    extraInteractionPendingItemList: MutableList<StateItemViewModel>,
    hasPreviousButton: Boolean
  ) {
    val targetList =
      if (isSplitView.get()!!) extraInteractionPendingItemList else conversationPendingItemList
    val hasPrevious = if (isSplitView.get()!!) false else hasPreviousButton
    targetList += ReturnToTopicButtonViewModel(
      hasPrevious,
      hasConversationView,
      previousNavigationButtonListener,
      fragment as ReturnToTopicNavigationButtonListener,
      isSplitView.get()!!
    )
    if (isSplitView.get()!!) {
      // "previous button" should appear in the conversation recycler view only
      addPreviousButtonNavigation(hasPreviousButton, conversationPendingItemList)
    }
  }

  private fun addReplyButton(
    conversationPendingItemList: MutableList<StateItemViewModel>,
    extraInteractionPendingItemList: MutableList<StateItemViewModel>
  ) {
    val targetList =
      if (isSplitView.get()!!) extraInteractionPendingItemList else conversationPendingItemList
    targetList +=
      ReplayButtonViewModel(
        hasConversationView,
        fragment as ReplayButtonListener,
        isSplitView.get()!!
      )
  }

  private fun addNextButtonNavigation(
    conversationPendingItemList: MutableList<StateItemViewModel>,
    extraInteractionPendingItemList: MutableList<StateItemViewModel>,
    hasPreviousButton: Boolean
  ) {
    val targetList =
      if (isSplitView.get()!!) extraInteractionPendingItemList else conversationPendingItemList
    val hasPrevious = if (isSplitView.get()!!) false else hasPreviousButton
    targetList += NextButtonViewModel(
      hasPrevious,
      hasConversationView,
      previousNavigationButtonListener,
      fragment as NextNavigationButtonListener,
      isSplitView.get()!!
    )
    if (isSplitView.get()!!) {
      // "previous button" should appear in the conversation recycler view only
      addPreviousButtonNavigation(hasPreviousButton, conversationPendingItemList)
    }
  }

  private fun addContinueNavigation(
    conversationPendingItemList: MutableList<StateItemViewModel>,
    extraInteractionPendingItemList: MutableList<StateItemViewModel>,
    hasPreviousButton: Boolean,
    shouldAnimateContinueButton: Boolean,
    continueButtonAnimationTimestampMs: Long
  ) {
    val targetList =
      if (isSplitView.get()!!) extraInteractionPendingItemList else conversationPendingItemList
    val hasPrevious = if (isSplitView.get()!!) false else hasPreviousButton
    targetList += ContinueNavigationButtonViewModel(
      hasPrevious,
      hasConversationView,
      previousNavigationButtonListener,
      fragment as ContinueNavigationButtonListener,
      isSplitView.get()!!,
      shouldAnimateContinueButton,
      continueButtonAnimationTimestampMs
    )
    if (isSplitView.get()!!) {
      // "previous button" should appear in the conversation recycler view only
      addPreviousButtonNavigation(hasPreviousButton, conversationPendingItemList)
    }
  }

  private fun addPreviousButtonNavigation(
    hasPreviousButton: Boolean,
    itemList: MutableList<StateItemViewModel>
  ) {
    if (hasPreviousButton) {
      itemList += PreviousButtonViewModel(
        hasConversationView,
        previousNavigationButtonListener,
        isSplitView.get()!!
      )
    }
  }

  private fun createBannerConfetti(confettiView: KonfettiView, config: ConfettiConfig) {
    val width = confettiView.width.toFloat()
    val height = confettiView.height.toFloat()
    // Set confetti lifetime to be the same as the congratulations text view.
    val timeToLiveMs = CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS +
      CONGRATULATIONS_TEXT_VIEW_VISIBLE_MILLIS +
      CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS
    val colorsList = ConfettiConfig.primaryColors.map { getColor(context, it) }

    config.startConfettiBurst(
      confettiView,
      xPosition = width / 3,
      yPosition = height / 2,
      minAngle = 180.0,
      maxAngle = 270.0,
      timeToLiveMs,
      delayMs = 0L,
      colorsList
    )
    config.startConfettiBurst(
      confettiView,
      xPosition = width * 2 / 3,
      yPosition = height / 2,
      minAngle = 270.0,
      maxAngle = 370.0,
      timeToLiveMs,
      delayMs = 0L,
      colorsList
    )
  }

  private fun animateCongratulationsTextView(congratulationsText: TextView) {
    congratulationsText.visibility = View.VISIBLE
    val fullAnimationMs = CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS +
      CONGRATULATIONS_TEXT_VIEW_VISIBLE_MILLIS + CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS

    val fadeIn = AlphaAnimation(0f, 1f)
    fadeIn.interpolator = DecelerateInterpolator()
    fadeIn.duration = CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS

    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.startOffset = CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS +
      CONGRATULATIONS_TEXT_VIEW_VISIBLE_MILLIS
    fadeOut.duration = CONGRATULATIONS_TEXT_VIEW_FADE_MILLIS

    val animation = AnimationSet(false)
    animation.addAnimation(fadeIn)
    animation.addAnimation(fadeOut)
    congratulationsText.animation = animation

    lifecycleSafeTimerFactory.createTimer(fullAnimationMs).observe(
      fragment,
      Observer {
        congratulationsText.clearAnimation()
        congratulationsText.visibility = View.INVISIBLE
      }
    )
  }

  private fun createEndOfSessionConfetti(confettiView: KonfettiView, config: ConfettiConfig) {
    val timeToLiveMillis = 4000L
    val delayMillis = 500L
    val colorsList = ConfettiConfig.primaryColors.map { getColor(context, it) }

    config.startConfettiBurst(
      confettiView,
      xPosition = 0f,
      yPosition = 0f,
      minAngle = -90.0,
      maxAngle = 90.0,
      timeToLiveMillis,
      delayMillis,
      colorsList
    )
    config.startConfettiBurst(
      confettiView,
      xPosition = confettiView.width.toFloat(),
      yPosition = 0f,
      minAngle = 90.0,
      maxAngle = 270.0,
      timeToLiveMillis,
      delayMillis,
      colorsList
    )
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
    private var accessibilityService: AccessibilityService,
    private val htmlParserFactory: HtmlParser.Factory,
    private val resourceBucketName: String,
    private val entityType: String,
    private val fragment: Fragment,
    private val profileId: ProfileId,
    private val context: Context,
    private val interactionViewModelFactoryMap: Map<String, InteractionItemFactory>,
    private val backgroundCoroutineDispatcher: CoroutineDispatcher,
    private val resourceHandler: AppLanguageResourceHandler,
    private val translationController: TranslationController,
    private val multiTypeBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
    private val singleTypeBuilderFactory: BindableAdapter.SingleTypeBuilder.Factory,
    private val userAnswerState: UserAnswerState
  ) {

    private val adapterBuilder: BindableAdapter.MultiTypeBuilder<StateItemViewModel,
      StateItemViewModel.ViewType> = multiTypeBuilderFactory.create { it.viewType }

    /**
     * Tracks features individually enabled for the assembler. No features are enabled by default.
     */
    private val featureSets = mutableSetOf(PlayerFeatureSet())
    private var congratulationsTextView: TextView? = null
    private var congratulationsTextConfettiView: KonfettiView? = null
    private var congratulationsTextConfettiConfig: ConfettiConfig? = null
    private var fullScreenConfettiView: KonfettiView? = null
    private var endOfSessionConfettiConfig: ConfettiConfig? = null
    private var hasConversationView: Boolean = true
    private var canSubmitAnswer: ObservableField<Boolean>? = null
    private var audioActivityId: String? = null
    private var currentStateName: ObservableField<String>? = null
    private var isAudioPlaybackEnabled: ObservableField<Boolean>? = null
    private var audioUiManagerRetriever: AudioUiManagerRetriever? = null
    private val customTagListener = object : HtmlParser.CustomOppiaTagActionListener {
      var proxyListener: HtmlParser.CustomOppiaTagActionListener? = null

      override fun onConceptCardLinkClicked(view: View, skillId: String) {
        proxyListener?.onConceptCardLinkClicked(view, skillId)
      }
    }

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
          binding.viewModel = contentViewModel
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName,
              entityType,
              contentViewModel.gcsEntityId,
              imageCenterAlign = true,
              customOppiaTagActionListener = customTagListener,
              displayLocale = resourceHandler.getDisplayLocale()
            ).parseOppiaHtml(
              contentViewModel.htmlContent.toString(),
              binding.contentTextView,
              supportsLinks = true,
              supportsConceptCards = contentViewModel.supportsConceptCards
            )
        }
      )
      featureSets += PlayerFeatureSet(contentSupport = true)
      return this
    }

    /** Adds support for displaying feedback to the user when they submit an answer. */
    fun addFeedbackSupport(): Builder {
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
          binding.viewModel = feedbackViewModel
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName,
              entityType,
              feedbackViewModel.gcsEntityId,
              imageCenterAlign = true,
              customOppiaTagActionListener = customTagListener,
              displayLocale = resourceHandler.getDisplayLocale()
            ).parseOppiaHtml(
              feedbackViewModel.htmlContent.toString(),
              binding.feedbackTextView,
              supportsLinks = true,
              supportsConceptCards = feedbackViewModel.supportsConceptCards
            )
        }
      )
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
        viewType = StateItemViewModel.ViewType.IMAGE_REGION_SELECTION_INTERACTION,
        inflateDataBinding = ImageRegionSelectionInteractionItemBinding::inflate,
        setViewModel = ImageRegionSelectionInteractionItemBinding::setViewModel,
        transformViewModel = { it as ImageRegionSelectionInteractionViewModel }
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
        viewType = StateItemViewModel.ViewType.RATIO_EXPRESSION_INPUT_INTERACTION,
        inflateDataBinding = RatioInputInteractionItemBinding::inflate,
        setViewModel = RatioInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as RatioExpressionInputInteractionViewModel }
      ).registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.NUMERIC_EXPRESSION_INPUT_INTERACTION,
        inflateDataBinding = MathExpressionInteractionsItemBinding::inflate,
        setViewModel = MathExpressionInteractionsItemBinding::setViewModel,
        transformViewModel = { it as MathExpressionInteractionsViewModel }
      ).registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.ALGEBRAIC_EXPRESSION_INPUT_INTERACTION,
        inflateDataBinding = MathExpressionInteractionsItemBinding::inflate,
        setViewModel = MathExpressionInteractionsItemBinding::setViewModel,
        transformViewModel = { it as MathExpressionInteractionsViewModel }
      ).registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.MATH_EQUATION_INPUT_INTERACTION,
        inflateDataBinding = MathExpressionInteractionsItemBinding::inflate,
        setViewModel = MathExpressionInteractionsItemBinding::setViewModel,
        transformViewModel = { it as MathExpressionInteractionsViewModel }
      ).registerViewDataBinder(
        viewType = StateItemViewModel.ViewType.SUBMIT_ANSWER_BUTTON,
        inflateDataBinding = SubmitButtonItemBinding::inflate,
        setViewModel = SubmitButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as SubmitButtonViewModel }
      )
      this.canSubmitAnswer = canSubmitAnswer
      featureSets += PlayerFeatureSet(interactionSupport = true)
      return this
    }

    /** Adds support for displaying previously submitted answers. */
    fun addPastAnswersSupport(): Builder {
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
          binding.viewModel = submittedAnswerViewModel
          val userAnswer = submittedAnswerViewModel.submittedUserAnswer
          when (userAnswer.textualAnswerCase) {
            UserAnswer.TextualAnswerCase.HTML_ANSWER -> {
              showSingleAnswer(binding)
              val accessibleAnswer = if (userAnswer.contentDescription.isNotEmpty()) {
                userAnswer.contentDescription
              } else null
              val htmlParser = htmlParserFactory.create(
                resourceBucketName,
                entityType,
                submittedAnswerViewModel.gcsEntityId,
                imageCenterAlign = false,
                customOppiaTagActionListener = customTagListener,
                displayLocale = resourceHandler.getDisplayLocale()
              )
              submittedAnswerViewModel.setSubmittedAnswer(
                htmlParser.parseOppiaHtml(
                  userAnswer.htmlAnswer,
                  binding.submittedAnswerTextView,
                  supportsConceptCards = submittedAnswerViewModel.supportsConceptCards
                ),
                accessibleAnswer
              )
            }
            UserAnswer.TextualAnswerCase.LIST_OF_HTML_ANSWERS -> {
              showListOfAnswers(binding)
              binding.submittedListAnswer = userAnswer.listOfHtmlAnswers
              binding.submittedAnswerRecyclerView.adapter =
                createListAnswerAdapter(
                  submittedAnswerViewModel.gcsEntityId,
                  submittedAnswerViewModel.supportsConceptCards
                )
            }
            else -> {
              showSingleAnswer(binding)
              submittedAnswerViewModel.setSubmittedAnswer(
                userAnswer.plainAnswer, accessibleAnswer = userAnswer.contentDescription
              )
            }
          }
        }
      )
      featureSets += PlayerFeatureSet(pastAnswerSupport = true)
      return this
    }

    private fun createListAnswerAdapter(
      gcsEntityId: String,
      supportsConceptCards: Boolean
    ): BindableAdapter<StringList> {
      return singleTypeBuilderFactory.create<StringList>()
        .registerViewBinder(
          inflateView = { parent ->
            SubmittedAnswerListItemBinding.inflate(
              LayoutInflater.from(parent.context), parent, /* attachToParent= */ false
            ).root
          },
          bindView = { view, viewModel ->
            val binding = DataBindingUtil.findBinding<SubmittedAnswerListItemBinding>(view)!!
            binding.answerItem = viewModel
            binding.submittedHtmlAnswerRecyclerView.adapter =
              createNestedAdapter(gcsEntityId, supportsConceptCards)
          }
        )
        .build()
    }

    private fun createNestedAdapter(
      gcsEntityId: String,
      supportsConceptCards: Boolean
    ): BindableAdapter<String> {
      return singleTypeBuilderFactory.create<String>()
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
                imageCenterAlign = false,
                customOppiaTagActionListener = customTagListener,
                displayLocale = resourceHandler.getDisplayLocale()
              ).parseOppiaHtml(
                viewModel,
                binding.submittedAnswerContentTextView,
                supportsConceptCards = supportsConceptCards
              )
          }
        )
        .build()
    }

    private fun showSingleAnswer(binding: ViewDataBinding) {
      when (binding) {
        is SubmittedAnswerItemBinding -> {
          binding.submittedAnswerRecyclerView.visibility = View.GONE
          binding.submittedAnswerTextView.visibility = View.VISIBLE
        }
      }
    }

    private fun showListOfAnswers(binding: ViewDataBinding) {
      when (binding) {
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
    fun addCelebrationForCorrectAnswers(
      congratulationsTextView: TextView,
      congratulationsTextConfettiView: KonfettiView,
      confettiConfig: ConfettiConfig
    ): Builder {
      this.congratulationsTextView = congratulationsTextView
      this.congratulationsTextConfettiView = congratulationsTextConfettiView
      this.congratulationsTextConfettiConfig = confettiConfig
      featureSets += PlayerFeatureSet(showCelebrationOnCorrectAnswer = true)
      return this
    }

    /**
     * Adds support for displaying a confetti animation when the learner completes an entire
     * exploration.
     */
    fun addCelebrationForEndOfSession(
      fullScreenConfettiView: KonfettiView,
      confettiConfig: ConfettiConfig
    ): Builder {
      this.endOfSessionConfettiConfig = confettiConfig
      this.fullScreenConfettiView = fullScreenConfettiView
      featureSets += PlayerFeatureSet(showCelebrationAtEndOfSession = true)
      return this
    }

    /**
     * Adds support for displaying with proper alignment and background.
     */
    fun hasConversationView(hasConversationView: Boolean): Builder {
      this.hasConversationView = hasConversationView
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

    /** Adds support for enabling concept cards links in explorations when the user gets stuck. */
    fun addConceptCardSupport(): Builder {
      featureSets += PlayerFeatureSet(conceptCardSupport = true)
      return this
    }

    /**
     * Returns a new [StatePlayerRecyclerViewAssembler] based on the builder-specified
     * configuration.
     */
    fun build(): StatePlayerRecyclerViewAssembler {
      val playerFeatureSet = featureSets.reduce(PlayerFeatureSet::union)
      val assembler = StatePlayerRecyclerViewAssembler(
        accessibilityService,
        /* adapter= */ adapterBuilder.build(),
        /* rhsAdapter= */ adapterBuilder.build(),
        playerFeatureSet,
        fragment,
        profileId,
        context,
        congratulationsTextView,
        congratulationsTextConfettiView,
        congratulationsTextConfettiConfig,
        fullScreenConfettiView,
        endOfSessionConfettiConfig,
        canSubmitAnswer,
        audioActivityId,
        currentStateName,
        isAudioPlaybackEnabled,
        audioUiManagerRetriever,
        interactionViewModelFactoryMap,
        backgroundCoroutineDispatcher,
        hasConversationView,
        resourceHandler,
        translationController,
        userAnswerState
      )
      if (playerFeatureSet.conceptCardSupport) {
        customTagListener.proxyListener = assembler
      }
      return assembler
    }

    /** Fragment injectable factory to create new [Builder]s. */
    class Factory @Inject constructor(
      private val accessibilityService: AccessibilityService,
      private val htmlParserFactory: HtmlParser.Factory,
      private val fragment: Fragment,
      private val context: Context,
      private val interactionViewModelFactoryMap: Map<
        String, @JvmSuppressWildcards InteractionItemFactory>,
      @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher,
      private val resourceHandler: AppLanguageResourceHandler,
      private val translationController: TranslationController,
      private val multiAdapterBuilderFactory: BindableAdapter.MultiTypeBuilder.Factory,
      private val singleAdapterFactory: BindableAdapter.SingleTypeBuilder.Factory
    ) {
      /**
       * Returns a new [Builder] for the specified GCS resource bucket information for loading
       * assets, and the current logged in [ProfileId].
       */
      fun create(
        resourceBucketName: String,
        entityType: String,
        profileId: ProfileId,
        userAnswerState: UserAnswerState
      ): Builder {
        return Builder(
          accessibilityService,
          htmlParserFactory,
          resourceBucketName,
          entityType,
          fragment,
          profileId,
          context,
          interactionViewModelFactoryMap,
          backgroundCoroutineDispatcher,
          resourceHandler,
          translationController,
          multiAdapterBuilderFactory,
          singleAdapterFactory,
          userAnswerState
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
    val showCelebrationOnCorrectAnswer: Boolean = false,
    val showCelebrationAtEndOfSession: Boolean = false,
    val hintsAndSolutionsSupport: Boolean = false,
    val supportAudioVoiceovers: Boolean = false,
    val conceptCardSupport: Boolean = false
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
        showCelebrationOnCorrectAnswer = showCelebrationOnCorrectAnswer ||
          other.showCelebrationOnCorrectAnswer,
        showCelebrationAtEndOfSession = showCelebrationAtEndOfSession ||
          other.showCelebrationAtEndOfSession,
        hintsAndSolutionsSupport = hintsAndSolutionsSupport || other.hintsAndSolutionsSupport,
        supportAudioVoiceovers = supportAudioVoiceovers || other.supportAudioVoiceovers,
        conceptCardSupport = conceptCardSupport || other.conceptCardSupport
      )
    }
  }

  private companion object {
    private fun UserAnswer.hasAnswerToDisplayToUser(): Boolean {
      return when (textualAnswerCase) {
        UserAnswer.TextualAnswerCase.HTML_ANSWER -> htmlAnswer.isNotEmpty()
        UserAnswer.TextualAnswerCase.PLAIN_ANSWER -> plainAnswer.isNotEmpty()
        UserAnswer.TextualAnswerCase.LIST_OF_HTML_ANSWERS ->
          listOfHtmlAnswers.setOfHtmlStringsOrBuilderList.isNotEmpty()
        UserAnswer.TextualAnswerCase.TEXTUALANSWER_NOT_SET, null -> false
      }
    }
  }
}
