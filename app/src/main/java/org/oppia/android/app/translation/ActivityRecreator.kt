package org.oppia.android.app.translation

import androidx.appcompat.app.AppCompatActivity

/**
 * Utility for recreating an activity. This should be used in all cases where activity recreation is
 * needed if that codepath can be run in tests (since certain testing frameworks can have issues
 * with activity recreation).
 *
 * This can be injected at the application scope and below.
 */
interface ActivityRecreator {
  /** Recreates the specified activity (see [android.app.Activity.recreate]. */
  fun recreate(activity: AppCompatActivity)
}
