package org.oppia.domain.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AudioPlayerController @Inject constructor(val context: Context) {

  class PlayStatus(val type: String, val value: Int)

  private var mediaPlayer: MediaPlayer? = null
  private var seekBarListener: SeekBarListener? = null
  private var executor: ScheduledExecutorService? = null
  private var prepared = false
  private val playState = MutableLiveData<PlayStatus>()

  fun initializeMediaPlayer (stringUri: String, listener: SeekBarListener) {
    if (mediaPlayer == null) mediaPlayer = MediaPlayer()
    seekBarListener = listener
    mediaPlayer?.setOnCompletionListener {
      stopUpdatingSeekBar()
      seekBarListener?.onCompleted()
      playState.postValue(PlayStatus("COMPLETE", 0))
    }
    mediaPlayer?.setDataSource(context, Uri.parse(stringUri))
    mediaPlayer?.prepareAsync()
    mediaPlayer?.setOnPreparedListener {
      prepared = true
      seekBarListener?.onDurationChanged(it.duration)
      seekBarListener?.onPositionChanged(0)
      playState.postValue(PlayStatus("DURATION", it.duration))
      playState.postValue(PlayStatus("POSITION", 0))
    }
  }

  fun play() {
    mediaPlayer?.let {
      if (prepared && !it.isPlaying) it.start()
      startUpdatingSeekBar()
    }
  }

  fun pause() {
    mediaPlayer?.let {
      if (prepared && it.isPlaying) it.pause()
    }
  }

  private fun startUpdatingSeekBar() {
    if (executor == null) executor = Executors.newSingleThreadScheduledExecutor()
    executor?.scheduleAtFixedRate({ updateSeekBar() }, 0, 1000, TimeUnit.MILLISECONDS)
  }

  private fun updateSeekBar() {
    mediaPlayer?.let {
      if (prepared && it.isPlaying) {
        seekBarListener?.onPositionChanged(it.currentPosition)
        playState.postValue(PlayStatus("POSITION", it.currentPosition))
      }
    }
  }

  private fun stopUpdatingSeekBar() {
    if (executor != null) {
      executor?.shutdown()
      executor = null
      playState.postValue(PlayStatus("POSITION", 0))
      seekBarListener?.let {
        it.onPositionChanged(0)
      }
    }
  }

  fun isPrepared(): Boolean = prepared
  fun isPlaying() : Boolean = mediaPlayer?.isPlaying ?: false
  fun seekTo(position: Int) = mediaPlayer?.seekTo(position)
  fun release() = mediaPlayer?.release()
  fun getPlayState() : LiveData<PlayStatus> = playState
}
