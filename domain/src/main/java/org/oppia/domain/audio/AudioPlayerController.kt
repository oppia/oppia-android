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
import org.oppia.util.logging.Logger
import org.oppia.util.threading.BackgroundDispatcher
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.concurrent.withLock

/**
 * Controller which provides audio playing capabilities.
 * [initializeMediaPlayer] should be used to download a specific audio track.
 * [releaseMediaPlayer] should be used to clean up the controller's resources.
 * See documentation for both to understand how to use them correctly.
 */
@Singleton
class AudioPlayerController @Inject constructor(
  private val logger: Logger,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {

  inner class AudioMutableLiveData : MutableLiveData<AsyncResult<PlayProgress>>() {
    override fun onActive() {
      super.onActive()
      audioLock.withLock {
        observerActive = true
        if (prepared && mediaPlayer.isPlaying)
          scheduleNextSeekBarUpdate()
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

  /** Represents current state of internal Media Player. */
  enum class PlayStatus {
    PREPARED, // mediaPlayer in "Prepared" state, ready to play(), pause(), seekTo().
    PLAYING, // mediaPlayer in "Started" state, ready to pause(), seekTo().
    PAUSED, // mediaPlayer in "Paused" state, ready to play(), seekTo().
    COMPLETED  // mediaPlayer in "PlaybackCompleted" state, ready to play(), seekTo().
  }

  /**
   * [type]: See above.
   * [position]: Represents mediaPlayer's current position in playback.
   * [duration]: Represents duration of current audio.
   */
  class PlayProgress(val type: PlayStatus, val position: Int, val duration: Int)

  private var mediaPlayer: MediaPlayer = MediaPlayer()
  private var playProgress: AudioMutableLiveData? = null
  private var nextUpdateJob: Job? = null
  private val audioLock = ReentrantLock()

  private var prepared = false
  private var observerActive = false
  private var mediaPlayerActive = false
  private var isReleased = false

  private val SEEKBAR_UPDATE_FREQUENCY = TimeUnit.SECONDS.toMillis(1)

  /**
   * Loads audio source from a URL and return LiveData to send updates.
   * This controller cannot already be initialized.
   */
  fun initializeMediaPlayer(): LiveData<AsyncResult<PlayProgress>> {
    audioLock.withLock {
      check(!mediaPlayerActive) { "Media player has already been initialized" }
      mediaPlayerActive = true
      if (isReleased) {
        // Recreation is necessary since media player's resources have been released
        mediaPlayer = MediaPlayer()
        isReleased = false
      }
      setMediaPlayerListeners()
    }
    val progressLiveData = AudioMutableLiveData()
    playProgress = progressLiveData
    return progressLiveData
  }

  /**
   * Changes audio source to specified.
   * Stops sending seek bar updates and put MediaPlayer in preparing state.
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
      playProgress?.value =
        AsyncResult.success(PlayProgress(PlayStatus.COMPLETED, 0, it.duration))
    }
    mediaPlayer.setOnPreparedListener {
      prepared = true
      playProgress?.value =
        AsyncResult.success(PlayProgress(PlayStatus.PREPARED, 0, it.duration))
    }
  }

  private fun prepareDataSource(url: String) {
    try {
      mediaPlayer.setDataSource(url)
      mediaPlayer.prepareAsync()
    } catch (e: IOException) {
      logger.e("AudioPlayerController", "Failed to set data source for media player", e)
    }
    playProgress?.value = AsyncResult.pending()
  }

  /**
   * Puts MediaPlayer in started state and begins sending seek bar updates.
   * Controller must already have audio prepared.
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
   * Puts MediaPlayer in paused state and stops sending seek bar updates.
   * Controller must already have audio prepared.
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
   * Puts MediaPlayer in end state and releases resources.
   * Stop updating seek bar and removes all observers.
   * MediaPlayer must already be initialized.
   */
  fun releaseMediaPlayer() {
    audioLock.withLock {
      check(mediaPlayerActive) { "Media player has not been previously initialized" }
      mediaPlayerActive = false
      isReleased = true
      prepared = false
      mediaPlayer.release()
      stopUpdatingSeekBar()
      playProgress = null
    }
  }

  /**
   * Seek to specific position in MediaPlayer.
   * Controller must already have audio prepared.
   */
  fun seekTo(position: Int)  {
    audioLock.withLock {
      check(prepared) { "Media Player not in a prepared state" }
      mediaPlayer.seekTo(position)
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun getTestMediaPlayer(): MediaPlayer = mediaPlayer
}
