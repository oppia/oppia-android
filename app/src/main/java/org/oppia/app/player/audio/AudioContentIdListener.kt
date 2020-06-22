package org.oppia.app.player.audio

/** Listener when audio is played/paused to highlight the content-card. */
interface AudioContentIdListener {
  fun contentIdForCurrentAudio(contentId: String, isPlaying: Boolean)
}
