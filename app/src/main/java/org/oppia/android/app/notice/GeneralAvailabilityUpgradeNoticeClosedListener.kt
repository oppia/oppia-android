package org.oppia.android.app.notice

/** Listener for when the general availability update dialog is closed. */
interface GeneralAvailabilityUpgradeNoticeClosedListener {
  /**
   * Called when the notice dialog was closed.
   *
   * @param permanentlyDismiss whether the user never wants to see this notice again
   */
  fun onGaUpgradeNoticeOkayButtonClicked(permanentlyDismiss: Boolean)
}
