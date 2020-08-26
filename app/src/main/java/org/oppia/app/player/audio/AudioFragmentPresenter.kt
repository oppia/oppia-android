package org.oppia.app.player.audio

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.R
import org.oppia.app.databinding.AudioFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.AudioLanguage
import org.oppia.app.model.CellularDataPreference
import org.oppia.app.model.Profile
import org.oppia.app.model.ProfileId
import org.oppia.app.model.State
import org.oppia.app.player.audio.AudioViewModel.UiAudioPlayStatus
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.audio.CellularAudioDialogController
import org.oppia.domain.profile.ProfileManagementController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.ConsoleLogger
import org.oppia.util.networking.NetworkConnectionUtil
import javax.inject.Inject

const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"
private const val TAG_CELLULAR_DATA_DIALOG = "CELLULAR_DATA_DIALOG"
const val AUDIO_FRAGMENT_PROFILE_ID_ARGUMENT_KEY =
  "AudioFragmentPresenter.audio_fragment_profile_id"

/** The presenter for [AudioFragment]. */
@FragmentScope
class AudioFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val activity: AppCompatActivity,
  private val context: Context,
  private val cellularAudioDialogController: CellularAudioDialogController,
  private val profileManagementController: ProfileManagementController,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val viewModelProvider: ViewModelProvider<AudioViewModel>,
  private val logger: ConsoleLogger
) {
  var userIsSeeking = false
  var userProgress = 0
  private lateinit var profileId: ProfileId
  private var feedbackId: String? = null
  private var showCellularDataDialog = true
  private var useCellularData = false
  private var prepared = false
  private val viewModel by lazy {
    getAudioViewModel()
  }

  /** Sets up SeekBar listener, ViewModel, and gets VoiceoverMappings or restores saved state */
  fun handleCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    internalProfileId: Int
  ): View? {
    profileId = ProfileId.newBuilder().setInternalId(internalProfileId).build()
    cellularAudioDialogController.getCellularDataPreference()
      .observe(
        fragment,
        Observer<AsyncResult<CellularDataPreference>> {
          if (it.isSuccess()) {
            val prefs = it.getOrDefault(CellularDataPreference.getDefaultInstance())
            showCellularDataDialog = !(prefs.hideDialog)
            useCellularData = prefs.useCellularData
          }
        }
      )

    val binding = AudioFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.sbAudioProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) {
          userProgress = progress
        }
      }

      override fun onStartTrackingTouch(seekBar: SeekBar?) {
        userIsSeeking = true
      }

      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        viewModel.handleSeekTo(userProgress)
        userIsSeeking = false
      }
    })
    viewModel.playStatusLiveData.observe(
      fragment,
      Observer {
        prepared = it != UiAudioPlayStatus.LOADING && it != UiAudioPlayStatus.FAILED
        binding.sbAudioProgress.isEnabled = prepared
      }
    )

    binding.let {
      it.viewModel = viewModel
      it.audioFragment = fragment as AudioFragment
      it.lifecycleOwner = fragment
    }
    subscribeToAudioLanguageLiveData()
    return binding.root
  }

  private fun getProfileData(): LiveData<String> {
    return Transformations.map(
      profileManagementController.getProfile(profileId),
      ::processGetProfileResult
    )
  }

  private fun subscribeToAudioLanguageLiveData() {
    getProfileData().observe(
      activity,
      Observer<String> { result ->
        viewModel.selectedLanguageCode = result
        viewModel.loadMainContentAudio(false)
      }
    )
  }

  /** Gets language code by [AudioLanguage]. */
  private fun getAudioLanguage(audioLanguage: AudioLanguage): String {
    return when (audioLanguage) {
      AudioLanguage.ENGLISH_AUDIO_LANGUAGE -> "en"
      AudioLanguage.HINDI_AUDIO_LANGUAGE -> "hi"
      AudioLanguage.FRENCH_AUDIO_LANGUAGE -> "fr"
      AudioLanguage.CHINESE_AUDIO_LANGUAGE -> "zh"
      else -> "en"
    }
  }

  private fun processGetProfileResult(profileResult: AsyncResult<Profile>): String {
    if (profileResult.isFailure()) {
      logger.e("AudioFragment", "Failed to retrieve profile", profileResult.getErrorOrNull()!!)
    }
    return getAudioLanguage(profileResult.getOrDefault(Profile.getDefaultInstance()).audioLanguage)
  }

  /** Sets selected language code in presenter and ViewModel */
  fun languageSelected(language: String) {
    if (viewModel.selectedLanguageCode != language) {
      viewModel.setAudioLanguageCode(language)
    }
  }

  /** Shows language dialog fragment with language list from exploration */
  fun showLanguageDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_LANGUAGE_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = LanguageDialogFragment.newInstance(
      ArrayList(viewModel.languages),
      viewModel.selectedLanguageCode
    )
    dialogFragment.showNow(fragment.childFragmentManager, TAG_LANGUAGE_DIALOG)
  }

  /** Pauses audio if in prepared state */
  fun handleOnStop() {
    if (!activity.isChangingConfigurations && prepared) {
      viewModel.pauseAudio()
    }
  }

  /** Releases audio player resources */
  fun handleOnDestroy() {
    if (!activity.isChangingConfigurations) {
      viewModel.handleRelease()
    }
  }

  fun setStateAndExplorationId(newState: State, explorationId: String) =
    viewModel.setStateAndExplorationId(newState, explorationId)

  fun loadMainContentAudio(allowAutoPlay: Boolean) = viewModel.loadMainContentAudio(allowAutoPlay)

  fun loadFeedbackAudio(contentId: String, allowAutoPlay: Boolean) =
    viewModel.loadFeedbackAudio(contentId, allowAutoPlay)

  fun pauseAudio() {
    if (prepared)
      viewModel.pauseAudio()
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

  fun handleAudioClick(shouldEnableAudioPlayback: Boolean, feedbackId: String?) {
    this.feedbackId = feedbackId
    if (shouldEnableAudioPlayback) {
      when (networkConnectionUtil.getCurrentConnectionStatus()) {
        NetworkConnectionUtil.ConnectionStatus.LOCAL -> setAudioFragmentVisible(true)
        NetworkConnectionUtil.ConnectionStatus.CELLULAR -> {
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
        NetworkConnectionUtil.ConnectionStatus.NONE -> {
          showOfflineDialog()
          setAudioFragmentVisible(false)
        }
      }
    } else {
      setAudioFragmentVisible(false)
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
      loadMainContentAudio(true)
    } else {
      loadFeedbackAudio(feedbackId!!, true)
    }
    fragment.view?.startAnimation(AnimationUtils.loadAnimation(context, R.anim.slide_down_audio))
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
    AlertDialog.Builder(activity, R.style.AlertDialogTheme)
      .setTitle(context.getString(R.string.audio_dialog_offline_title))
      .setMessage(context.getString(R.string.audio_dialog_offline_message))
      .setPositiveButton(context.getString(R.string.audio_dialog_offline_positive)) { dialog, _ ->
        dialog.dismiss()
      }.create().show()
  }

  private fun getAudioViewModel(): AudioViewModel {
    return viewModelProvider.getForFragment(fragment, AudioViewModel::class.java)
  }
}
