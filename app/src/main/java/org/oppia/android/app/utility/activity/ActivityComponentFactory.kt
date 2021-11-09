package org.oppia.android.app.utility.activity

import androidx.appcompat.app.AppCompatActivity

/** Factory for creating new [ActivityComponent]s. */
interface ActivityComponentFactory {
  /**
   * Returns a new [ActivityComponentImpl] for the specified activity. This should only be used by
   * [org.oppia.android.app.utility.activity.InjectableAppCompatActivity].
   */
  fun createActivityComponent(activity: AppCompatActivity): ActivityComponent
}
