package org.oppia.app.player.audio

/* Interface to show or hide the audio button and change its icon. */
interface AudioButtonListener {
  /** Show audio button from toolbar. */
  fun showAudioButton()

  /** Hide audio button from toolbar. */
  fun hideAudioButton()

  /** Change audio image view src to streaming on. */
  fun showAudioStreamingOn()

  /** Change audio image view src to streaming off. */
  fun showAudioStreamingOff()
}
