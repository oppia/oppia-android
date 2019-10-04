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
private const val KEY_IS_PLAYING = "IS_PLAYING"
private const val KEY_CURRENT_POSITION = "CURRENT_POSITION"
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
      binding.sbAudioProgress.isEnabled = it != AudioViewModel.AudioPlayStatus.LOADING
    })

    savedInstanceState?.let { bundle ->
      viewModel.playStatusLiveData.observe(fragment, object: Observer<AudioViewModel.AudioPlayStatus>{
        override fun onChanged(status: AudioViewModel.AudioPlayStatus?) {
          if (status == AudioViewModel.AudioPlayStatus.PREPARED) {
            if (bundle.getBoolean(KEY_IS_PLAYING)) viewModel.handlePlayPause(status)
            viewModel.handleSeekTo(bundle.getInt(KEY_CURRENT_POSITION))
            viewModel.playStatusLiveData.removeObserver(this)
          }
        }
      })
    }

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

  fun handleOnDestroy() = getAudioViewModel().handleRelease()

  fun handleSaveInstanceState(outState: Bundle) {
    val viewModel = getAudioViewModel()
    outState.putBoolean(KEY_IS_PLAYING, viewModel.getIsPlaying())
    outState.putInt(KEY_CURRENT_POSITION, viewModel.getCurrentPosition())
    outState.putString(KEY_SELECTED_LANGUAGE, selectedLanguageCode)
  }

  private fun getVoiceoverMappings(explorationId: String, stateId: String, selectedLang: String?) {
    val explorationResultLiveData = explorationDataController.getExplorationById(explorationId)
    val explorationLiveData = processExplorationLiveData(explorationResultLiveData)
    explorationLiveData.observe(fragment, Observer {
      val state = it.statesMap[stateId] ?: State.getDefaultInstance()
      val contentId = state.content.contentId
      val voiceoverMapping = (state.recordedVoiceoversMap[contentId] ?: VoiceoverMapping.getDefaultInstance()).voiceoverMappingMap

      //Json parsing not working for some reason, manually adding voiceovers
      val dummyVoiceoverMapping = getDummyVoiceoverMapping()

      languages = dummyVoiceoverMapping.keys.toList()
      selectedLanguageCode = selectedLang ?: languages.first()
      val viewModel = getAudioViewModel()
      viewModel.setVoiceoverMappings(dummyVoiceoverMapping)
      viewModel.setAudioLanguageCode(selectedLanguageCode)
    })
  }

  private fun getDummyVoiceoverMapping(): Map<String, Voiceover> {
    val dummyVoiceoverMapping = mutableMapOf<String, Voiceover>()
    val voiceover = Voiceover.newBuilder()
    voiceover.fileName = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
    dummyVoiceoverMapping["en"] = voiceover.build()
    voiceover.fileName = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
    dummyVoiceoverMapping["es"] = voiceover.build()
    voiceover.fileName = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
    dummyVoiceoverMapping["cn"] = voiceover.build()
    return dummyVoiceoverMapping
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
