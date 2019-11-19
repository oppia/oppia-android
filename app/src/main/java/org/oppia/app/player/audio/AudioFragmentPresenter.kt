package org.oppia.app.player.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.oppia.app.databinding.AudioFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.viewmodel.ViewModelProvider
import javax.inject.Inject

private const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"

/** The presenter for [AudioFragment]. */
@FragmentScope
class AudioFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<AudioViewModel>
) {
  var userIsSeeking = false
  var userProgress = 0

  private var prepared = false
  private val viewModel by lazy {
    getAudioViewModel()
  }

  /** Sets up SeekBar listener, ViewModel, and gets VoiceoverMappings or restores saved state */
  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, explorationId: String, stateId: String): View? {
    val binding = AudioFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.sbAudioProgress.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
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
    viewModel.playStatusLiveData.observe(fragment, Observer {
      prepared = it != AudioViewModel.UiAudioPlayStatus.LOADING
      binding.sbAudioProgress.isEnabled = prepared
    })

    binding.let {
      it.viewModel = viewModel
      it.audioFragment = fragment as AudioFragment
      it.lifecycleOwner = fragment
    }

    if (savedInstanceState == null) {
      retrieveVoiceoverMappings(explorationId, stateId)
    }
    return binding.root
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
    if (!fragment.requireActivity().isChangingConfigurations && prepared) {
      viewModel.pauseAudio()
    }
  }

  /** Releases audio player resources */
  fun handleOnDestroy() {
    if (!fragment.requireActivity().isChangingConfigurations) {
      viewModel.handleRelease()
    }
  }

  fun setVoiceoverMappingsByState(stateId: String, contentId: String? = null) = viewModel.setVoiceoverMappingsByState(stateId, contentId)

  fun getCurrentPlayStatus() = viewModel.playStatusLiveData

  /**
   * Retrieves VoiceoverMapping from the ExplorationDataController
   * Sets languages in Presenter and ViewModel, selected language code is defaulted to first
   */
  private fun retrieveVoiceoverMappings(explorationId: String, stateId: String) {
    viewModel.initializeVoiceOverMappings(explorationId, stateId)
  }

  private fun getAudioViewModel(): AudioViewModel {
    return viewModelProvider.getForFragment(fragment, AudioViewModel::class.java)
  }
}
