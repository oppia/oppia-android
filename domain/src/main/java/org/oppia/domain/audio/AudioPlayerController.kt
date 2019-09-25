package org.oppia.domain.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.util.data.AsyncResult
import org.oppia.util.threading.BackgroundDispatcher
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

/**
 * Controller which provides audio playing capabilities.
 * [initializeMediaPlayer] should be used to download a specific audio track.
 * [releaseMediaPlayer] should be used to clean up the controller's resources.
 * See documentation for both to understand how to use them correctly.
 */

class AudioPlayerController @Inject constructor(
  private val context: Context,
  private val fragment: Fragment,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {

  inner class AudioMutableLiveData : MutableLiveData<AsyncResult<PlayProgress>>() {
    override fun onActive() {
      super.onActive()
      audioLock.withLock {
        observerActive = true
      }
    }

    override fun onInactive() {
      super.onInactive()
      audioLock.withLock {
        observerActive = false
        stopUpdatingSeekBar()
      }
    }
  }

  /**
   * [PREPARED]: mediaPlayer in "Prepared" state, ready to play(), pause(), seekTo()
   * [PLAYING]: mediaPlayer in "Started" state, ready to pause(), seekTo()
   * [PAUSED]: mediaPlayer in "Paused" state, ready to play(), seekTo()
   * [COMPLETED]: mediaPlayer in "PlaybackCompleted" state, ready to play(), seekTo()
   */
  enum class PlayStatus { PREPARED, PLAYING, PAUSED, COMPLETED }

  /**
   * [type]: Represents current state of mediaPlayer, see above
   * [position]: Represents mediaPlayer's current position in playback
   * [duration]: Represents duration of current audio
   */
  class PlayProgress(val type: PlayStatus, val position: Int, val duration: Int)

  private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }
  private var playProgress: AudioMutableLiveData? = null
  private var nextUpdateJob: Job? = null
  private val audioLock = ReentrantLock()

  private var prepared = false
  private var observerActive = false
  private var mediaPlayerActive = false

  private val SEEKBAR_UPDATE_FREQUENCY = TimeUnit.SECONDS.toMillis(1)

  /**
   * Call function to begin loading a audio sourced specified by url
   * MediaPlayer must be inactive when calling this function
   */
  fun initializeMediaPlayer(url: String): LiveData<AsyncResult<PlayProgress>> {
    audioLock.withLock {
      check(!mediaPlayerActive) { "Media player has already been initialized" }
      mediaPlayer.reset()
      mediaPlayerActive = true
      setMediaPlayerListeners()
      prepareDataSource(url)
      playProgress = AudioMutableLiveData()
      return playProgress!!
    }
  }

  /*
   * Call function to change data source of media player
   * Puts media player in a preparing state
   */
  fun changeDataSource(url: String) {
    audioLock.withLock {
      prepared = false
      stopUpdatingSeekBar()
      mediaPlayer.reset()
      prepareDataSource(url)
    }
  }

  private fun setMediaPlayerListeners() {
    mediaPlayer.setOnCompletionListener {
      stopUpdatingSeekBar()
      playProgress?.postValue(
        AsyncResult.success(PlayProgress(PlayStatus.COMPLETED, 0, mediaPlayer.duration))
      )
    }
    mediaPlayer.setOnPreparedListener {
      prepared = true
      playProgress?.postValue(AsyncResult.success(PlayProgress(PlayStatus.PREPARED, 0, it.duration)))
    }
  }

  private fun prepareDataSource(url: String) {
    mediaPlayer.setDataSource(context, Uri.parse(url))
    mediaPlayer.prepareAsync()
    playProgress?.postValue(AsyncResult.pending())
  }

  /**
   * Call function to play audio.
   * Must call [initializeMediaPlayer] and wait for prepared state first.
   * MediaPlayer should be in paused state
   */
  fun play() {
    audioLock.withLock {
      check(prepared) { "Media Player not in a prepared state" }
      if (!mediaPlayer.isPlaying) {
        mediaPlayer.start()
        scheduleNextSeekBarUpdate()
      }
    }
  }

  /**
   * Call function to pause audio.
   * Must call [initializeMediaPlayer] and wait for prepared state first.
   * MediaPlayer should be in playing state
   */
  fun pause() {
    audioLock.withLock {
      check(prepared) { "Media Player not in a prepared state" }
      if (mediaPlayer.isPlaying) {
        playProgress?.value =
          AsyncResult.success(
            PlayProgress(PlayStatus.PAUSED, mediaPlayer.currentPosition, mediaPlayer.duration)
          )
        mediaPlayer.pause()
        stopUpdatingSeekBar()
      }
    }
  }

  private fun scheduleNextSeekBarUpdate() {
    audioLock.withLock {
      if (observerActive && prepared) {
        nextUpdateJob = CoroutineScope(backgroundDispatcher).launch {
          updateSeekBar()
          delay(SEEKBAR_UPDATE_FREQUENCY)
          scheduleNextSeekBarUpdate()
        }
      }
    }
  }

  private fun updateSeekBar() {
    audioLock.withLock {
      if (mediaPlayer.isPlaying) {
        playProgress?.postValue(
          AsyncResult.success(
            PlayProgress(PlayStatus.PLAYING, mediaPlayer.currentPosition, mediaPlayer.duration)
          )
        )
      }
    }
  }

  private fun stopUpdatingSeekBar() {
    audioLock.withLock {
      nextUpdateJob?.cancel()
      nextUpdateJob = null
    }
  }

  /**
   * Call function to release media player and stop seek bar updates
   * MediaPlayer must be active when calling this function
   */
  fun releaseMediaPlayer() {
    audioLock.withLock {
      check(mediaPlayerActive) { "Media player has not been previously initialized" }
      mediaPlayerActive = false
      prepared = false
      mediaPlayer.release()
      stopUpdatingSeekBar()
      playProgress?.removeObservers(fragment)
      playProgress = null
    }
  }

  /**
   * Call function to change progress of MediaPlayer
   * Must call [initializeMediaPlayer] and wait for prepared state first.
   */
  fun seekTo(position: Int)  {
    audioLock.withLock {
      check(prepared) { "Media Player not in a prepared state" }
      mediaPlayer.seekTo(position)
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun getTestMediaPlayer(): MediaPlayer = mediaPlayer

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun getNextUpdateJob(): Job? = nextUpdateJob
}
