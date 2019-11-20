package org.oppia.app.player.state

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.recyclerview.widget.RecyclerView
import org.oppia.app.R
import org.oppia.app.databinding.ContentItemBinding
import org.oppia.app.databinding.ContinueInteractionItemBinding
import org.oppia.app.databinding.FeedbackItemBinding
import org.oppia.app.databinding.FractionInteractionItemBinding
import org.oppia.app.databinding.NumericInputInteractionItemBinding
import org.oppia.app.databinding.SelectionInteractionItemBinding
import org.oppia.app.databinding.StateButtonItemBinding
import org.oppia.app.databinding.StateFragmentBinding
import org.oppia.app.databinding.TextInputInteractionItemBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AnswerAndResponse
import org.oppia.app.model.AnswerOutcome
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.EphemeralState
import org.oppia.app.model.Interaction
import org.oppia.app.model.InteractionObject
import org.oppia.app.model.State
import org.oppia.app.model.SubtitledHtml
import org.oppia.app.player.audio.AudioButtonListener
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.AudioFragmentInterface
import org.oppia.app.player.audio.AudioViewModel
import org.oppia.app.player.audio.CellularAudioDialogFragment
import org.oppia.app.player.state.answerhandling.InteractionAnswerReceiver
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.ContinueInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.FractionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.InteractionViewModelFactory
import org.oppia.app.player.state.itemviewmodel.NumericInputViewModel
import org.oppia.app.player.state.itemviewmodel.SelectionInteractionViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel
import org.oppia.app.player.state.itemviewmodel.StateNavigationButtonViewModel.ContinuationNavigationButtonType
import org.oppia.app.player.state.itemviewmodel.TextInputViewModel
import org.oppia.app.player.state.listener.StateNavigationButtonListener
import org.oppia.app.recyclerview.BindableAdapter
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularAudioDialogController
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import org.oppia.util.networking.NetworkConnectionUtil
import org.oppia.util.networking.NetworkConnectionUtil.ConnectionStatus
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import org.oppia.util.parser.HtmlParser
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
  private val cellularAudioDialogController: CellularAudioDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationDataController: ExplorationDataController,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger,
  private val htmlParserFactory: HtmlParser.Factory,
  private val context: Context,
  private val interactionViewModelFactoryMap: Map<String, @JvmSuppressWildcards InteractionViewModelFactory>,
  private val networkConnectionUtil: NetworkConnectionUtil
) : StateNavigationButtonListener {

  private var showCellularDataDialog = true
  private var useCellularData = false
  private var feedbackId: String? = null
  private var autoPlayAudio = false
  private var isFeedbackPlaying = false
  private lateinit var explorationId: String
  private lateinit var currentStateName: String
  private lateinit var binding: StateFragmentBinding
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private val viewModel: StateViewModel by lazy {
    getStateViewModel()
  }
  private val ephemeralStateLiveData: LiveData<AsyncResult<EphemeralState>> by lazy {
    explorationProgressController.getCurrentState()
  }

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?): View? {
    cellularAudioDialogController.getCellularDataPreference()
      .observe(fragment, Observer<AsyncResult<CellularDataPreference>> {
        if (it.isSuccess()) {
          val prefs = it.getOrDefault(CellularDataPreference.getDefaultInstance())
          showCellularDataDialog = !(prefs.hideDialog)
          useCellularData = prefs.useCellularData
        }
      })
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

    // Initialize Audio Player
    fragment.childFragmentManager.beginTransaction()
      .add(R.id.audio_fragment_placeholder, AudioFragment(), TAG_AUDIO_FRAGMENT).commitNow()

    subscribeToCurrentState()

    return binding.root
  }

  private fun createRecyclerViewAdapter(): BindableAdapter<StateItemViewModel> {
    return BindableAdapter.Builder
      .newBuilder<StateItemViewModel>()
      .registerViewTypeComputer { viewModel ->
        when (viewModel) {
          is StateNavigationButtonViewModel -> ViewType.VIEW_TYPE_STATE_NAVIGATION_BUTTON.ordinal
          is ContentViewModel -> ViewType.VIEW_TYPE_CONTENT.ordinal
          is FeedbackViewModel -> ViewType.VIEW_TYPE_FEEDBACK.ordinal
          is ContinueInteractionViewModel -> ViewType.VIEW_TYPE_CONTINUE_INTERACTION.ordinal
          is SelectionInteractionViewModel -> ViewType.VIEW_TYPE_SELECTION_INTERACTION.ordinal
          is FractionInteractionViewModel -> ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION.ordinal
          is NumericInputViewModel -> ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION.ordinal
          is TextInputViewModel -> ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal
          else -> throw IllegalArgumentException("Encountered unexpected view model: $viewModel")
        }
      }
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_STATE_NAVIGATION_BUTTON.ordinal,
        inflateDataBinding = StateButtonItemBinding::inflate,
        setViewModel = StateButtonItemBinding::setButtonViewModel,
        transformViewModel = { it as StateNavigationButtonViewModel }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_CONTENT.ordinal,
        inflateView = { parent ->
          ContentItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<ContentItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
            (viewModel as ContentViewModel).htmlContent.toString(), binding.contentTextView
          )
        }
      )
      .registerViewBinder(
        viewType = ViewType.VIEW_TYPE_FEEDBACK.ordinal,
        inflateView = { parent ->
          FeedbackItemBinding.inflate(LayoutInflater.from(parent.context), parent, /* attachToParent= */ false).root
        },
        bindView = { view, viewModel ->
          val binding = DataBindingUtil.findBinding<FeedbackItemBinding>(view)!!
          binding.htmlContent = htmlParserFactory.create(entityType, explorationId).parseOppiaHtml(
            (viewModel as FeedbackViewModel).htmlContent.toString(), binding.feedbackTextView
          )
        }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_CONTINUE_INTERACTION.ordinal,
        inflateDataBinding = ContinueInteractionItemBinding::inflate,
        setViewModel = ContinueInteractionItemBinding::setViewModel,
        transformViewModel = { it as ContinueInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_SELECTION_INTERACTION.ordinal,
        inflateDataBinding = SelectionInteractionItemBinding::inflate,
        setViewModel = SelectionInteractionItemBinding::setViewModel,
        transformViewModel = { it as SelectionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_FRACTION_INPUT_INTERACTION.ordinal,
        inflateDataBinding = FractionInteractionItemBinding::inflate,
        setViewModel = FractionInteractionItemBinding::setViewModel,
        transformViewModel = { it as FractionInteractionViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_NUMERIC_INPUT_INTERACTION.ordinal,
        inflateDataBinding = NumericInputInteractionItemBinding::inflate,
        setViewModel = NumericInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as NumericInputViewModel }
      )
      .registerViewDataBinder(
        viewType = ViewType.VIEW_TYPE_TEXT_INPUT_INTERACTION.ordinal,
        inflateDataBinding = TextInputInteractionItemBinding::inflate,
        setViewModel = TextInputInteractionItemBinding::setViewModel,
        transformViewModel = { it as TextInputViewModel }
      )
      .build()
  }

  fun handleAudioClick() {
    if (isAudioShowing()) {
      setAudioFragmentVisible(false)
    } else {
      when (networkConnectionUtil.getCurrentConnectionStatus()) {
        ConnectionStatus.LOCAL -> setAudioFragmentVisible(true)
        ConnectionStatus.CELLULAR -> {
          if (showCellularDataDialog) {
            setAudioFragmentVisible(false)
            showCellularDataDialogFragment()
          } else {
            if (useCellularData) {
              setAudioFragmentVisible(true)
            } else {
              setAudioFragmentVisible(false)
            }
          }
        }
        ConnectionStatus.NONE-> {
          showOfflineDialog()
          setAudioFragmentVisible(false)
        }
      }
    }
  }

  fun handleEnableAudio(saveUserChoice: Boolean) {
    setAudioFragmentVisible(true)
    if (saveUserChoice) {
      cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    }
  }

  fun handleDisableAudio(saveUserChoice: Boolean) {
    setAudioFragmentVisible(false)
    if (saveUserChoice) {
      cellularAudioDialogController.setNeverUseCellularDataPreference()
    }
  }

  fun handleAnswerReadyForSubmission(answer: InteractionObject) {
    // An interaction has indicated that an answer is ready for submission.
    handleSubmitAnswer(answer)
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularAudioDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(TAG_AUDIO_FRAGMENT)
  }

  private fun setAudioFragmentVisible(isVisible: Boolean) {
    if (isVisible) {
      getStateViewModel().setAudioBarVisibility(true)
      autoPlayAudio = true
      (fragment.requireActivity() as AudioButtonListener).showAudioStreamingOn()
      val currentYOffset = binding.stateRecyclerView.computeVerticalScrollOffset()
      if (currentYOffset == 0) {
        binding.stateRecyclerView.smoothScrollToPosition(0)
      }
      getAudioFragment()?.let {
        (it as AudioFragmentInterface).setVoiceoverMappings(explorationId, currentStateName, feedbackId)
        it.view?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down_audio))
      }
      subscribeToAudioPlayStatus()
    } else {
      (fragment.requireActivity() as AudioButtonListener).showAudioStreamingOff()
      if (isAudioShowing()) {
        getAudioFragment()?.let {
          (it as AudioFragmentInterface).pauseAudio()
          val animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_audio)
          animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(p0: Animation?) {
              getStateViewModel().setAudioBarVisibility(false)
            }
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
          })
          it.view?.startAnimation(animation)
        }
      }
    }
  }

  private fun subscribeToAudioPlayStatus() {
    val audioFragmentInterface = getAudioFragment() as AudioFragmentInterface
    audioFragmentInterface.getCurrentPlayStatus().observe(fragment, Observer {
      if (it == AudioViewModel.UiAudioPlayStatus.COMPLETED) {
        getAudioFragment()?.let {
          if (isFeedbackPlaying) {
            audioFragmentInterface.setVoiceoverMappings(explorationId, currentStateName)
          }
          isFeedbackPlaying = false
          feedbackId = null
        }
      } else if (it == AudioViewModel.UiAudioPlayStatus.PREPARED) {
        if (autoPlayAudio) {
          autoPlayAudio = false
          audioFragmentInterface.playAudio()
        }
      }
    })
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
    currentStateName = ephemeralState.state.name

    showOrHideAudioByState(ephemeralState.state)
    if (isAudioShowing()) {
      (getAudioFragment() as AudioFragmentInterface).setVoiceoverMappings(explorationId, currentStateName, feedbackId)
    }
    feedbackId = null

    val pendingItemList = mutableListOf<StateItemViewModel>()
    addContentItem(pendingItemList, ephemeralState)
    val interaction = ephemeralState.state.interaction
    if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.PENDING_STATE) {
      addPreviousAnswers(pendingItemList, interaction, ephemeralState.pendingState.wrongAnswerList)
      addInteractionForPendingState(pendingItemList, interaction)
    } else if (ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.COMPLETED_STATE) {
      addPreviousAnswers(pendingItemList, interaction, ephemeralState.completedState.answerList)
    }

    val hasPreviousState = ephemeralState.hasPreviousState
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

    updateNavigationButtonVisibility(
      pendingItemList,
      hasPreviousState,
      canContinueToNextState,
      hasGeneralContinueButton,
      ephemeralState.stateTypeCase == EphemeralState.StateTypeCase.TERMINAL_STATE
    )

    viewModel.itemList.clear()
    viewModel.itemList += pendingItemList
  }

  /**clickNegative_checkNo
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
      } else {
        isFeedbackPlaying = true
        feedbackId = result.feedback.contentId
      }
      if (isAudioShowing()) {
        autoPlayAudio = true
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

  private fun showOrHideAudioByState(state: State) {
    if (state.recordedVoiceoversCount == 0) {
      (fragment.requireActivity() as AudioButtonListener).hideAudioButton()
    } else {
      (fragment.requireActivity() as AudioButtonListener).showAudioButton()
    }
  }

  override fun onReturnToTopicButtonClicked() {
    hideKeyboard()
    explorationDataController.stopPlayingExploration()
    activity.finish()
  }

  override fun onSubmitButtonClicked() {
    hideKeyboard()
    handleSubmitAnswer(viewModel.getPendingAnswer())
  }

  override fun onContinueButtonClicked() {
    hideKeyboard()
    moveToNextState()
    if (isAudioShowing()) {
      autoPlayAudio = true
    }
  }

  private fun handleSubmitAnswer(answer: InteractionObject) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer))
  }

  override fun onPreviousButtonClicked() {
    explorationProgressController.moveToPreviousState()
  }

  override fun onNextButtonClicked() = moveToNextState()

  private fun moveToNextState() {
    explorationProgressController.moveToNextState()
  }

  private fun addInteractionForPendingState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction
  ) = addInteraction(pendingItemList, interaction)

  private fun addInteractionForCompletedState(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, existingAnswer: InteractionObject
  ) = addInteraction(pendingItemList, interaction, existingAnswer = existingAnswer, isReadOnly = true)

  private fun addInteraction(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction, existingAnswer:
    InteractionObject? = null, isReadOnly: Boolean = false
  ) {
    val interactionViewModelFactory = interactionViewModelFactoryMap.getValue(interaction.id)
    pendingItemList += interactionViewModelFactory(
      explorationId, interaction, fragment as InteractionAnswerReceiver, existingAnswer, isReadOnly
    )
  }

  private fun addContentItem(pendingItemList: MutableList<StateItemViewModel>, ephemeralState: EphemeralState) {
    val contentSubtitledHtml: SubtitledHtml = ephemeralState.state.content
    pendingItemList += ContentViewModel(contentSubtitledHtml.html)
  }

  private fun addPreviousAnswers(
    pendingItemList: MutableList<StateItemViewModel>, interaction: Interaction,
    answersAndResponses: List<AnswerAndResponse>
  ) {
    // TODO: add support for displaying the previous answer, too.
    for (answerAndResponse in answersAndResponses) {
      addInteractionForCompletedState(pendingItemList, interaction, answerAndResponse.userAnswer)
      addFeedbackItem(pendingItemList, answerAndResponse.feedback)
    }
  }

  private fun addFeedbackItem(pendingItemList: MutableList<StateItemViewModel>, feedback: SubtitledHtml) {
    // Only show feedback if there's some to show.
    if (feedback.html.isNotEmpty()) {
      pendingItemList += FeedbackViewModel(feedback.html)
    }
  }

  private fun updateNavigationButtonVisibility(
    pendingItemList: MutableList<StateItemViewModel>,
    hasPreviousState: Boolean,
    canContinueToNextState: Boolean,
    hasGeneralContinueButton: Boolean,
    stateIsTerminal: Boolean
  ) {
    val stateNavigationButtonViewModel = StateNavigationButtonViewModel(context, this as StateNavigationButtonListener)
    stateNavigationButtonViewModel.updatePreviousButton(isEnabled = hasPreviousState)

    // Set continuation button.
    when {
      hasGeneralContinueButton -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON, isEnabled = true
        )
      }
      canContinueToNextState -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.NEXT_BUTTON, isEnabled = canContinueToNextState
        )
      }
      stateIsTerminal -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.RETURN_TO_TOPIC_BUTTON, isEnabled = true
        )
      }
      viewModel.doesMostRecentInteractionRequireExplicitSubmission(pendingItemList) -> {
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.SUBMIT_BUTTON, isEnabled = true
        )
      }
      else -> {
        // No continuation button needs to be set since the interaction itself will push for answer submission.
        stateNavigationButtonViewModel.updateContinuationButton(
          ContinuationNavigationButtonType.CONTINUE_BUTTON, isEnabled = false
        )
      }
    }
    pendingItemList += stateNavigationButtonViewModel
  }

  private fun hideKeyboard() {
    val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(fragment.view!!.windowToken, InputMethodManager.SHOW_FORCED)
  }

  private fun showOfflineDialog() {
    AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(context.getString(R.string.audio_dialog_offline_title))
      .setMessage(context.getString(R.string.audio_dialog_offline_message))
      .setPositiveButton(context.getString(R.string.audio_dialog_offline_positive)) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun isAudioShowing(): Boolean = viewModel.isAudioBarVisible.get()!!

  private enum class ViewType {
    VIEW_TYPE_CONTENT,
    VIEW_TYPE_FEEDBACK,
    VIEW_TYPE_STATE_NAVIGATION_BUTTON,
    VIEW_TYPE_CONTINUE_INTERACTION,
    VIEW_TYPE_SELECTION_INTERACTION,
    VIEW_TYPE_FRACTION_INPUT_INTERACTION,
    VIEW_TYPE_NUMERIC_INPUT_INTERACTION,
    VIEW_TYPE_TEXT_INPUT_INTERACTION
  }
}
