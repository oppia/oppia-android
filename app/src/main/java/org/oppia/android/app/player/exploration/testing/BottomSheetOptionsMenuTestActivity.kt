package org.oppia.android.app.player.exploration.testing

import android.os.Bundle
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenu
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenuItemClickListener
import org.oppia.android.app.testing.activity.TestActivity

/** [TestActivity] for setting up a test environment for testing the BottomSheetOptionsMenu. */
class BottomSheetOptionsMenuTestActivity : TestActivity(), BottomSheetOptionsMenuItemClickListener {
  /**
   * [BottomSheetOptionsMenuItemClickListener] that must be initialized by the test,
   * and is presumed to be a Mockito mock (though this is not, strictly speaking, required).
   *
   * This listener will be used as the callback for the dialog in response to UI operations.
   */
  lateinit var mockCallbacklistner: BottomSheetOptionsMenuItemClickListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BottomSheetOptionsMenu().showNow(supportFragmentManager, "bottom_sheet_options_menu")
  }

  override fun handleOnOptionsItemSelected(itemId: Int) {
    mockCallbacklistner.handleOnOptionsItemSelected(itemId)
  }
}
