package org.oppia.android.app.player.stopplaying

/** Listener for when the current state playing session should be stop and the user navigated back to the topic. */
interface StopStatePlayingSessionWithSavedProgressListener {
  /** leave the currently playing session without saving the current progress. */
  fun deleteCurrentProgressStopCurrentSession()

  /**
   * leave the currently playing session and overwrite the oldest saved progress with the current
   * progress.
   */
  fun deleteOldestProgressAndStopCurrentSession()
}
