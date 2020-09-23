package org.oppia.android.app.player.audio

/** Interface to show or hide the audio button and change its icon. */
interface AudioButtonListener {
  /** Show audio button from toolbar. */
  fun showAudioButton()

  /** Hide audio button from toolbar. */
  fun hideAudioButton()

  /** Change audio image view src to streaming on. */
  fun showAudioStreamingOn()

  /** Change audio image view src to streaming off. */
  fun showAudioStreamingOff()

  /** Adds padding to recycler view to accommodate audio bar. */
  fun setAudioBarVisibility(isVisible: Boolean)

  /** Scroll to the top of the recycler view. */
  fun scrollToTop()
}
