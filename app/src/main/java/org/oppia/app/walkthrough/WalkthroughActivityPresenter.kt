package org.oppia.app.walkthrough

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The presenter for [WalkthroughActivity]. */
@ActivityScope
class WalkthroughActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {

  fun handleOnCreate() {
    activity.setContentView(R.layout.walkthrough_activity)
    if (getWalkthroughFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.walkthrough_fragment_placeholder,
        WalkthroughFragment()
      ).commitNow()
    }
  }

  private fun getWalkthroughFragment(): WalkthroughFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.walkthrough_fragment_placeholder) as WalkthroughFragment?
  }
}
