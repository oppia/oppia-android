package org.oppia.app.player.stopplaying

/** Listener for when the current state playing session should be stop and the user navigated back to the topic. */
interface StopStatePlayingSessionListener {
  fun stopSession()
}
