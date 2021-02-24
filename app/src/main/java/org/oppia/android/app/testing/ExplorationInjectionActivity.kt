package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.domain.exploration.ExplorationDataController
import org.oppia.android.util.networking.NetworkConnectionUtil
import javax.inject.Inject

/** Activity used in [ExplorationActivityTest] to get certain dependencies. */
class ExplorationInjectionActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var explorationDataController: ExplorationDataController

  @Inject
  lateinit var networkConnectionUtil: NetworkConnectionUtil

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
  }
}
