package org.oppia.app.application

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.multidex.MultiDexApplication
import org.oppia.app.activity.ActivityComponent

/** The root [Application] of the Oppia app. */
class OppiaApplication : MultiDexApplication() {
  /** The root [ApplicationComponent]. */
  private val component: ApplicationComponent by lazy {
    DaggerApplicationComponent.builder()
      .setApplication(this)
      .build()
  }

  /**
   * Returns a new [ActivityComponent] for the specified activity. This should only be used by
   * [org.oppia.app.activity.InjectableAppCompatActivity].
   */
  fun createActivityComponent(activity: AppCompatActivity): ActivityComponent {
    return component.getActivityComponentBuilderProvider().get().setActivity(activity).build()
  }
}
