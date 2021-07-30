package org.oppia.android.instrumentation.player

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiCollection
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until.hasObject
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.oppia.android.instrumentation.EndToEndTestHelper.startOppiaFromScratch

/** Tests for Explorations. */
class ExplorationPlayerTest {
  private val OPPIA_PACKAGE = "org.oppia.android"
  private val TRANSITION_TIMEOUT = 5000L
  private lateinit var device: UiDevice

  @Before
  fun setUp() {
    // Initialize UiDevice instance
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.startOppiaFromScratch()
  }

  @Test
  fun testExploration_prototypeExploration_toolbarTitle_isDisplayedSuccessfully() {
    navigateToPrototypeExploration()
    device.wait(
      hasObject(
        By.res("$OPPIA_PACKAGE:id/exploration_toolbar_title")
      ),
      TRANSITION_TIMEOUT
    )
    val explorationTitle = device.findObject(By.res("$OPPIA_PACKAGE:id/exploration_toolbar_title"))
    assertEquals("Prototype Exploration", explorationTitle.text)
  }

  /** Navigates and opens the Prototype Exploration using the admin profile. */
  private fun navigateToPrototypeExploration() {
    val skip_button = device.findObject(By.res("$OPPIA_PACKAGE:id/skip_text_view"))
    skip_button?.let {
      it.click()
      device.wait(
        hasObject(By.res("$OPPIA_PACKAGE:id/get_started_button")),
        TRANSITION_TIMEOUT
      )
      device.findObject(By.res("$OPPIA_PACKAGE:id/get_started_button"))
        .click()
    }
    device.wait(
      hasObject(By.res("$OPPIA_PACKAGE:id/profile_select_text")),
      TRANSITION_TIMEOUT
    )
    val profiles = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    profiles.getChildByText(UiSelector().className("android.widget.LinearLayout"), "Admin").click()
    val recyclerview = UiScrollable(UiSelector().scrollable(true))
    recyclerview.setAsVerticalList()
    recyclerview.scrollTextIntoView("First Test Topic")
    val firstTestTopicText = device.findObject(UiSelector().text("First Test Topic"))
    firstTestTopicText.click()
    device.findObject(UiSelector().text("First Test Topic")).click()
    device.findObject(UiSelector().text("LESSONS")).click()
    device.findObject(UiSelector().text("First Story")).click()
    device.findObject(UiSelector().text("Chapter 1: Prototype Exploration")).click()
  }
}
