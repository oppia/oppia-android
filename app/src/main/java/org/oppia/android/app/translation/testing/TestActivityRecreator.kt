package org.oppia.android.app.translation.testing

import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import org.oppia.android.app.translation.ActivityRecreator

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
class TestActivityRecreator @Inject constructor(): ActivityRecreator {
  override fun recreate(activity: AppCompatActivity) {
    // Do nothing since activity recreation doesn't work correctly in Robolectric.
  }
}
