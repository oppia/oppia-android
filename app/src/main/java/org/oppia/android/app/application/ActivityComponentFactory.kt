package org.oppia.app.application

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.activity.ActivityComponent

interface ActivityComponentFactory {
  /**
   * Returns a new [ActivityComponent] for the specified activity. This should only be used by
   * [org.oppia.app.activity.InjectableAppCompatActivity].
   */
  fun createActivityComponent(activity: AppCompatActivity): ActivityComponent
}
