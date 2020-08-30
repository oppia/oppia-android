package org.oppia.app.screenshottesting

import android.os.Environment
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.onboarding.OnboardingActivity
import org.oppia.testing.screenshots.OUTPUT_FOLDER_NAME
import org.oppia.testing.screenshots.ScreenshotManager
import java.io.File

@RunWith(AndroidJUnit4::class)
class OnboardingActivityScreenshotTest {

  private val screenshotManager = ScreenshotManager()

  @Before
  fun setup() {
    createOutputFolder()
  }

  @Test
  fun testOnboardingActivity_takeScreenshot() {
    launch(OnboardingActivity::class.java).use {
      it.onActivity { activity ->
        screenshotManager.takeScreenshot(activity)
      }
    }
  }

  private fun createOutputFolder() {
    val outputFolder =
      File("${Environment.getExternalStorageDirectory().path}/$OUTPUT_FOLDER_NAME")
    if (!outputFolder.exists()) {
      outputFolder.mkdir()
    }
  }
}
