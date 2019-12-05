package org.oppia.app.player.state.listener

/** Listener when audio is played/paused to highlight the content-card. */
interface AudioContentIdListener {
  fun contentIdForCurrentAudio(contentId: String, isPlaying: Boolean)
}
