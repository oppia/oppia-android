package org.oppia.app.player.audio

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import org.oppia.app.fragment.FragmentScope
import org.oppia.domain.audio.AudioPlayerController
import org.oppia.domain.audio.AudioPlayerController.PlayProgress
import org.oppia.domain.audio.AudioPlayerController.PlayStatus
import org.oppia.util.data.AsyncResult
import javax.inject.Inject

/** [ViewModel] for audio-player state. */
@FragmentScope
class AudioViewModel @Inject constructor(
  private val audioPlayerController: AudioPlayerController
) : ViewModel() {
  val currentLanguageCode = ObservableField<String>("en")
  val playProgressLiveData by lazy {
    getPlayProgress()
  }

  fun setAudioLanguageCode(languageCode: String) {
    currentLanguageCode.set(languageCode)
    audioPlayerController.changeDataSource("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3")
  }

  fun handlePlayPause(type: PlayStatus) {
    when (type) {
      PlayStatus.PREPARED -> audioPlayerController.play()
      PlayStatus.PLAYING -> audioPlayerController.pause()
      PlayStatus.PAUSED -> audioPlayerController.play()
      PlayStatus.COMPLETED -> audioPlayerController.play()
      else -> {}
    }
  }

  fun handleSeekTo(position: Int) = audioPlayerController.seekTo(position)

  private fun getPlayProgress(): LiveData<PlayProgress>? {
    return Transformations.map(
      audioPlayerController.initializeMediaPlayer("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"),
      ::processPlayProgressLiveData
    )
  }

  private fun processPlayProgressLiveData(playProgressResult: AsyncResult<PlayProgress>): PlayProgress {
    if (playProgressResult.isPending()) {
      return PlayProgress(PlayStatus.PREPARING, 0, 0) //What to return here?
    }
    return playProgressResult.getOrDefault(PlayProgress(PlayStatus.PREPARING, 0, 0))
  }
}
