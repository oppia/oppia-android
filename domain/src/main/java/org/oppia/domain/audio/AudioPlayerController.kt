package org.oppia.domain.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import kotlin.concurrent.withLock

/* AudioPlayerController controls interactions with an internal MediaPlayer,
*  Use initializeMediaPlayer to set a source url for the audio
*  Observe on getPlayState to get updates to status of MediaPlayer
*  Use deinitializeMediaPlayer to release MediaPlayer and stop scheduling Seek Bar updates
*/
class AudioPlayerController @Inject constructor(val context: Context) {

  inner class AudioMutableLiveData : MutableLiveData<PlayStatus>() {
    override fun onActive() {
      super.onActive()
      seekBarActive = true
    }

    override fun onInactive() {
      super.onInactive()
      seekBarActive = false
      stopUpdatingSeekBar()
    }
  }

  enum class Status { PREPARED, COMPLETED, POSITION_UPDATE }
  class PlayStatus(val type: Status, val position: Int, val duration: Int)

  private val mediaPlayer: MediaPlayer by lazy { MediaPlayer() }
  private val playState = AudioMutableLiveData()
  private var nextUpdateJob: Job? = null
  private val seekBarLock = ReentrantLock()

  private var prepared = false
  private var seekBarActive = false

  private val SEEKBAR_UPDATE_FREQUENCY = TimeUnit.SECONDS.toMillis(1)

  fun initializeMediaPlayer(url: String) {
    mediaPlayer.setOnCompletionListener {
      stopUpdatingSeekBar()
      playState.postValue(PlayStatus(Status.COMPLETED, 0, mediaPlayer.duration))
    }
    mediaPlayer.setOnPreparedListener {
      prepared = true
      playState.postValue(PlayStatus(Status.PREPARED, 0, it.duration))
    }
    mediaPlayer.setDataSource(context, Uri.parse(url))
    mediaPlayer.prepare()
  }

  fun play() {
    if (prepared && !mediaPlayer.isPlaying) {
      mediaPlayer.start()
      scheduleNextSeekBarUpdate()
    }
  }

  fun pause() {
    if (prepared && mediaPlayer.isPlaying) {
      mediaPlayer.pause()
      stopUpdatingSeekBar()
    }
  }

  private fun scheduleNextSeekBarUpdate() {
    if (seekBarActive) {
      nextUpdateJob = CoroutineScope(Dispatchers.Default).launch {
        delay(SEEKBAR_UPDATE_FREQUENCY)
        seekBarLock.withLock { updateSeekBar() }
        scheduleNextSeekBarUpdate()
      }
    }
  }

  private fun updateSeekBar() {
    if (prepared && mediaPlayer.isPlaying) {
      playState.postValue(PlayStatus(Status.POSITION_UPDATE, mediaPlayer.currentPosition, mediaPlayer.duration))
    }
  }

  private fun stopUpdatingSeekBar() {
    nextUpdateJob?.cancel()
    nextUpdateJob = null
  }

  fun deinitializeMediaPlayer () {
    mediaPlayer.release()
    stopUpdatingSeekBar()
  }

  fun isPrepared(): Boolean = prepared
  fun isPlaying() : Boolean = mediaPlayer.isPlaying
  fun seekTo(position: Int) = mediaPlayer.seekTo(position)
  fun getPlayState() : LiveData<PlayStatus> = playState
}
