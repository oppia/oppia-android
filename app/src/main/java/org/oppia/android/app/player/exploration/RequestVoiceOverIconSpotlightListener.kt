package org.oppia.android.app.player.exploration

/** Interface to request voice-over icon spotlight. */
interface RequestVoiceOverIconSpotlightListener {
  /** Requests the spotlight fragment to show voice-over icon spotlight. */
  fun requestVoiceOverIconSpotlight(numberOfLogins: Int)
}
