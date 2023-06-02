package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import javax.inject.Inject

/** The activity for testing [HomeFragment]. */
class HomeTestActivity : InjectableAutoLocalizedAppCompatActivity() {

  @Inject
  lateinit var homeTestActivityPresenter: HomeTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    homeTestActivityPresenter.handleOnCreate()
  }
}
