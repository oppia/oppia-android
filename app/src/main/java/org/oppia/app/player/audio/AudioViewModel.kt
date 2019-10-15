package org.oppia.app.player.audio

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.app.model.Voiceover
import org.oppia.domain.audio.AudioPlayerController
import org.oppia.domain.audio.AudioPlayerController.PlayProgress
import org.oppia.domain.audio.AudioPlayerController.PlayStatus
import org.oppia.util.data.AsyncResult
import org.oppia.util.gcsresource.DefaultResource
import javax.inject.Inject

/** [ViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
  private val audioPlayerController: AudioPlayerController,
  @DefaultResource private val gcsResource: String
) : ViewModel() {

  private lateinit var explorationId: String
  private var voiceoverMap = mapOf<String, Voiceover>()

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

  fun setVoiceoverMappings(map : Map<String, Voiceover>) {
    voiceoverMap = map
  }

  fun setExplorationId(id: String) {
    explorationId = id
  }

  /** Sets language code for data binding and changes data source to correct audio */
  fun setAudioLanguageCode(languageCode: String) {
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
}
