package org.oppia.app.screenshottesting

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.testing.ConceptCardFragmentTestActivity
import org.oppia.testing.screenshots.ScreenshotManager
import java.io.File

/** A test class that is used to take screenshots of the [ConceptCardFragmentTestActivity].*/
@RunWith(AndroidJUnit4::class)
class ConceptCardFragmentScreenshotTest {

  private val screenshotManager = ScreenshotManager()

  @Before
  fun setup() {
    createScreenshotTestResultDirectory()
  }

  @Test
  fun testConceptCardFragment_takeScreenshot() {
    ActivityScenario.launch(ConceptCardFragmentTestActivity::class.java).use {
      it.onActivity { activity ->
        screenshotManager.takeScreenshot(activity)
        val fileName = "org.oppia.app.testing.ConceptCardFragmentTestActivity.png"
        val screenshotFile = File("${ScreenshotManager.getOutputPath()}/$fileName")
        Truth.assertThat(screenshotFile.exists()).isTrue()
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
