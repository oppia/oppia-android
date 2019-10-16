package org.oppia.app.player.state

interface InteractionListener{
  fun onPreviousButtonClicked()
  fun onNextButtonClicked()
  fun onInteractionButtonClicked()
}