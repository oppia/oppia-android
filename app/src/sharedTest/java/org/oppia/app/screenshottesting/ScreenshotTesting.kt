package org.oppia.app.screenshottesting

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.app.screenshots.ScreenshotManager

@RunWith(AndroidJUnit4::class)
class ScreenshotTesting {

  @get:Rule val permissionRule: GrantPermissionRule =
    GrantPermissionRule.grant(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
  private val screenshotManager = ScreenshotManager()

  @Test
  fun testOnboardingActivity_takeScreenshot() {
    ActivityScenario.launch(OnboardingActivity::class.java).use {
      it.onActivity { activity ->
        screenshotManager.takeScreenshot(activity)
      }
    }
  }
}
