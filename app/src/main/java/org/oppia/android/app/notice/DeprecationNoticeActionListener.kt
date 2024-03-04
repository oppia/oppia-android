package org.oppia.android.app.notice

import org.oppia.android.app.model.DeprecationNoticeType

/** Listener for when an option on any deprecation dialog is clicked. */
interface DeprecationNoticeActionListener {
  /** Called when a dialog button is clicked. */
  fun onActionButtonClicked(noticeActionResponse: DeprecationNoticeActionResponse)
}

/** Sealed data class for the response to a deprecation notice action. */
sealed class DeprecationNoticeActionResponse {
  /** Action for when the user presses the 'Close' button on a deprecation dialog. */
  object Close : DeprecationNoticeActionResponse()

  /** Action for when the user presses the 'Dismiss' button on a deprecation dialog. */
  data class Dismiss(
    val deprecationNoticeType: DeprecationNoticeType,
    val deprecatedVersion: Int,
  ) : DeprecationNoticeActionResponse()

  /** Action for when the user presses the 'Update' button on a deprecation dialog. */
  object Update : DeprecationNoticeActionResponse()
}
