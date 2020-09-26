package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.domain.profile.ProfileManagementController
import org.oppia.android.util.system.OppiaClock
import javax.inject.Inject

/** Activity used in [HomeActivityTest] to get certain dependencies. */
class HomeInjectionActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var profileManagementController: ProfileManagementController

  @Inject
  lateinit var oppiaClock: OppiaClock

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
  }
}
