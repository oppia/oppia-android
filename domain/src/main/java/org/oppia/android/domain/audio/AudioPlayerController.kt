package org.oppia.android.domain.audio

import android.media.MediaPlayer
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.oppia.android.domain.oppialogger.OppiaLogger
import org.oppia.android.domain.oppialogger.analytics.LearnerAnalyticsLogger
import org.oppia.android.domain.oppialogger.exceptions.ExceptionsController
import org.oppia.android.util.data.AsyncResult
import org.oppia.android.util.threading.BackgroundDispatcher
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
  private val oppiaLogger: OppiaLogger,
  private val exceptionsController: ExceptionsController,
  private val learnerAnalyticsLogger: LearnerAnalyticsLogger,
  @BackgroundDispatcher private val backgroundDispatcher: CoroutineDispatcher
) {

  inner class AudioMutableLiveData :
    MutableLiveData<AsyncResult<PlayProgress>>(AsyncResult.Pending()) {
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
    COMPLETED // mediaPlayer in "PlaybackCompleted" state, ready to play(), seekTo().
  }

  /**
   * [type]: See above.
   * [position]: Represents mediaPlayer's current position in playback.
   * [duration]: Represents duration of current audio.
   */
  class PlayProgress(val type: PlayStatus, val position: Int, val duration: Int)

  /** General audio player exception used in on error listener. */
  class AudioPlayerException(message: String) : Exception(message)

  private var mediaPlayer: MediaPlayer = MediaPlayer()
  private var playProgress: AudioMutableLiveData? = null
  private var nextUpdateJob: Job? = null
  private val audioLock = ReentrantLock()

  private var prepared = false
  private var observerActive = false
  private var mediaPlayerActive = false
  private var isReleased = false
  private var duration = 0
  private var completed = false
  private var currentContentId: String? = null
  private var currentLanguageCode: String? = null

  private val SEEKBAR_UPDATE_FREQUENCY = TimeUnit.SECONDS.toMillis(1)

  /**
   * Loads audio source from a URL and return LiveData to send updates.
   * This controller cannot already be initialized.
   */
  fun initializeMediaPlayer(): LiveData<AsyncResult<PlayProgress>> {
    audioLock.withLock {
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
  fun changeDataSource(url: String, contentId: String?, languageCode: String) {
    audioLock.withLock {
      prepared = false
      currentContentId = contentId
      currentLanguageCode = languageCode
      stopUpdatingSeekBar()
      mediaPlayer.reset()
      prepareDataSource(url)
    }
  }

  private fun setMediaPlayerListeners() {
    mediaPlayer.setOnCompletionListener {
      completed = true
      stopUpdatingSeekBar()
      playProgress?.value =
        AsyncResult.Success(PlayProgress(PlayStatus.COMPLETED, 0, duration))
    }
    mediaPlayer.setOnPreparedListener {
      prepared = true
      duration = it.duration
      playProgress?.value =
        AsyncResult.Success(PlayProgress(PlayStatus.PREPARED, 0, duration))
    }
    mediaPlayer.setOnErrorListener { _, what, extra ->
      playProgress?.value =
        AsyncResult.Failure(
          AudioPlayerException("Audio Player put in error state with what: $what and extra: $extra")
        )
      releaseMediaPlayer()
      initializeMediaPlayer()
      // Indicates that error was handled and to not invoke completion listener.
      return@setOnErrorListener true
    }
  }

  private fun prepareDataSource(url: String) {
    try {
      mediaPlayer.setDataSource(url)
      mediaPlayer.prepareAsync()
    } catch (e: IOException) {
      exceptionsController.logNonFatalException(e)
      oppiaLogger.e("AudioPlayerController", "Failed to set data source for media player", e)
    }
    playProgress?.value = AsyncResult.Pending()
  }

  /**
   * Puts MediaPlayer in started state and begins sending seek bar updates.
   * Controller must already have audio prepared.
   */
  fun play(isPlayingFromAutoPlay: Boolean, reloadingMainContent: Boolean) {
    audioLock.withLock {
      check(prepared) { "Media Player not in a prepared state" }
      if (!mediaPlayer.isPlaying) {
        mediaPlayer.start()
        scheduleNextSeekBarUpdate()

        // Log an auto play only if it's the one that initiates playing audio (since it more or less
        // corresponds to manually clicking the 'play' button). Note this will not log any play
        // events after the state completes (since there'll no longer be a state logger).
        if (!isPlayingFromAutoPlay || !reloadingMainContent) {
          val explorationLogger = learnerAnalyticsLogger.explorationAnalyticsLogger.value
          val stateLogger = explorationLogger?.stateAnalyticsLogger?.value
          stateLogger?.logPlayVoiceOver(currentContentId, currentLanguageCode)
        }
      }
    }
  }

  /**
   * Puts MediaPlayer in paused state and stops sending seek bar updates.
   *
   * The controller must already have audio prepared.
   *
   * @param isFromExplicitUserAction indicates whether this pause is from an explicit user action
   *     (like clicking a pause button) vs. an incidental one (like an autoplay transition or
   *     closing the audio bar)
   */
  fun pause(isFromExplicitUserAction: Boolean) {
    audioLock.withLock {
      check(prepared) { "Media Player not in a prepared state" }
      if (mediaPlayer.isPlaying) {
        playProgress?.value =
          AsyncResult.Success(
            PlayProgress(PlayStatus.PAUSED, mediaPlayer.currentPosition, duration)
          )
        mediaPlayer.pause()
        stopUpdatingSeekBar()

        if (isFromExplicitUserAction) {
          val explorationLogger = learnerAnalyticsLogger.explorationAnalyticsLogger.value
          val stateLogger = explorationLogger?.stateAnalyticsLogger?.value
          stateLogger?.logPauseVoiceOver(currentContentId, currentLanguageCode)
        }
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
        val position = if (completed) 0 else mediaPlayer.currentPosition
        completed = false
        playProgress?.postValue(
          AsyncResult.Success(
            PlayProgress(PlayStatus.PLAYING, position, mediaPlayer.duration)
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
      if (!isReleased) {
        check(mediaPlayerActive) { "Media player has not been previously initialized" }
        mediaPlayerActive = false
        isReleased = true
        prepared = false
        mediaPlayer.release()
        stopUpdatingSeekBar()
        playProgress = null
      }
    }
  }

  /**
   * Seek to specific position in MediaPlayer.
   * Controller must already have audio prepared.
   */
  fun seekTo(position: Int) {
    audioLock.withLock {
      check(prepared) { "Media Player not in a prepared state" }
      mediaPlayer.seekTo(position)
    }
  }

  @VisibleForTesting(otherwise = VisibleForTesting.NONE)
  fun getTestMediaPlayer(): MediaPlayer = mediaPlayer
}
