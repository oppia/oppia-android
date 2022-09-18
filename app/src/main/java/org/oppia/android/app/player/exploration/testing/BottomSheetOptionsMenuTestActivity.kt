package org.oppia.android.app.player.exploration.testing

import android.os.Bundle
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenu
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenuItemClickListener
import org.oppia.android.app.testing.activity.TestActivity

class BottomSheetOptionsMenuTestActivity : TestActivity(), BottomSheetOptionsMenuItemClickListener {

  lateinit var mockCallbacklistner: BottomSheetOptionsMenuItemClickListener

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    BottomSheetOptionsMenu().showNow(supportFragmentManager, "bottom_sheet_options_menu")
  }

  override fun handleOnOptionsItemSelected(itemId: Int) {
    mockCallbacklistner.handleOnOptionsItemSelected(itemId)
  }

}
