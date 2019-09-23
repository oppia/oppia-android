package org.oppia.domain.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
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

/** Controller which provides audio playing capabilities.
 * [initializeMediaPlayer] should be used to download a specific audio track.
 * [releaseMediaPlayer] should be used to clean up the controller's resources.
 * See documentation for both to understand how to use them correctly. */
class AudioPlayerController @Inject constructor(
  val context: Context, @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {

  inner class AudioMutableLiveData : MutableLiveData<AsyncResult<PlayProgress>>() {
    override fun onActive() {
      super.onActive()
      observerActive = true
    }

    override fun onInactive() {
      super.onInactive()
      observerActive = false
      stopUpdatingSeekBar()
    }
  }

  enum class PlayStatus { PREPARED, PLAYED, PAUSED, COMPLETED}
  class PlayProgress(val type: PlayStatus, val position: Int, val duration: Int)
  class MediaPlayerAlreadyInitException : Exception()
  class MediaPlayerNotInitException : Exception()

  private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }
  private val playState = AudioMutableLiveData()
  private var nextUpdateJob: Job? = null
  private val seekBarLock = ReentrantLock()

  private var prepared = false
  private var observerActive = false
  private var mediaPlayerActive = false

  private val SEEKBAR_UPDATE_FREQUENCY = TimeUnit.SECONDS.toMillis(1)

  /* Call function to begin loading a audio sourced specified by url
  *  MediaPlayer must be inactive when calling this function */
  fun initializeMediaPlayer(url: String) {
    if (mediaPlayerActive) throw MediaPlayerAlreadyInitException()
    mediaPlayerActive = true
    mediaPlayer.setOnCompletionListener {
      stopUpdatingSeekBar()
      playState.postValue(AsyncResult.success(PlayProgress(PlayStatus.COMPLETED, 0, mediaPlayer.duration)))
    }
    mediaPlayer.setOnPreparedListener {
      prepared = true
      playState.postValue(AsyncResult.success(PlayProgress(PlayStatus.PREPARED, 0, it.duration)))
    }
    mediaPlayer.setDataSource(context, Uri.parse(url))
    mediaPlayer.prepareAsync()
    playState.postValue(AsyncResult.pending())
  }

  /* Call function to change data source of media player
  *  Puts media player in a preparing state */
  fun changeDataSource(url: String) {
    prepared = false
    stopUpdatingSeekBar()
    mediaPlayer.reset()
    mediaPlayer.setDataSource(context, Uri.parse(url))
    mediaPlayer.prepareAsync()
    playState.postValue(AsyncResult.pending())
  }

  /** Call function to play audio.
   * Must call [initializeMediaPlayer] and wait for prepared state first.
   * MediaPlayer should be in paused state */
  fun play() {
    check(prepared) { "Media Player not in a prepared state" }
    if (!mediaPlayer.isPlaying) {
      mediaPlayer.start()
      scheduleNextSeekBarUpdate()
    }
  }

  /** Call function to pause audio.
   * Must call [initializeMediaPlayer] and wait for prepared state first.
   * MediaPlayer should be in playing state */
  fun pause() {
    check(prepared) { "Media Player not in a prepared state" }
    if (mediaPlayer.isPlaying) {
      playState.postValue(AsyncResult.success(PlayProgress(PlayStatus.PAUSED, mediaPlayer.currentPosition, mediaPlayer.duration)))
      mediaPlayer.pause()
      stopUpdatingSeekBar()
    }
  }

  private fun scheduleNextSeekBarUpdate() {
    if (observerActive && prepared) {
      seekBarLock.withLock {
        nextUpdateJob = CoroutineScope(backgroundDispatcher).launch {
          delay(SEEKBAR_UPDATE_FREQUENCY)
          updateSeekBar()
          scheduleNextSeekBarUpdate()
        }
      }
    }
  }

  private fun updateSeekBar() {
    if (mediaPlayer.isPlaying) {
      playState.postValue(AsyncResult.success(PlayProgress(PlayStatus.PLAYED, mediaPlayer.currentPosition, mediaPlayer.duration)))
    }
  }

  private fun stopUpdatingSeekBar() {
    seekBarLock.withLock {
      nextUpdateJob?.cancel()
      nextUpdateJob = null
    }
  }

  /* Call function to release media player and stop seek bar updates
  *  MediaPlayer must be active when calling this function */
  fun releaseMediaPlayer() {
    if (!mediaPlayerActive) throw MediaPlayerNotInitException()
    mediaPlayerActive = false
    prepared = false
    mediaPlayer.release()
    stopUpdatingSeekBar()
  }

  /** Call function to check playing state
  *  Must call [initializeMediaPlayer] and wait for prepared state first. */
  fun isPlaying() : Boolean {
    check(prepared) { "Media Player not in a prepared state" }
    return mediaPlayer.isPlaying
  }

  /** Call function to change progress of MediaPlayer
   *  Must call [initializeMediaPlayer] and wait for prepared state first. */
  fun seekTo(position: Int)  {
    check(prepared) { "Media Player not in a prepared state" }
    mediaPlayer.seekTo(position)
  }

  /* Observe to get updates on MediaPlayer current Progress
  *  Prepared: Set duration of seek bar and attach listeners to UI
  *  Playing: Update seek bar with current position
  *  Completed: Reset play button and seek bar to original state */
  fun getPlayState() : LiveData<AsyncResult<PlayProgress>> = playState
}
