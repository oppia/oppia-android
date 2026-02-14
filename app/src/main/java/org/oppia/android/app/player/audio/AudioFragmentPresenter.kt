package org.oppia.android.app.player.audio

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import org.oppia.android.app.databinding.databinding.AudioFragmentBinding
import org.oppia.android.app.fragment.FragmentScope
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.model.Spotlight
import org.oppia.android.app.model.State
import org.oppia.android.app.player.audio.AudioViewModel.UiAudioPlayStatus
import org.oppia.android.app.spotlight.SpotlightManager
import org.oppia.android.app.spotlight.SpotlightShape
import org.oppia.android.app.spotlight.SpotlightTarget
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.ui.R
import org.oppia.android.domain.audio.CellularAudioDialogController
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.data.DataProviders.Companion.toLiveData
import org.oppia.android.util.networking.NetworkConnectionUtil
import org.oppia.android.util.platformparameter.EnableSpotlightUi
import org.oppia.android.util.platformparameter.PlatformParameterValue
import javax.inject.Inject

const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"
private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"

/** The presenter for [AudioFragment]. */
@FragmentScope
class AudioFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val context: Context,
  private val cellularAudioDialogController: CellularAudioDialogController,
  private val profileManagementController: ProfileManagementController,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val audioViewModel: AudioViewModel,
  private val oppiaLogger: OppiaLogger,
  private val resourceHandler: AppLanguageResourceHandler,
  @EnableSpotlightUi private val enableSpotlightUi: PlatformParameterValue<Boolean>
) {
  var userIsSeeking = false
  var userProgress = 0
  private lateinit var profileId: ProfileId
  private var feedbackId: String? = null
  private var showCellularDataDialog = true
  private var useCellularData = false
  private var prepared = false

  private var isPauseAudioRequestPending = false
  private lateinit var binding: AudioFragmentBinding

  private sealed class PendingAudioAction {
    data class Load(
      val contentId: String?,
      val isFeedback: Boolean,
      val allowAutoPlay: Boolean,
      val reloadingContent: Boolean
    ) : PendingAudioAction()

    data class PlayPause(val isPlaying: Boolean) : PendingAudioAction()

    object ShowAudioFragment : PendingAudioAction()
  }

  private var pendingAudioAction: PendingAudioAction? = null
  private var allowCellularForNextLoad = false

  /** Sets up SeekBar listener, ViewModel, and gets VoiceoverMappings or restores saved state. */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    cellularAudioDialogController.getCellularDataPreference().toLiveData()
      .observe(
        fragment,
        {
          if (it is AsyncResult.Success) {
            showCellularDataDialog = !it.value.hideDialog
            useCellularData = it.value.useCellularData
          }
        }
      )

    binding = AudioFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.audioProgressSeekBar.setOnSeekBarChangeListener(
      object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
          if (fromUser) {
            userProgress = progress
          }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
          userIsSeeking = true
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
          audioViewModel.handleSeekTo(userProgress)
          userIsSeeking = false
        }
      })
    audioViewModel.playStatusLiveData.observe(
      fragment,
      {
        prepared = it != UiAudioPlayStatus.LOADING && it != UiAudioPlayStatus.FAILED
        binding.audioProgressSeekBar.isEnabled = prepared

        // This check will execute any pending pause request that causes issues with
        // audio not being paused as the user navigates through lessons in a topic.
        // Check #1801 for more details, and specifically
        // https://github.com/oppia/oppia-android/pull/4629#issuecomment-1410005186
        // for notes on why this fix works.
        if (prepared && isPauseAudioRequestPending) {
          pauseAudio()
        }
      }
    )

    binding.let {
      it.viewModel = audioViewModel
      it.audioFragment = fragment as AudioFragment
      it.lifecycleOwner = fragment
    }
    subscribeToAudioLanguageLiveData()
    return binding.root
  }

  private fun startSpotlights() {
    val audioLanguageIconSpotlightTarget = SpotlightTarget(
      binding.audioLanguageIcon,
      resourceHandler.getStringInLocale(R.string.voiceover_language_icon_spotlight_hint),
      SpotlightShape.Circle,
      Spotlight.FeatureCase.VOICEOVER_LANGUAGE_ICON
    )

    checkNotNull(getSpotlightManager()).requestSpotlightViewWithDelayedLayout(
      audioLanguageIconSpotlightTarget
    )
  }

  private fun getSpotlightManager(): SpotlightManager? {
    return fragment.requireActivity().supportFragmentManager.findFragmentByTag(
      SpotlightManager.SPOTLIGHT_FRAGMENT_TAG
    ) as? SpotlightManager
  }

  private fun retrieveAudioLanguageCode(): LiveData<String> {
    return Transformations.map(
      profileManagementController.getAudioLanguage(profileId).toLiveData(),
      ::processAudioLanguageResult
    )
  }

  private fun subscribeToAudioLanguageLiveData() {
    retrieveAudioLanguageCode().observe(
      activity,
      { result ->
        audioViewModel.selectedLanguageCode = result
        audioViewModel.loadMainContentAudio(allowAutoPlay = false, reloadingContent = false)
      }
    )
  }

  /** Gets language code by [AudioLanguage]. */
  private fun computeLanguageCode(audioLanguage: AudioLanguage): String {
    return when (audioLanguage) {
      AudioLanguage.HINDI_AUDIO_LANGUAGE -> "hi"
      AudioLanguage.BRAZILIAN_PORTUGUESE_LANGUAGE -> "pt"
      AudioLanguage.ARABIC_LANGUAGE -> "ar"
      AudioLanguage.NIGERIAN_PIDGIN_LANGUAGE -> "pcm"
      AudioLanguage.NO_AUDIO, AudioLanguage.UNRECOGNIZED, AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED,
      AudioLanguage.ENGLISH_AUDIO_LANGUAGE -> "en"
    }
  }

  private fun processAudioLanguageResult(languageResult: AsyncResult<AudioLanguage>): String {
    val audioLanguage = when (languageResult) {
      is AsyncResult.Failure -> {
        oppiaLogger.e("AudioFragment", "Failed to retrieve audio language", languageResult.error)
        AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED
      }
      is AsyncResult.Pending -> AudioLanguage.AUDIO_LANGUAGE_UNSPECIFIED
      is AsyncResult.Success -> languageResult.value
    }
    return computeLanguageCode(audioLanguage)
  }

  /** Sets selected language code in presenter and ViewModel. */
  fun languageSelected(language: String) {
    if (audioViewModel.selectedLanguageCode != language) {
      audioViewModel.setAudioLanguageCode(language)
    }
  }

  /** Shows language dialog fragment with language list from exploration. */
  fun showLanguageDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_LANGUAGE_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = LanguageDialogFragment.newInstance(
      ArrayList(audioViewModel.languages),
      audioViewModel.selectedLanguageCode
    )
    dialogFragment.showNow(fragment.childFragmentManager, TAG_LANGUAGE_DIALOG)
  }

  /** Pauses audio if in prepared state. */
  fun handleOnStop() {
    if (!activity.isChangingConfigurations && prepared) {
      audioViewModel.pauseAudio()
    }
  }

  /** Releases audio player resources. */
  fun handleOnDestroy() {
    if (!activity.isChangingConfigurations) {
      audioViewModel.handleRelease()
    }
  }

  fun setStateAndExplorationId(newState: State, explorationId: String) =
    audioViewModel.setStateAndExplorationId(newState, explorationId)

  fun loadMainContentAudio(allowAutoPlay: Boolean, reloadingContent: Boolean) =
    requestAudioLoad(
      contentId = null,
      isFeedback = false,
      allowAutoPlay = allowAutoPlay,
      reloadingContent = reloadingContent,
      shouldPromptForNetworkIssues = allowAutoPlay
    )

  fun loadFeedbackAudio(contentId: String, allowAutoPlay: Boolean) =
    requestAudioLoad(
      contentId = contentId,
      isFeedback = true,
      allowAutoPlay = allowAutoPlay,
      reloadingContent = false,
      shouldPromptForNetworkIssues = allowAutoPlay
    )

  fun pauseAudio() {
    isPauseAudioRequestPending = true
    if (prepared && isPauseAudioRequestPending) {
      audioViewModel.pauseAudio()
      isPauseAudioRequestPending = false
    }
  }

  fun handleEnableAudio(saveUserChoice: Boolean) {
    val action = pendingAudioAction
    pendingAudioAction = null
    if (saveUserChoice) {
      cellularAudioDialogController.setAlwaysUseCellularDataPreference()
    }

    if (action != null) {
      performPendingAction(action, assumeCellularAllowed = true)
    } else {
      setAudioFragmentVisible(true)
    }
  }

  fun handleDisableAudio(saveUserChoice: Boolean) {
    if (saveUserChoice) {
      cellularAudioDialogController.setNeverUseCellularDataPreference()
    }

    val action = pendingAudioAction
    pendingAudioAction = null
    if (action is PendingAudioAction.ShowAudioFragment || action == null) {
      setAudioFragmentVisible(false)
    }
    if (action != null) {
      showCellularDataDisallowedDialog()
    }
  }

  fun handleAudioClick(shouldEnableAudioPlayback: Boolean, feedbackId: String?) {
    this.feedbackId = feedbackId
    if (shouldEnableAudioPlayback) {
      when (networkConnectionUtil.getCurrentConnectionStatus()) {
        NetworkConnectionUtil.ProdConnectionStatus.LOCAL -> setAudioFragmentVisible(true)
        NetworkConnectionUtil.ProdConnectionStatus.CELLULAR -> {
          if (showCellularDataDialog) {
            pendingAudioAction = PendingAudioAction.ShowAudioFragment
            setAudioFragmentVisible(false)
            showCellularDataDialogFragment()
          } else {
            if (useCellularData) {
              setAudioFragmentVisible(true)
            } else {
              pendingAudioAction = PendingAudioAction.ShowAudioFragment
              showCellularDataDisallowedDialog()
              setAudioFragmentVisible(false)
            }
          }
        }
        NetworkConnectionUtil.ProdConnectionStatus.NONE -> {
          pendingAudioAction = PendingAudioAction.ShowAudioFragment
          showOfflineDialog()
          setAudioFragmentVisible(false)
        }
      }
    } else {
      setAudioFragmentVisible(false)
    }
  }

  fun handlePlayPauseClick(isPlaying: Boolean) {
    if (isPlaying) {
      audioViewModel.togglePlayPause(UiAudioPlayStatus.PLAYING)
      return
    }

    val pendingLoad = pendingAudioAction as? PendingAudioAction.Load
    if (pendingLoad != null) {
      requestAudioLoad(
        contentId = pendingLoad.contentId,
        isFeedback = pendingLoad.isFeedback,
        allowAutoPlay = true,
        reloadingContent = pendingLoad.reloadingContent,
        shouldPromptForNetworkIssues = true
      )
      return
    }

    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ProdConnectionStatus.LOCAL ->
        audioViewModel.togglePlayPause(null)
      NetworkConnectionUtil.ProdConnectionStatus.CELLULAR -> {
        if (showCellularDataDialog) {
          pendingAudioAction = PendingAudioAction.PlayPause(isPlaying)
          showCellularDataDialogFragment()
        } else {
          if (useCellularData) {
            audioViewModel.togglePlayPause(null)
          } else {
            pendingAudioAction = PendingAudioAction.PlayPause(isPlaying)
            showCellularDataDisallowedDialog()
          }
        }
      }
      NetworkConnectionUtil.ProdConnectionStatus.NONE -> showOfflineDialog()
    }
  }

  private fun setAudioFragmentVisible(isVisible: Boolean) {
    if (isVisible) {
      showAudioFragment()
    } else {
      hideAudioFragment()
    }
  }

  private fun showAudioFragment() {
    val audioButtonListener = activity as AudioButtonListener
    audioButtonListener.setAudioBarVisibility(true)
    audioButtonListener.showAudioStreamingOn()
    audioButtonListener.scrollToTop()
    if (feedbackId == null) {
      // This isn't reloading content since it's the first case of the content auto-playing.
      requestAudioLoad(
        contentId = null,
        isFeedback = false,
        allowAutoPlay = !enableSpotlightUi.value,
        reloadingContent = false,
        shouldPromptForNetworkIssues = true
      )
    } else {
      requestAudioLoad(
        contentId = feedbackId,
        isFeedback = true,
        allowAutoPlay = !enableSpotlightUi.value,
        reloadingContent = false,
        shouldPromptForNetworkIssues = true
      )
    }
    fragment.view?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down_audio))
    startSpotlights()
  }

  private fun hideAudioFragment() {
    (activity as AudioButtonListener).showAudioStreamingOff()
    (fragment as AudioUiManager).pauseAudio()
    val animation = AnimationUtils.loadAnimation(context, R.anim.slide_up_audio)
    animation.setAnimationListener(object : Animation.AnimationListener {
      override fun onAnimationEnd(p0: Animation?) {
        (activity as AudioButtonListener).setAudioBarVisibility(false)
      }

      override fun onAnimationStart(p0: Animation?) {}
      override fun onAnimationRepeat(p0: Animation?) {}
    })
    fragment.view?.startAnimation(animation)
  }

  private fun showCellularDataDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_CELLULAR_DATA_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = CellularAudioDialogFragment.newInstance()
    dialogFragment.showNow(fragment.childFragmentManager, TAG_CELLULAR_DATA_DIALOG)
  }

  private fun showOfflineDialog() {
    AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setTitle(resourceHandler.getStringInLocale(R.string.audio_dialog_offline_title))
      .setMessage(resourceHandler.getStringInLocale(R.string.audio_dialog_offline_message))
      .setPositiveButton(
        resourceHandler.getStringInLocale(R.string.audio_dialog_offline_positive)
      ) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun showCellularDataDisallowedDialog() {
    AlertDialog.Builder(activity, R.style.OppiaAlertDialogTheme)
      .setTitle(resourceHandler.getStringInLocale(R.string.audio_dialog_cellular_disallowed_title))
      .setMessage(resourceHandler.getStringInLocale(R.string.audio_dialog_cellular_disallowed))
      .setPositiveButton(
        resourceHandler.getStringInLocale(R.string.audio_dialog_offline_positive)
      ) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun requestAudioLoad(
    contentId: String?,
    isFeedback: Boolean,
    allowAutoPlay: Boolean,
    reloadingContent: Boolean,
    shouldPromptForNetworkIssues: Boolean
  ) {
    if (allowCellularForNextLoad) {
      allowCellularForNextLoad = false
      performAudioLoad(contentId, isFeedback, allowAutoPlay, reloadingContent)
      return
    }

    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ProdConnectionStatus.LOCAL ->
        performAudioLoad(contentId, isFeedback, allowAutoPlay, reloadingContent)
      NetworkConnectionUtil.ProdConnectionStatus.CELLULAR ->
        handleCellularLoad(
          contentId,
          isFeedback,
          allowAutoPlay,
          reloadingContent,
          shouldPromptForNetworkIssues
        )
      NetworkConnectionUtil.ProdConnectionStatus.NONE -> {
        if (shouldPromptForNetworkIssues) {
          showOfflineDialog()
        }
        pendingAudioAction = PendingAudioAction.Load(
          contentId = contentId,
          isFeedback = isFeedback,
          allowAutoPlay = allowAutoPlay,
          reloadingContent = reloadingContent
        )
      }
    }
  }

  private fun handleCellularLoad(
    contentId: String?,
    isFeedback: Boolean,
    allowAutoPlay: Boolean,
    reloadingContent: Boolean,
    shouldPromptForNetworkIssues: Boolean
  ) {
    when {
      showCellularDataDialog && shouldPromptForNetworkIssues -> {
        pendingAudioAction = PendingAudioAction.Load(
          contentId = contentId,
          isFeedback = isFeedback,
          allowAutoPlay = allowAutoPlay,
          reloadingContent = reloadingContent
        )
        showCellularDataDialogFragment()
      }
      useCellularData -> performAudioLoad(contentId, isFeedback, allowAutoPlay, reloadingContent)
      shouldPromptForNetworkIssues -> {
        pendingAudioAction = PendingAudioAction.Load(
          contentId = contentId,
          isFeedback = isFeedback,
          allowAutoPlay = allowAutoPlay,
          reloadingContent = reloadingContent
        )
        showCellularDataDisallowedDialog()
      }
      else -> {
        pendingAudioAction = PendingAudioAction.Load(
          contentId = contentId,
          isFeedback = isFeedback,
          allowAutoPlay = allowAutoPlay,
          reloadingContent = reloadingContent
        )
      }
    }
  }

  private fun performAudioLoad(
    contentId: String?,
    isFeedback: Boolean,
    allowAutoPlay: Boolean,
    reloadingContent: Boolean
  ) {
    pendingAudioAction = null
    if (isFeedback) {
      audioViewModel.loadFeedbackAudio(checkNotNull(contentId), allowAutoPlay)
    } else {
      audioViewModel.loadMainContentAudio(allowAutoPlay, reloadingContent)
    }
  }

  private fun performPendingAction(action: PendingAudioAction, assumeCellularAllowed: Boolean) {
    when (action) {
      PendingAudioAction.ShowAudioFragment -> {
        if (assumeCellularAllowed) {
          allowCellularForNextLoad = true
        }
        setAudioFragmentVisible(true)
      }
      is PendingAudioAction.Load -> {
        if (assumeCellularAllowed) {
          performAudioLoad(
            action.contentId,
            action.isFeedback,
            action.allowAutoPlay,
            action.reloadingContent
          )
        } else {
          requestAudioLoad(
            action.contentId,
            action.isFeedback,
            action.allowAutoPlay,
            action.reloadingContent,
            shouldPromptForNetworkIssues = true
          )
        }
      }
      is PendingAudioAction.PlayPause -> {
        if (assumeCellularAllowed) {
          audioViewModel.togglePlayPause(null)
        } else {
          handlePlayPauseClick(action.isPlaying)
        }
      }
    }
  }
}
