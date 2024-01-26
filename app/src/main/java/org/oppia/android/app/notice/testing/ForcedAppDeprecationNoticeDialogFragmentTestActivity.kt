package org.oppia.android.app.notice.testing

import android.os.Bundle
import org.oppia.android.app.notice.DeprecationNoticeActionListener
import org.oppia.android.app.notice.DeprecationNoticeActionResponse
import org.oppia.android.app.notice.ForcedAppDeprecationNoticeDialogFragment
import org.oppia.android.app.testing.activity.TestActivity

/** [TestActivity] for setting up a test environment for testing the beta notice dialog. */
class ForcedAppDeprecationNoticeDialogFragmentTestActivity :
  TestActivity(),
  DeprecationNoticeActionListener {
  /**
   * [DeprecationNoticeActionListener] that must be initialized by the test, and is presumed to be a
   * Mockito mock (though this is not, strictly speaking, required).
   *
   * This listener will be used as the callback for the dialog in response to UI operations.
   */
  lateinit var mockCallbackListener: DeprecationNoticeActionListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    ForcedAppDeprecationNoticeDialogFragment.newInstance()
      .showNow(supportFragmentManager, "forced_app_deprecation_dialog")
  }

  override fun onActionButtonClicked(noticeActionResponse: DeprecationNoticeActionResponse) {
    mockCallbackListener.onActionButtonClicked(noticeActionResponse)
  }
}
