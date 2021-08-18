package org.oppia.android.instrumentation.player

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.findObjectByRes
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.findObjectByText
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.scrollRecyclerViewTextIntoView
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.startOppiaFromScratch
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.waitForRes

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
    device.findObjectByRes("skip_text_view")?.click()
    device.findObjectByRes("get_started_button")?.click()
    device.waitForRes("profile_select_text")
    device.findObjectByText("Admin")?.click()
    scrollRecyclerViewTextIntoView("First Test Topic")
    device.findObjectByText("First Test Topic")?.click()
    device.findObjectByText("LESSONS")?.click()
    device.findObjectByText("First Story")?.click()
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration")
    device.findObjectByText("Chapter 1: Prototype Exploration")?.click()
  }
}
