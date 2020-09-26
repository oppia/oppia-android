package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The activity for testing [HomeFragment]. */
class HomeTestActivity : InjectableAppCompatActivity() {

  @Inject
  lateinit var homeTestActivityPresenter: HomeTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    homeTestActivityPresenter.handleOnCreate()
  }
}
