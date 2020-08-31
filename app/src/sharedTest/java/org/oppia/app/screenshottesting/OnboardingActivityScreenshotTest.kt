package org.oppia.app.screenshottesting

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.testing.screenshots.ScreenshotManager
import java.io.File

/** A test class that is used to take screenshots of the [OnboardingActivity]. */
@RunWith(AndroidJUnit4::class)
class OnboardingActivityScreenshotTest {

  private val screenshotManager = ScreenshotManager()

  @Before
  fun setup() {
    createScreenshotTestResultDirectory()
  }

  @Test
  fun testOnboardingActivity_takeScreenshot() {
    launch(OnboardingActivity::class.java).use {
      it.onActivity { activity ->
        screenshotManager.takeScreenshot(activity)
        val fileName = "org.oppia.app.onboarding.OnboardingActivity.png"
        val screenshotFile = File("${ScreenshotManager.getOutputPath()}/$fileName")
        assertThat(screenshotFile.exists()).isTrue()
      }
    }
  }

  private fun createScreenshotTestResultDirectory() {
    val outputFolder = File(ScreenshotManager.getOutputPath())
    if (!outputFolder.exists()) {
      outputFolder.mkdir()
    }
  }
}
