package org.oppia.app.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.domain.exploration.ExplorationDataController
import org.oppia.util.networking.NetworkConnectionUtil
import javax.inject.Inject

/** Activity used in [ExplorationActivityTest] to get certain dependencies. */
class ExplorationInjectionActivity : InjectableAppCompatActivity() {
  @Inject lateinit var explorationDataController: ExplorationDataController
  @Inject lateinit var networkConnectionUtil: NetworkConnectionUtil

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
  }
}
