package org.oppia.app.player.state

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
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
import org.oppia.app.model.State
import org.oppia.app.model.UserAnswer
import org.oppia.app.player.audio.AudioButtonListener
import org.oppia.app.player.audio.AudioFragment
import org.oppia.app.player.audio.AudioFragmentInterface
import org.oppia.app.player.audio.AudioViewModel
import org.oppia.app.player.audio.CellularAudioDialogFragment
import org.oppia.app.player.state.itemviewmodel.ContentViewModel
import org.oppia.app.player.state.itemviewmodel.FeedbackViewModel
import org.oppia.app.player.state.itemviewmodel.StateItemViewModel
import org.oppia.app.player.state.listener.AudioContentIdListener
import org.oppia.app.player.stopplaying.StopStatePlayingSessionListener
import org.oppia.app.topic.conceptcard.ConceptCardFragment
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularAudioDialogController
import org.oppia.domain.exploration.ExplorationProgressController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import org.oppia.util.logging.Logger
import org.oppia.util.networking.NetworkConnectionUtil
import org.oppia.util.networking.NetworkConnectionUtil.ConnectionStatus
import org.oppia.util.parser.ExplorationHtmlParserEntityType
import javax.inject.Inject

const val STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY = "STATE_FRAGMENT_EXPLORATION_ID_ARGUMENT_KEY"
private const val CELLULAR_DATA_DIALOG_FRAGMENT_TAG = "CELLULAR_DATA_DIALOG_FRAGMENT"
private const val AUDIO_FRAGMENT_TAG = "AUDIO_FRAGMENT"
private const val CONCEPT_CARD_DIALOG_FRAGMENT_TAG = "CONCEPT_CARD_FRAGMENT"

/** The presenter for [StateFragment]. */
@FragmentScope
class StateFragmentPresenter @Inject constructor(
  @ExplorationHtmlParserEntityType private val htmlParserEntityType: String,
  private val activity: AppCompatActivity,
  private val fragment: Fragment,
  private val cellularAudioDialogController: CellularAudioDialogController,
  private val viewModelProvider: ViewModelProvider<StateViewModel>,
  private val explorationProgressController: ExplorationProgressController,
  private val logger: Logger,
  private val context: Context,
  @DefaultResourceBucketName private val resourceBucketName: String,
  private val assemblerBuilderFactory: StatePlayerRecyclerViewAssembler.Builder.Factory,
  private val networkConnectionUtil: NetworkConnectionUtil
) {
  private var showCellularDataDialog = true
  private var useCellularData = false
  private var feedbackId: String? = null
  private var autoPlayAudio = false
  private var isFeedbackPlaying = false
  private lateinit var explorationId: String
  private lateinit var currentStateName: String
  private lateinit var binding: StateFragmentBinding
  private lateinit var currentHighlightedContentItem: StateItemViewModel
  private lateinit var recyclerViewAdapter: RecyclerView.Adapter<*>
  private val viewModel: StateViewModel by lazy {
    getStateViewModel()
  }
  private lateinit var recyclerViewAssembler: StatePlayerRecyclerViewAssembler
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
    recyclerViewAssembler = createRecyclerViewAssembler(
      assemblerBuilderFactory.create(resourceBucketName, htmlParserEntityType),
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

    // Initialize Audio Player
    fragment.childFragmentManager.beginTransaction()
      .add(R.id.audio_fragment_placeholder, AudioFragment(), AUDIO_FRAGMENT_TAG).commitNow()

    binding.stateRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
      if (bottom < oldBottom) {
        binding.stateRecyclerView.postDelayed({
          binding.stateRecyclerView.scrollToPosition(stateRecyclerViewAdapter.itemCount - 1)
        }, 100)
      }
    }

    subscribeToCurrentState()

    return binding.root
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
    recyclerViewAssembler.adapter.notifyDataSetChanged()
  }

  fun onConceptCardLinkClicked(view: View, skillId: String) {
    ConceptCardFragment.newInstance(skillId).showNow(fragment.childFragmentManager, CONCEPT_CARD_DIALOG_FRAGMENT_TAG)
  }

  fun handleContentCardHighlighting(contentId: String, playing: Boolean) {
    if (::currentHighlightedContentItem.isInitialized) {
      if (currentHighlightedContentItem is ContentViewModel && (currentHighlightedContentItem as ContentViewModel).contentId != contentId) {
        (currentHighlightedContentItem as ContentViewModel).updateIsAudioPlaying(false)
      } else if (currentHighlightedContentItem is FeedbackViewModel && (currentHighlightedContentItem as FeedbackViewModel).contentId != contentId) {
        (currentHighlightedContentItem as FeedbackViewModel).updateIsAudioPlaying(false)
      }
    }
    val itemList = viewModel.itemList
    for (item in itemList) {
      if (item is ContentViewModel) {
        if (item.contentId == contentId) {
          currentHighlightedContentItem = item
        }
      } else if (item is FeedbackViewModel) {
        if (item.contentId == contentId) {
          currentHighlightedContentItem = item
        }
      }
    }
    if (::currentHighlightedContentItem.isInitialized && currentHighlightedContentItem is ContentViewModel) {
      (currentHighlightedContentItem as ContentViewModel).updateIsAudioPlaying(playing)
    }
    if (::currentHighlightedContentItem.isInitialized && currentHighlightedContentItem is FeedbackViewModel) {
      (currentHighlightedContentItem as FeedbackViewModel).updateIsAudioPlaying(playing)
    }
  }

  fun dismissConceptCard() {
    fragment.childFragmentManager.findFragmentByTag(CONCEPT_CARD_DIALOG_FRAGMENT_TAG)?.let { dialogFragment ->
      fragment.childFragmentManager.beginTransaction().remove(dialogFragment).commitNow()
    }
  }

  fun handleAudioClick() {
    if (isAudioShowing()) {
      setAudioFragmentVisible(false)
    } else {
      if (explorationId == "umPkwp0L1M0-") {
        setAudioFragmentVisible(true)
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
          ConnectionStatus.NONE -> {
            showOfflineDialog()
            setAudioFragmentVisible(false)
          }
        }
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
      .build()
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(CELLULAR_DATA_DIALOG_FRAGMENT_TAG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularAudioDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, CELLULAR_DATA_DIALOG_FRAGMENT_TAG)
  }

  private fun getStateViewModel(): StateViewModel {
    return viewModelProvider.getForFragment(fragment, StateViewModel::class.java)
  }

  private fun getAudioFragment(): Fragment? {
    return fragment.childFragmentManager.findFragmentByTag(AUDIO_FRAGMENT_TAG)
  }

  private fun setAudioFragmentVisible(isVisible: Boolean) {
    if (isVisible) {
      showAudioFragment()
    } else {
      hideAudioFragment()
    }
  }

  private fun showAudioFragment() {
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
      (it as AudioFragment).setContentIdListener(fragment as AudioContentIdListener)
    }
    subscribeToAudioPlayStatus()
  }

  private fun hideAudioFragment() {
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
        if(isAudioShowing() && !isFeedbackPlaying){
          autoPlayAudio = true
        }
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
    val scrollToTop = ::currentStateName.isInitialized && currentStateName != ephemeralState.state.name
    currentStateName = ephemeralState.state.name

    isFeedbackPlaying = false

    showOrHideAudioByState(ephemeralState.state)
    if (isAudioShowing()) {
      (getAudioFragment() as AudioFragmentInterface).setVoiceoverMappings(explorationId, currentStateName, feedbackId)
    }
    feedbackId = null

    viewModel.itemList.clear()
    viewModel.itemList += recyclerViewAssembler.compute(ephemeralState, explorationId)

    if (scrollToTop) {
      (binding.stateRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 200)
    }
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
        if (result.labelledAsCorrectAnswer) {
          recyclerViewAssembler.showCongratulationMessageOnCorrectAnswer()
        }
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

  private fun handleSubmitAnswer(answer: UserAnswer) {
    subscribeToAnswerOutcome(explorationProgressController.submitAnswer(answer))
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

  private fun showOfflineDialog() {
    AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(context.getString(R.string.audio_dialog_offline_title))
      .setMessage(context.getString(R.string.audio_dialog_offline_message))
      .setPositiveButton(context.getString(R.string.audio_dialog_offline_positive)) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun isAudioShowing(): Boolean = viewModel.isAudioBarVisible.get()!!
}
