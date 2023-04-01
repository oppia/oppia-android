package org.oppia.android.app.player.stopplaying

/**
 * Listener for when the current state playing session should be stop and the user navigated back to
 * the topic.
 */
interface StopStatePlayingSessionWithSavedProgressListener {

  /**
   * Leave the currently playing session without saving the current progress.
   *
   * @param isCompletion indicates whether the current session is ending due to the session
   *     resulting in a completion
   */
  fun deleteCurrentProgressAndStopSession(isCompletion: Boolean)

  /**
   * Leave the currently playing session and overwrite the oldest saved progress with the current
   * progress.
   */
  fun deleteOldestProgressAndStopSession()
}
