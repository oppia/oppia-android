package org.oppia.app.player.state.listener

/** This interface helps to know when a button has been clicked. */
interface ButtonInteractionListener {
  fun onPreviousButtonClicked()
  fun onNextButtonClicked()
  fun onInteractionButtonClicked()
}
