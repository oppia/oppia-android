package org.oppia.android.app.activity

import androidx.appcompat.app.AppCompatActivity

/** Factory for creating new [ActivityComponent]s. */
interface ActivityComponentFactory {
  /**
   * Returns a new [ActivityComponentImpl] for the specified activity. This should only be used by
   * [org.oppia.android.app.activity.InjectableAppCompatActivity].
   */
  fun createActivityComponent(activity: AppCompatActivity): ActivityComponent
}
