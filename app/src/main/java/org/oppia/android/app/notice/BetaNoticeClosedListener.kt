package org.oppia.android.app.notice

/** Listener for when the beta notice dialog is closed. */
interface BetaNoticeClosedListener {
  /**
   * Called when the notice dialog was closed.
   *
   * @param permanentlyDismiss whether the user never wants to see this notice again
   */
  fun onBetaNoticeOkayButtonClicked(permanentlyDismiss: Boolean)
}
