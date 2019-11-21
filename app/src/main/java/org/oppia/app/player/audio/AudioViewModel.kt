package org.oppia.app.player.audio

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Exploration
import org.oppia.app.model.State
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.domain.audio.AudioPlayerController
import org.oppia.domain.audio.AudioPlayerController.PlayProgress
import org.oppia.domain.audio.AudioPlayerController.PlayStatus
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResource
import org.oppia.util.logging.Logger
import java.util.Locale
import javax.inject.Inject

/** [ViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
  private val audioPlayerController: AudioPlayerController,
  private val logger: Logger,
  private val explorationDataController: ExplorationDataController,
  private val fragment: Fragment,
  @DefaultResource private val gcsResource: String
) : ViewModel() {

  private lateinit var exploration: Exploration
  private lateinit var explorationId: String
  private var voiceoverMap = mapOf<String, Voiceover>()
  private val defaultLanguage = "en"
  private var languageSelectionShown = false

  var selectedLanguageCode: String = ""
  var languages = listOf<String>()

  /** Mirrors PlayStatus in AudioPlayerController except adds LOADING state */
  enum class UiAudioPlayStatus {
    LOADING,
    PREPARED,
    PLAYING,
    PAUSED,
    COMPLETED
  }

  val currentLanguageCode = ObservableField<String>()

  val durationLiveData: LiveData<Int> by lazy {
    processDurationLiveData()
  }
  val positionLiveData: LiveData<Int> by lazy {
    processPositionLiveData()
  }
  val playStatusLiveData: LiveData<UiAudioPlayStatus> by lazy {
    processPlayStatusLiveData()
  }

  fun setVoiceoverMappings(explorationId: String, stateId: String, contentId: String? = null) {
    this.explorationId = explorationId
    if (!::exploration.isInitialized) {
      explorationDataController.getExplorationById(explorationId).observeOnce(fragment, Observer {
        if (it.isFailure()) {
          logger.e("AudioFragment", "Failed to get Exploration", it.getErrorOrNull()!!)
        }
        exploration = it.getOrDefault(Exploration.getDefaultInstance())
        setVoiceoverMappingsByStateAndContentId(stateId, contentId)
      })
    } else {
      setVoiceoverMappingsByStateAndContentId(stateId, contentId)
    }
  }

  private fun setVoiceoverMappingsByStateAndContentId(stateId: String, contentId: String?) {
    val state = exploration.statesMap[stateId] ?: State.getDefaultInstance()
    voiceoverMap = (state.recordedVoiceoversMap[contentId ?: state.content.contentId] ?: VoiceoverMapping.getDefaultInstance()).voiceoverMappingMap
    languages = voiceoverMap.keys.toList().map { it.toLowerCase(Locale.getDefault()) }
    when {
      selectedLanguageCode.isEmpty() && languages.any { it == defaultLanguage } -> setAudioLanguageCode(defaultLanguage)
      languages.any { it == selectedLanguageCode } -> setAudioLanguageCode(selectedLanguageCode)
      languages.isNotEmpty() -> {
        languageSelectionShown = true
        val languageCode = if (languages.any { it == exploration.languageCode }) {
          exploration.languageCode
        } else {
          languages.first()
        }
        setAudioLanguageCode(languageCode)
        (fragment as LanguageInterface).languageSelectionClicked()
      }
    }
  }

  /** Sets language code for data binding and changes data source to correct audio */
  fun setAudioLanguageCode(languageCode: String) {
    selectedLanguageCode = languageCode
    currentLanguageCode.set(languageCode)
    audioPlayerController.changeDataSource(voiceOverToUri(voiceoverMap[languageCode]))
  }

  /** Plays or pauses AudioController depending on passed in state */
  fun togglePlayPause(type: UiAudioPlayStatus?) {
    if (type == UiAudioPlayStatus.PLAYING) {
      audioPlayerController.pause()
    } else {
      audioPlayerController.play()
    }
  }

  fun playAudio() {
    // Do not auto play audio if the LanguageDialogFragment was shown
    if (!languageSelectionShown) {
      audioPlayerController.play()
    } else {
      languageSelectionShown = false
    }
  }

  fun pauseAudio() = audioPlayerController.pause()
  fun handleSeekTo(position: Int) = audioPlayerController.seekTo(position)
  fun handleRelease() = audioPlayerController.releaseMediaPlayer()

  private val playProgressResultLiveData: LiveData<AsyncResult<PlayProgress>> by lazy {
    audioPlayerController.initializeMediaPlayer()
  }

  private fun processDurationLiveData(): LiveData<Int> {
    return Transformations.map(playProgressResultLiveData, ::processDurationResultLiveData)
  }

  private fun processPositionLiveData(): LiveData<Int> {
    return Transformations.map(playProgressResultLiveData, ::processPositionResultLiveData)
  }

  private fun processPlayStatusLiveData(): LiveData<UiAudioPlayStatus> {
    return Transformations.map(playProgressResultLiveData, ::processPlayStatusResultLiveData)
  }

  private fun processDurationResultLiveData(playProgressResult: AsyncResult<PlayProgress>): Int {
    if (playProgressResult.isPending()) {
      return 0
    }
    return playProgressResult.getOrThrow().duration
  }

  private fun processPositionResultLiveData(playProgressResult: AsyncResult<PlayProgress>): Int {
    if (playProgressResult.isPending()) {
      return 0
    }
    return playProgressResult.getOrThrow().position
  }

  private fun processPlayStatusResultLiveData(playProgressResult: AsyncResult<PlayProgress>): UiAudioPlayStatus {
    if (playProgressResult.isPending()) return UiAudioPlayStatus.LOADING
    return when (playProgressResult.getOrThrow().type) {
      PlayStatus.PREPARED -> UiAudioPlayStatus.PREPARED
      PlayStatus.PLAYING -> UiAudioPlayStatus.PLAYING
      PlayStatus.PAUSED -> UiAudioPlayStatus.PAUSED
      PlayStatus.COMPLETED -> UiAudioPlayStatus.COMPLETED
    }
  }

  private fun voiceOverToUri(voiceover: Voiceover?): String {
    return "https://storage.googleapis.com/$gcsResource/exploration/$explorationId/assets/audio/${voiceover?.fileName}"
  }

  private fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
      override fun onChanged(t: T?) {
        observer.onChanged(t)
        removeObserver(this)
      }
    })
  }
}
