package org.oppia.app.player.audio

import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.State
import org.oppia.app.model.Voiceover
import org.oppia.app.model.VoiceoverMapping
import org.oppia.domain.audio.AudioPlayerController
import org.oppia.domain.audio.AudioPlayerController.PlayProgress
import org.oppia.domain.audio.AudioPlayerController.PlayStatus
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResourceBucketName
import java.util.Locale
import javax.inject.Inject

/** [ViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
  private val audioPlayerController: AudioPlayerController,
  private val fragment: Fragment,
  @DefaultResourceBucketName private val gcsResource: String
) : ViewModel() {

  private lateinit var state: State
  private lateinit var explorationId: String
  private lateinit var contentId: String
  private var voiceoverMap = mapOf<String, Voiceover>()
  private val defaultLanguage = "en"
  private var languageSelectionShown = false
  private var autoPlay = false
  private var hasFeedback = false

  private lateinit var audioContentIdListener: AudioContentIdListener

  var selectedLanguageCode: String = ""
  var languages = listOf<String>()

  /** Mirrors PlayStatus in AudioPlayerController except adds LOADING state */
  enum class UiAudioPlayStatus {
    FAILED,
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

  fun setStateAndExplorationId(newState: State, id: String) {
    state = newState
    explorationId = id
  }

  fun setContentIdListener(audioContentIdListener: AudioContentIdListener) {
    this.audioContentIdListener = audioContentIdListener
  }

  fun loadMainContentAudio(allowAutoPlay: Boolean) {
    hasFeedback = false
    loadAudio(null, allowAutoPlay)
  }

  fun loadFeedbackAudio(contentId: String, allowAutoPlay: Boolean) {
    hasFeedback = true
    this.contentId = contentId
    loadAudio(contentId, allowAutoPlay)
  }

  /**
   * Load audio based on the contentId.
   *
   * @param contentId If contentId is null, then state.content.contentId is used as default.
   * @param allowAutoPlay If false, audio is guaranteed not to be autoPlayed.
   */
  private fun loadAudio(contentId: String?, allowAutoPlay: Boolean) {
    this.contentId = contentId ?: state.content.contentId
    autoPlay = allowAutoPlay
    voiceoverMap = (
      state.recordedVoiceoversMap[contentId ?: state.content.contentId]
        ?: VoiceoverMapping.getDefaultInstance()
      ).voiceoverMappingMap
    languages = voiceoverMap.keys.toList().map { it.toLowerCase(Locale.getDefault()) }
    when {
      selectedLanguageCode.isEmpty() && languages.any {
        it == defaultLanguage
      } -> setAudioLanguageCode(
        defaultLanguage
      )
      languages.any { it == selectedLanguageCode } -> setAudioLanguageCode(selectedLanguageCode)
      languages.isNotEmpty() -> {
        autoPlay = false
        languageSelectionShown = true
        val languageCode = if (languages.contains("en")) {
          "en"
        } else {
          languages.first()
        }
        setAudioLanguageCode(languageCode)
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
      if (::audioContentIdListener.isInitialized) {
        audioContentIdListener.contentIdForCurrentAudio(contentId, isPlaying = false)
      }
    } else {
      audioPlayerController.play()
      if (::audioContentIdListener.isInitialized) {
        audioContentIdListener.contentIdForCurrentAudio(contentId, isPlaying = true)
      }
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

  private fun processPlayStatusResultLiveData(
    playProgressResult: AsyncResult<PlayProgress>
  ): UiAudioPlayStatus {
    if (playProgressResult.isPending()) return UiAudioPlayStatus.LOADING
    if (playProgressResult.isFailure()) return UiAudioPlayStatus.FAILED
    return when (playProgressResult.getOrThrow().type) {
      PlayStatus.PREPARED -> {
        if (autoPlay) audioPlayerController.play()
        autoPlay = false
        UiAudioPlayStatus.PREPARED
      }
      PlayStatus.PLAYING -> UiAudioPlayStatus.PLAYING
      PlayStatus.PAUSED -> UiAudioPlayStatus.PAUSED
      PlayStatus.COMPLETED -> {
        if (hasFeedback) loadAudio(null, false)
        hasFeedback = false
        UiAudioPlayStatus.COMPLETED
      }
    }
  }

  private fun voiceOverToUri(voiceover: Voiceover?): String {
    return "https://storage.googleapis.com/$gcsResource/exploration/$explorationId/" +
      "assets/audio/${voiceover?.fileName}"
  }
}
