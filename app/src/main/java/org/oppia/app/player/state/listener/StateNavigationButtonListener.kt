package org.oppia.app.player.state.listener

/** This interface helps to know when a button has been clicked. */
interface StateNavigationButtonListener {
  fun onPreviousButtonClicked()
  fun onNextButtonClicked()
  fun onReturnToTopicButtonClicked()
  fun onSubmitButtonClicked()
  fun onContinueButtonClicked()
}
