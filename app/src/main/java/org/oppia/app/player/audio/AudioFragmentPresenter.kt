package org.oppia.app.player.audio

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

private const val TAG_LANGUAGE_DIALOG = "LANGUAGE_DIALOG"

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

  private var selectedLanguageCode: String = "en"
  private lateinit var languages: Set<String>

  fun handleCreateView(inflater: LayoutInflater, container: ViewGroup?, explorationId: String, stateId: String): View? {
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
    val voiceoverMap = getVoiceoverMappings(explorationId, stateId)
    languages = voiceoverMap.keys
    val viewModel = getAudioViewModel()
    viewModel.setVoiceoverMappings(voiceoverMap)
    viewModel.playStatusLiveData.observe(fragment, Observer {
      binding.sbAudioProgress.isEnabled = it != AudioViewModel.AudioPlayStatus.LOADING
    })
    binding.let {
      it.viewModel = viewModel
      it.audioFragment = fragment as AudioFragment
      it.lifecycleOwner = fragment
    }
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
      languages.toList() as ArrayList<String>,
      selectedLanguageCode
    )
    dialogFragment.showNow(fragment.childFragmentManager, TAG_LANGUAGE_DIALOG)
  }

  private fun getVoiceoverMappings(explorationId: String, stateId: String): Map<String, Voiceover> {
    val explorationResultLiveData = explorationDataController.getExplorationById(explorationId)
    val explorationLiveData = processExplorationLiveData(explorationResultLiveData)
    val exploration = checkNotNull(explorationLiveData.value)
    val state = exploration.statesMap.getOrDefault(stateId, State.getDefaultInstance())
    val contentId = state.content.contentId
    return state.recordedVoiceoversMap.getOrDefault(contentId, VoiceoverMapping.getDefaultInstance()).voiceoverMappingMap
  }

//  private fun getDummyAudioLanguageList(): List<String> {
//    val languageCodeList = ArrayList<String>()
//    languageCodeList.add("en")
//    languageCodeList.add("hi")
//    languageCodeList.add("hi-en")
//    return languageCodeList
//  }

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
