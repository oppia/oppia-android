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
import androidx.databinding.ObservableList
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.ContinueNavigationButtonItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NextButtonItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.PreviousButtonItemBinding
import org.oppia.app.databinding.PreviousResponsesHeaderItemBinding
import org.oppia.app.databinding.ReplayButtonItemBinding
import org.oppia.app.databinding.ReturnToTopicButtonItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.SubmitButtonItemBinding
import org.oppia.app.databinding.SubmittedAnswerItemBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Interaction
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.state.StatePlayerRecyclerViewAssembler.Builder.Factory
import org.oppia.app.player.state.answerhandling.InteractionAnswerErrorReceiver
import org.oppia.app.player.state.answerhandling.InteractionAnswerHandler
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
import org.oppia.app.player.state.listener.SubmitNavigationButtonListener
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.util.parser.HtmlParser
import org.oppia.util.threading.BackgroundDispatcher
import javax.inject.Inject

/**
 * An assembler for generating the list of view models to bind to the state player recycler view. This class also
 * handles some non-recycler view feature management, such as the congratulations message for a correct answer.
 *
 * One instance of this class should exist per fragment hosting the underlying recycler view. It's expected that this
 * class be reconstructed on configuration changes, as it nor its contents are directly parcelable. The state loss from
 * recreating this class will have the expected behavior so long as the next assembler has the same feature set as the
 * one being destroyed.
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
  val adapter: BindableAdapter<StateItemViewModel>, private val playerFeatureSet: PlayerFeatureSet,
  private val fragment: Fragment, private val congratulationsTextView: TextView?,
  private val interactionViewModelFactoryMap: Map<String, @JvmSuppressWildcards InteractionViewModelFactory>,
  backgroundCoroutineDispatcher: CoroutineDispatcher
) {
  /**
   * A list of view models corresponding to past view models that are hidden by default. These are intentionally not
   * retained upon configuration changes since the user can just re-expand the list. Note that the first element of this
   * list (when initialized), will always be the previous answers header to help locate the items in the recycler view
   * (when present).
   */
  private val previousAnswerViewModels: MutableList<StateItemViewModel> = mutableListOf()
  /**
   * Whether the previously submitted wrong answers should be expanded. This value is intentionally not retained upon
   * configuration changes since the user can just re-expand the list.
   */
  private var hasPreviousResponsesExpanded: Boolean = false

  private val backgroundCoroutineScope = CoroutineScope(backgroundCoroutineDispatcher)

  /**
   * An ever-present [PreviousNavigationButtonListener] that can exist even if backward navigation is disabled. This
   * listener no-ops if backward navigation is enabled. This serves to allows the host fragment to not need to implement
   * [PreviousNavigationButtonListener] if backward navigation is disabled.
   */
  private val previousNavigationButtonListener = object : PreviousNavigationButtonListener {
    override fun onPreviousButtonClicked() {
      if (playerFeatureSet.backwardNavigation) {
        (fragment as PreviousNavigationButtonListener).onPreviousButtonClicked()
      }
    }
  }

  /**
   * Computes a list of view models corresponding to the specified [EphemeralState] and the configuration of this
   * assembler, as well as the GCS entity ID that should be associated with rich-text rendering for this state.
   */
  fun compute(ephemeralState: EphemeralState, gcsEntityId: String): List<StateItemViewModel> {
    val hasPreviousState = ephemeralState.hasPreviousState

    previousAnswerViewModels.clear() // But retain whether the list is currently open.
    val pendingItemList = mutableListOf<StateItemViewModel>()
    if (playerFeatureSet.contentSupport) {
      addContentItem(pendingItemList, ephemeralState, gcsEntityId)
    }
    val interaction = ephemeralState.state.interaction
    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.pendingState.wrongAnswerList, gcsEntityId)
      if (playerFeatureSet.interactionSupport) {
        addInteractionForPendingState(pendingItemList, interaction, hasPreviousState, gcsEntityId)
      }
    } else if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      addPreviousAnswers(pendingItemList, ephemeralState.completedState.answerList, gcsEntityId)
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
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, hasPreviousButton: Boolean,
    gcsEntityId: String
  ) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory(
      gcsEntityId, interaction, fragment as InteractionAnswerReceiver, fragment as InteractionAnswerErrorReceiver, hasPreviousButton
    )
  }

  private fun addContentItem(
    pendingItemList: MutableList<StateItemViewModel>, ephemeralState: EphemeralState, gcsEntityId: String
  ) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    pendingItemList += ContentViewModel(contentSubtitledHtml.html, gcsEntityId)
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>, answersAndResponses: List<AnswerAndResponse>, gcsEntityId: String
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
      val showPreviousAnswers = !playerFeatureSet.wrongAnswerCollapsing || hasPreviousResponsesExpanded
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
    val previousAnswersAndFeedbacks = previousAnswerViewModels.takeLast(previousAnswerViewModels.size - 1)
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
   * Ensures that the previous responses, if any, are no longer expanded. This does not recompute the recycler view
   * adapter data--that requires another call to [compute]. If this is meant to have an immediate UI effect,
   * [togglePreviousAnswers] should be used, instead.
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
    val textView = checkNotNull(congratulationsTextView) { "Expected non-null reference to congratulations text view" }
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

    createLifecycleSafeTimer(2000).observe(fragment, Observer {
      textView.clearAnimation()
      textView.visibility = View.INVISIBLE
    })
  }

  private fun createSubmittedAnswer(userAnswer: UserAnswer, gcsEntityId: String): SubmittedAnswerViewModel {
    return SubmittedAnswerViewModel(userAnswer, gcsEntityId)
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
    stateIsTerminal: Boolean) {
    val hasPreviousButton = playerFeatureSet.backwardNavigation && hasPreviousState
    when {
      hasGeneralContinueButton && playerFeatureSet.forwardNavigation -> {
        pendingItemList += ContinueNavigationButtonViewModel(
          hasPreviousButton, previousNavigationButtonListener, fragment as ContinueNavigationButtonListener
        )
      }
      canContinueToNextState && playerFeatureSet.forwardNavigation -> {
        pendingItemList += NextButtonViewModel(
          hasPreviousButton, previousNavigationButtonListener, fragment as NextNavigationButtonListener
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
      doesMostRecentInteractionRequireExplicitSubmission(pendingItemList) && playerFeatureSet.forwardNavigation -> {
        pendingItemList += SubmitButtonViewModel(
          hasPreviousButton, previousNavigationButtonListener, fragment as SubmitNavigationButtonListener
        )
      }
      // Otherwise, just show the previous button since the interaction itself will push the answer submission.
      hasPreviousButton && !isMostRecentInteractionAutoNavigating(pendingItemList) -> {
        pendingItemList += PreviousButtonViewModel(
          previousNavigationButtonListener
        )
      }

      // Otherwise, there's no navigation button that should be shown since the current interaction handles this or
      // navigation in this context is disabled.
    }
  }

  /**
   * Returns a new [LiveData] that will be triggered exactly once after the specified timeout in milliseconds. This
   * should be used instead of Android's Handler class because this guarantees that observers will not be triggered if
   * its lifecycle is no longer valid, but will trigger upon the lifecycle becoming active again.
   */
  private fun createLifecycleSafeTimer(timeoutMs: Long): LiveData<Any?> {
    val liveData = MutableLiveData<Any?>()
    backgroundCoroutineScope.launch {
      delay(timeoutMs)
      liveData.postValue(null)
    }
    return liveData
  }

  /**
   * Returns whether there is currently a pending interaction that requires an additional user action to submit the
   * answer.
   */
  private fun doesMostRecentInteractionRequireExplicitSubmission(itemList: List<StateItemViewModel>): Boolean {
    return getPendingAnswerHandler(itemList)?.isExplicitAnswerSubmissionRequired() ?: true
  }

  /** Returns whether there is currently a pending interaction that also acts like a navigation button. */
  private fun isMostRecentInteractionAutoNavigating(itemList: List<StateItemViewModel>): Boolean {
    return getPendingAnswerHandler(itemList)?.isAutoNavigating() ?: false
  }

  /** Returns the latest [InteractionAnswerHandler] representing the current pending one, or null if there is none. */
  fun getPendingAnswerHandler(itemList: List<StateItemViewModel>): InteractionAnswerHandler? {
    // In the future, it may be ideal to make this more robust by actually tracking the handler corresponding to the
    // pending interaction.
    return itemList.findLast { it is InteractionAnswerHandler } as? InteractionAnswerHandler
  }

  /**
   * Builder to construct new [StatePlayerRecyclerViewAssembler]s in a way that allows granular control over the
   * features enabled by the assembler. Instances of this class should be created using its injectable [Factory].
   */
  class Builder private constructor(
    private val htmlParserFactory: HtmlParser.Factory, private val resourceBucketName: String,
    private val entityType: String, private val fragment: Fragment,
    private val interactionViewModelFactoryMap: Map<String, InteractionViewModelFactory>,
    private val backgroundCoroutineDispatcher: CoroutineDispatcher
  ) {
    private val adapterBuilder = BindableAdapter.MultiTypeBuilder.newBuilder(StateItemViewModel::viewType)
    /** Tracks features individually enabled for the assembler. No features are enabled by default. */
    private val featureSets = mutableSetOf(PlayerFeatureSet())
    private var congratulationsTextView: TextView? = null

    /** Adds support for displaying state content to the learner. */
    fun addContentSupport(): Builder {
      adapterBuilder.registerViewBinder(
        viewType = StateItemViewModel.ViewType.CONTENT,
        inflateView = { parent ->
          ContentItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<ContentItemBinding>(view)!!
          val contentViewModel = viewModel as ContentViewModel
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName, entityType, contentViewModel.gcsEntityId, /* imageCenterAlign= */ true
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
      adapterBuilder.registerViewBinder(
        viewType = StateItemViewModel.ViewType.FEEDBACK,
        inflateView = { parent ->
          FeedbackItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<FeedbackItemBinding>(view)!!
          val feedbackViewModel = viewModel as FeedbackViewModel
          binding.htmlContent =
            htmlParserFactory.create(
              resourceBucketName, entityType, feedbackViewModel.gcsEntityId, /* imageCenterAlign= */ true
            ).parseOppiaHtml(
              feedbackViewModel.htmlContent.toString(), binding.feedbackTextView
            )
        }
      )
      featureSets += PlayerFeatureSet(feedbackSupport = true)
      return this
    }

    /**
     * Adds support for rendering interactions and submitting answers to them. The 'continue' interaction is not
     * included since that's considered a navigation interaction.
     */
    fun addInteractionSupport(): Builder {
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
          val userAnswer = submittedAnswerViewModel.submittedUserAnswer
          when (userAnswer.textualAnswerCase) {
            UserAnswer.TextualAnswerCase.HTML_ANSWER -> {
              val htmlParser = htmlParserFactory.create(
                resourceBucketName, entityType, submittedAnswerViewModel.gcsEntityId, imageCenterAlign = false
              )
              binding.submittedAnswer = htmlParser.parseOppiaHtml(
                userAnswer.htmlAnswer, binding.submittedAnswerTextView
              )
            }
            else -> binding.submittedAnswer = userAnswer.plainAnswer
          }
        }
      )
      featureSets += PlayerFeatureSet(pastAnswerSupport = true)
      return this
    }

    /**
     * Adds support for automatically collapsing past wrong answers. This feature is not enabled without
     * [addPastAnswersSupport] also being enabled.
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

    /** Adds support for navigating to next states. Note that this also enables the 'Continue' interaction. */
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

    /** Adds support for displaying a button that allows the learner to replay the lesson experience. */
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

    /** Adds support for displaying a 'return to topic' button at the end of the lesson experience. */
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

    /** Adds support for displaying a congratulations answer when the learner submits a correct answer. */
    fun addCongratulationsForCorrectAnswers(congratulationsTextView: TextView): Builder {
      this.congratulationsTextView = congratulationsTextView
      featureSets += PlayerFeatureSet(showCongratulationsOnCorrectAnswer = true)
      return this
    }

    /** Returns a new [StatePlayerRecyclerViewAssembler] based on the builder-specified configuration. */
    fun build(): StatePlayerRecyclerViewAssembler {
      val playerFeatureSet = featureSets.reduce(PlayerFeatureSet::union)
      return StatePlayerRecyclerViewAssembler(
        adapterBuilder.build(), playerFeatureSet, fragment, congratulationsTextView, interactionViewModelFactoryMap,
        backgroundCoroutineDispatcher
      )
    }

    /** Fragment injectable factory to create new [Builder]s. */
    class Factory @Inject constructor(
      private val htmlParserFactory: HtmlParser.Factory, private val fragment: Fragment,
      private val interactionViewModelFactoryMap: Map<String, @JvmSuppressWildcards InteractionViewModelFactory>,
      @BackgroundDispatcher private val backgroundCoroutineDispatcher: CoroutineDispatcher
    ) {
      /** Returns a new [Builder] for the specified GCS resource bucket information for loading assets. */
      fun create(resourceBucketName: String, entityType: String): Builder {
        return Builder(
          htmlParserFactory, resourceBucketName, entityType, fragment, interactionViewModelFactoryMap,
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
    val showCongratulationsOnCorrectAnswer: Boolean = false
  ) {
    /** Returns a union of this feature set with other one. Loosely based on https://stackoverflow.com/a/49605849. */
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
        showCongratulationsOnCorrectAnswer = showCongratulationsOnCorrectAnswer
            || other.showCongratulationsOnCorrectAnswer
      )
    }
  }
}
