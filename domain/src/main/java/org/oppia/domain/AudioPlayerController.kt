package org.oppia.domain

import android.content.Context
import android.media.MediaPlayer
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AudioPlayerController @Inject constructor(val context: Context) {
  private var mediaPlayer: MediaPlayer? = null
  private var seekBarListener: SeekBarListener? = null
  private var executor: ScheduledExecutorService? = null
  private var prepared = false

  fun initializeMediaPlayer (stringUri: String, fileDescriptor: Int, listener: SeekBarListener) {
    if (mediaPlayer == null) mediaPlayer = MediaPlayer()
    if (seekBarListener == null) seekBarListener = listener
    mediaPlayer?.setOnCompletionListener {
      stopUpdatingSeekBar()
      seekBarListener?.onCompleted()
    }
    val assetFileDescriptor = context.getResources().openRawResourceFd(fileDescriptor)
    //mediaPlayer?.setDataSource(context, Uri.parse(stringUri))
    mediaPlayer?.setDataSource(assetFileDescriptor)
    mediaPlayer?.prepareAsync()
    mediaPlayer?.setOnPreparedListener {
      prepared = true
      seekBarListener?.onDurationChanged(it.duration)
      seekBarListener?.onPositionChanged(0)
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
      }
    }
  }

  private fun stopUpdatingSeekBar() {
    if (executor != null) {
      executor?.shutdown()
      executor = null
      seekBarListener?.let {
        it.onPositionChanged(0)
      }
    }

  }


  fun isPlaying() : Boolean = mediaPlayer?.isPlaying ?: false
  fun seekTo(position: Int) = mediaPlayer?.seekTo(position)
  fun release() = mediaPlayer?.release()
}
