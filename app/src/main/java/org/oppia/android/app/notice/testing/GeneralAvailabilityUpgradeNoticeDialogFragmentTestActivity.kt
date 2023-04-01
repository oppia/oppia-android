package org.oppia.android.app.notice.testing

import android.os.Bundle
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeClosedListener
import org.oppia.android.app.notice.GeneralAvailabilityUpgradeNoticeDialogFragment
import org.oppia.android.app.testing.activity.TestActivity

/** [TestActivity] for setting up a test environment for testing the GA upgrade notice dialog. */
class GeneralAvailabilityUpgradeNoticeDialogFragmentTestActivity :
  TestActivity(), GeneralAvailabilityUpgradeNoticeClosedListener {
  /**
   * [GeneralAvailabilityUpgradeNoticeClosedListener] that must be initialized by the test, and is
   * presumed to be a Mockito mock (though this is not, strictly speaking, required).
   *
   * This listener will be used as the callback for the dialog in response to UI operations.
   */
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
