package org.oppia.android.app.testing

import android.os.Bundle
import javax.inject.Inject
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.model.ProfileId
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenu
import org.oppia.android.app.player.exploration.BottomSheetOptionsMenuItemClickListener
import org.oppia.android.app.player.exploration.ExplorationActivityPresenter
import org.oppia.android.app.testing.activity.TestActivity

class BottomSheetOptionsMenuTestActivity: InjectableAppCompatActivity(), BottomSheetOptionsMenuItemClickListener {

  @Inject
  lateinit var bottomSheetOptionsMenuTestActivityPresenter: BottomSheetOptionsMenuTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    bottomSheetOptionsMenuTestActivityPresenter.handleOnCreate()
  }

  override fun handleOnOptionsItemSelected(itemId: Int) {
    bottomSheetOptionsMenuTestActivityPresenter.handleOnOptionsItemSelected(itemId)
  }

}