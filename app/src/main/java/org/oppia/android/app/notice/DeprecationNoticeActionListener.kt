package org.oppia.android.app.notice

/** Listener for when an option on any deprecation dialog is clicked. */
interface DeprecationNoticeActionListener {
  /** Called when a dialog button is clicked. */
  fun onActionButtonClicked(noticeType: DeprecationNoticeActionType)
}

/** Enum class for the various deprecation notice actions available to the user. */
enum class DeprecationNoticeActionType {
  /** Action for when the user presses the 'Close' option on a deprecation dialog. */
  CLOSE,
  /** Action for when the user presses the 'Dismiss' option on a deprecation dialog. */
  DISMISS,
  /** Action for when the user presses the 'Update' option on a deprecation dialog. */
  UPDATE
}
