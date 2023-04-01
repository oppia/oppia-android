package org.oppia.android.app.player.exploration

/** Listener for scenarios when the user should be shown a spotlight for playing voiceovers. */
interface RequestVoiceOverIconSpotlightListener {
  /** Requests to show a voice-over icon spotlight. */
  fun requestVoiceOverIconSpotlight(numberOfLogins: Int)
}
