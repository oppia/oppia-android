package org.oppia.android.app.notice

import org.oppia.android.app.model.DeprecationResponse

/** Listener for when an option on any deprecation dialog is clicked. */
interface DeprecationNoticeActionListener {
  /** Called when a dialog button is clicked. */
  fun onActionButtonClicked(noticeActionResponse: DeprecationNoticeActionResponse)
}

data class DeprecationNoticeActionResponse(
  val deprecationResponse: DeprecationResponse,
  val deprecationNoticeActionType: DeprecationNoticeActionType,
)

/** Enum class for the various deprecation notice actions available to the user. */
enum class DeprecationNoticeActionType {
  /** Action for when the user presses the 'Close' button on a deprecation dialog. */
  CLOSE,
  /** Action for when the user presses the 'Dismiss' button on a deprecation dialog. */
  DISMISS,
  /** Action for when the user presses the 'Update' button on a deprecation dialog. */
  UPDATE
}
