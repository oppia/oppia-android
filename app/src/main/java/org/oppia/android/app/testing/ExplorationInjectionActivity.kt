package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
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
    (activityComponent as Injector).inject(this)
  }

  /** Dagger injector for [ExplorationInjectionActivity]. */
  interface Injector {
    /** Injects dependencies into the [activity]. */
    fun inject(activity: ExplorationInjectionActivity)
  }

  companion object {
    /** Returns an [Intent] for opening new instances of [ExplorationInjectionActivity]. */
    fun createIntent(context: Context): Intent =
      Intent(context, ExplorationInjectionActivity::class.java)
  }
}
