package org.oppia.android.app.translation.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.translation.ActivityRecreator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Test version of [ActivityRecreator] that no-ops activity recreation.
 *
 * This should always be used by default in Robolectric tests that might trigger an activity
 * recreation since, without this utility, Robolectric tests may fail or hang. Note that this
 * intentionally removes recreation flows which can result in disparity with production. Care
 * should be taken to ensure that activity recreation occurs when expected rather than verifying
 * that recreation itself works (since that's ultimately testing Robolectric in this case).
 * Recreation flows can also be directly tested using ActivityScenario if needed.
 */
@Singleton
class TestActivityRecreator @Inject constructor() : ActivityRecreator {
  private var recreationCount: Int = 0

  override fun recreate(activity: AppCompatActivity) {
    // Do nothing since activity recreation doesn't work correctly in Robolectric.
    ++recreationCount
  }

  /** Returns the number of times [recreate] was called for this recreator. */
  fun getRecreateCount(): Int = recreationCount
}
