package org.oppia.android.instrumentation.player

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.findObjectByDesc
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
  fun testProtoTypeExploration_answerAllInteractionsAndCompleteExploration() {
    navigateToPrototypeExploration()

    // Exploration description.
    device.findObjectByText("CONTINUE")?.click()

    // Fraction Input Interaction.
    device.findObjectByRes("fraction_input_interaction_view")?.text = "1/2"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Multiple Choice Interaction - 1.
    device.findObjectByText("Eagle")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Multiple Choice Interaction - 2.
    device.findObjectByText("Green")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Item Selection Interaction.
    device.findObjectByText("Red")?.click()
    device.findObjectByText("Green")?.click()
    device.findObjectByText("Blue")?.click()
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Numeric Input Interaction.
    device.findObjectByRes("numeric_input_interaction_view")?.text = "121"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Ratio Input Interaction.
    device.findObjectByRes("ratio_input_interaction_view")?.text = "4:5"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Text Input Interaction.
    device.findObjectByRes("text_input_interaction_view")?.text = "Finnish"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Drag And Drop Interaction.
    device.findObjectByDesc("Move item down to 2")?.click()
    device.findObjectByDesc("Move item down to 3")?.click()
    device.findObjectByDesc("Move item down to 4")?.click()
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // Drag Drop Merge Interaction.
    device.findObjectByDesc("Link to item 2")?.click()
    device.findObjectByDesc("Move item down to 3")?.click()
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // End Exploration.
    device.findObjectByText("RETURN TO TOPIC")?.click()

    // Assert Topic Completed.
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration")
    val chapterCompletedTick = device.findObjectByText(
      "Chapter 1: Prototype Exploration"
    )?.parent?.children?.get(2)
    assertThat(chapterCompletedTick).isNotNull()
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
