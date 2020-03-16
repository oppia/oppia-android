package org.oppia.app.walkthrough

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Activity that contains the walkthrough flow for users. */
class WalkthroughActivity : InjectableAppCompatActivity(), WalkthroughFragmentChangeListener {
  @Inject lateinit var walkthroughActivityPresenter: WalkthroughActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    walkthroughActivityPresenter.handleOnCreate()
  }

  override fun currentPage(walkthroughPage: Int) {
    walkthroughActivityPresenter.changePage(walkthroughPage)
  }

  override fun onBackPressed() {
    walkthroughActivityPresenter.handleSystemBack()
  }
}
