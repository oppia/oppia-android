package org.oppia.android.app.notice

import org.oppia.android.app.splash.DeprecationNoticeActionType

/** Listener for when an option on any deprecation dialog is clicked. */
interface DeprecationNoticeActionListener {
  /** Called when the positive dialog button is clicked. */
  fun onPositiveActionButtonClicked(noticeType: DeprecationNoticeActionType)

  /** Called when the negative dialog button is clicked. */
  fun onNegativeActionButtonClicked(noticeType: DeprecationNoticeActionType)
}
