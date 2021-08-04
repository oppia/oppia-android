package org.oppia.android.instrumentation.player

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.testing.uiautomator.EndToEndTestHelper.findObjectByRes
import org.oppia.android.testing.uiautomator.EndToEndTestHelper.scrollRecyclerViewTextIntoView
import org.oppia.android.testing.uiautomator.EndToEndTestHelper.startOppiaFromScratch
import org.oppia.android.testing.uiautomator.EndToEndTestHelper.waitForRes

/** Tests for Explorations. */
class ExplorationPlayerTest {
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
    assertThat(device.findObjectByRes("exploration_toolbar_title")).isNotNull()
  }

  /** Navigates and opens the Prototype Exploration using the admin profile. */
  private fun navigateToPrototypeExploration() {
    val skip_button = device.findObjectByRes("skip_text_view")
    skip_button?.let {
      it.click()
      device.waitForRes("get_started_button")
      device.findObjectByRes("get_started_button")?.click()
    }
    device.waitForRes("profile_select_text")
    device.findObject(By.text("Admin")).click()
    scrollRecyclerViewTextIntoView("First Test Topic")
    device.findObject(By.text("First Test Topic")).click()
    device.waitForRes("topic_toolbar_title")
    device.findObject(By.text("LESSONS")).click()
    device.findObject(By.text("First Story")).click()
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration")
    device.findObject(By.text("Chapter 1: Prototype Exploration")).click()
  }
}
