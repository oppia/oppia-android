package org.oppia.app.screenshottesting

import android.content.Intent
import androidx.test.rule.ActivityTestRule
import com.facebook.testing.screenshot.Screenshot
import org.junit.Rule
import org.junit.Test
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.home.HomeActivity

class ScreenshotTestingTests {

  @get:Rule
  var activityTestRule = ActivityTestRule(HomeActivity::class.java)

  @Test
  fun testHomeActivity() {
    val intent = Intent()
    intent.putExtra(KEY_NAVIGATION_PROFILE_ID, 0)
    val activity = activityTestRule.launchActivity(intent)
    Screenshot.snapActivity(activity).record()
  }
}
