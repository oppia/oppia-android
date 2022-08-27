package org.oppia.android.app.notice.testing

import android.os.Bundle
import org.oppia.android.app.notice.BetaNoticeClosedListener
import org.oppia.android.app.notice.BetaNoticeDialogFragment
import org.oppia.android.app.testing.activity.TestActivity

/** [TestActivity] for setting up a test environment for testing the beta notice dialog. */
class BetaNoticeDialogFragmentTestActivity : TestActivity(), BetaNoticeClosedListener {
  /**
   * [BetaNoticeClosedListener] that must be initialized by the test, and is presumed to be a
   * Mockito mock (though this is not, strictly speaking, required).
   *
   * This listener will be used as the callback for the dialog in response to UI operations.
   */
  lateinit var mockCallbackListener: BetaNoticeClosedListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BetaNoticeDialogFragment.newInstance().showNow(supportFragmentManager, "beta_notice_dialog")
  }

  override fun onBetaNoticeOkayButtonClicked(permanentlyDismiss: Boolean) {
    mockCallbackListener.onBetaNoticeOkayButtonClicked(permanentlyDismiss)
  }
}
