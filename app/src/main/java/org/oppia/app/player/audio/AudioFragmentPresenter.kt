package org.oppia.app.player.audio

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import org.oppia.app.databinding.AudioFragmentBinding
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Exploration
import org.oppia.app.model.State
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.app.viewmodel.ViewModelProvider
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import org.oppia.util.logging.Logger
import javax.inject.Inject
import kotlin.collections.ArrayList

private const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"
private const val KEY_SELECTED_LANGUAGE = "SELECTED_LANGUAGE"

/** The presenter for [AudioFragment]. */
@FragmentScope
class AudioFragmentPresenter @Inject constructor(
  private val fragment: Fragment,
  private val viewModelProvider: ViewModelProvider<AudioViewModel>,
  private val logger: Logger,
  private val explorationDataController: ExplorationDataController
) {
  var userIsSeeking = false
  var userProgress = 0
  private var prepared = false

  private var selectedLanguageCode: String = ""
  private var languages = listOf<String>()

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?, explorationId: String, stateId: String): View? {
    val binding = AudioFragmentBinding.inflate(inflater, container, /* attachToRoot= */ false)
    binding.sbAudioProgress.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if (fromUser) userProgress = progress
      }
      override fun onStartTrackingTouch(seekBar: SeekBar?) {
        userIsSeeking = true
      }
      override fun onStopTrackingTouch(seekBar: SeekBar?) {
        getAudioViewModel().handleSeekTo(userProgress)
        userIsSeeking = false
      }
    })

    val viewModel = getAudioViewModel()
    viewModel.setExplorationId(explorationId)
    viewModel.playStatusLiveData.observe(fragment, Observer {
      prepared = it != AudioViewModel.AudioPlayStatus.LOADING
      binding.sbAudioProgress.isEnabled = prepared
    })

    binding.let {
      it.viewModel = viewModel
      it.audioFragment = fragment as AudioFragment
      it.lifecycleOwner = fragment
    }
    getVoiceoverMappings(explorationId, stateId, savedInstanceState?.getString(KEY_SELECTED_LANGUAGE))
    return binding.root
  }

  fun languageSelected(language: String) {
    selectedLanguageCode = language
    getAudioViewModel().setAudioLanguageCode(language)
  }

  fun showLanguageDialogFragment() {
    val previousFragment = fragment.childFragmentManager.findFragmentByTag(TAG_LANGUAGE_DIALOG)
    if (previousFragment != null) {
      fragment.childFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = LanguageDialogFragment.newInstance(
      languages as ArrayList<String>,
      selectedLanguageCode
    )
    dialogFragment.showNow(fragment.childFragmentManager, TAG_LANGUAGE_DIALOG)
  }

  fun handleSaveInstanceState(outstate: Bundle) =
    outstate.putString(KEY_SELECTED_LANGUAGE, selectedLanguageCode)

  fun handleOnDestroy() = getAudioViewModel().handleRelease()

  fun handleOnStop() {
    if (prepared)
      getAudioViewModel().handlePlayPause(AudioViewModel.AudioPlayStatus.PLAYING)
  }

  private fun getVoiceoverMappings(explorationId: String, stateId: String, selectedLang: String?) {
    val explorationResultLiveData = explorationDataController.getExplorationById(explorationId)
    processExplorationLiveData(explorationResultLiveData).observe(fragment, Observer {
      val state = it.statesMap[stateId] ?: State.getDefaultInstance()
      val contentId = state.content.contentId
      val voiceoverMapping = (state.recordedVoiceoversMap[contentId] ?: VoiceoverMapping.getDefaultInstance()).voiceoverMappingMap
      languages = voiceoverMapping.keys.toList()
      selectedLanguageCode = selectedLang ?: languages.first()
      if (selectedLang == null) {
        val viewModel = getAudioViewModel()
        viewModel.setVoiceoverMappings(voiceoverMapping)
        viewModel.setAudioLanguageCode(selectedLanguageCode)
      }
    })
  }

  private fun getAudioViewModel(): AudioViewModel {
    return viewModelProvider.getForFragment(fragment, AudioViewModel::class.java)
  }

  private fun processExplorationLiveData(explorationResultLiveData: LiveData<AsyncResult<Exploration>>): LiveData<Exploration> {
    return Transformations.map(explorationResultLiveData, ::processExplorationResult)
  }

  private fun processExplorationResult(explorationResult: AsyncResult<Exploration>): Exploration {
    if (explorationResult.isFailure()) {
      logger.e("AudioFragment", "Failed to retrieve Exploration: " + explorationResult.getErrorOrNull())
    }
    return explorationResult.getOrDefault(Exploration.getDefaultInstance())
  }
}

