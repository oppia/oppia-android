package org.oppia.android.app.survey

/**
 *  A handler for receiving any change in answer availability to update the 'next' button.
 */
interface SelectedAnswerAvailabilityReceiver {

  /**
   *  Called when the input answer availability changes.
   */
  fun onPendingAnswerAvailabilityCheck(inputAnswerAvailable: Boolean)
}
