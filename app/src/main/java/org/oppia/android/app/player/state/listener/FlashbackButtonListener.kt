package org.oppia.android.app.player.state.listener

/** Listener for when the 'See example' button is clicked. */
interface FlashbackButtonListener {
  /** Called when the 'See example' button is clicked. */
  fun onFlashbackButtonClicked(stateName: String)
}
