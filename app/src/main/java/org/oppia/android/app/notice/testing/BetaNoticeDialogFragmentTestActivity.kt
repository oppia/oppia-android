package org.oppia.android.app.notice.testing

import android.os.Bundle
import org.oppia.android.app.notice.BetaNoticeClosedListener
import org.oppia.android.app.notice.BetaNoticeDialogFragment
import org.oppia.android.app.testing.activity.TestActivity

class BetaNoticeDialogFragmentTestActivity: TestActivity(), BetaNoticeClosedListener {
  // Must be initialized by the test.
  lateinit var mockCallbackListener: BetaNoticeClosedListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BetaNoticeDialogFragment.newInstance().showNow(supportFragmentManager, "beta_notice_dialog")
  }

  override fun onBetaNoticeOkayButtonClicked(permanentlyDismiss: Boolean) {
    mockCallbackListener.onBetaNoticeOkayButtonClicked(permanentlyDismiss)
  }
}
