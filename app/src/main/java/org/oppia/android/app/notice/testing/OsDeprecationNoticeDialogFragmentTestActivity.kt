package org.oppia.android.app.notice.testing

import android.os.Bundle
import org.oppia.android.app.notice.DeprecationNoticeActionListener
import org.oppia.android.app.notice.OsDeprecationNoticeDialogFragment
import org.oppia.android.app.splash.DeprecationNoticeActionType
import org.oppia.android.app.testing.activity.TestActivity

/** [TestActivity] for setting up a test environment for testing the beta notice dialog. */
class OsDeprecationNoticeDialogFragmentTestActivity :
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
    OsDeprecationNoticeDialogFragment.newInstance()
      .showNow(supportFragmentManager, "os_deprecation_dialog")
  }

  override fun onActionButtonClicked(noticeType: DeprecationNoticeActionType) {
    mockCallbackListener.onActionButtonClicked(noticeType)
  }
}
