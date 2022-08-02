package org.oppia.android.app.notice.testing

import android.os.Bundle
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeClosedListener
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeDialogFragment
import org.oppia.android.app.testing.activity.TestActivity

class GeneralAvailabilityUpgradeNoticeDialogFragmentTestActivity
  : TestActivity(), GeneralAvailabilityUpgradeNoticeClosedListener {
  // Must be initialized by the test.
  lateinit var mockCallbackListener: GeneralAvailabilityUpgradeNoticeClosedListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    GeneralAvailabilityUpgradeNoticeDialogFragment.newInstance()
      .showNow(supportFragmentManager, "ga_upgrade_notice_dialog")
  }

  override fun onGaUpgradeNoticeOkayButtonClicked(permanentlyDismiss: Boolean) {
    mockCallbackListener.onGaUpgradeNoticeOkayButtonClicked(permanentlyDismiss)
  }
}
