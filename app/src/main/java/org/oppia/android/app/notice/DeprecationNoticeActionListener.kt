package org.oppia.android.app.notice

import org.oppia.android.app.splash.DeprecationNoticeActionType

/** Listener for when an option on any deprecation dialog is clicked. */
interface DeprecationNoticeActionListener {
  /** Called when a dialog button is clicked. */
  fun onActionButtonClicked(noticeType: DeprecationNoticeActionType)
}
