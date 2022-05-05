package org.oppia.android.app.player.state.listener

interface AudioContentIdListener {
  fun contentIdForCurrentAudio(contentId: String, isPlaying: Boolean)
}
