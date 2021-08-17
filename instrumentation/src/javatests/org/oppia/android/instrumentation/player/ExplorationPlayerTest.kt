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

    // Play through all interactions in PrototypeExploration
    playExplorationDescription()
    playFractionInputInteraction()
    playMultipleChoiceIntearction1()
    playMultipleChoiceIntearction2()
    playItemSelectionInteraction()
    playNumericInputInteraction()
    playRatioInputInteraction()
    playTextInputInteraction()
    playDragAndDropInteraction()
    playDragDropMergeInteraction()
    endExploration()

    // Assert Topic Completed.
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration")
    val chapterCompletedTick = device.findObjectByText(
      "Chapter 1: Prototype Exploration"
    )?.parent?.children?.get(2)
    assertThat(chapterCompletedTick).isNotNull()
  }

  @Ignore
  @Test
  fun testImageRegionSelectionInteraction_answerAllInteractionsAndCompleteExploration() {
    navigateToImageRegionSelectionInteraction()

    // / Image Region Selection Interaction.
    val imageSelectionView = device.findObjectByRes("interaction_container_frame_layout")
    device.waitForRes("image_click_interaction_image_view")
    imageSelectionView?.children?.get(2)?.click()
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()

    // End Exploration.
    device.findObjectByText("RETURN TO TOPIC")?.click()

    // Assert Topic Completed.
    scrollRecyclerViewTextIntoView("Chapter 2: Image Region Selection Exploration")
    val chapterCompletedTick = device.findObjectByText(
      "Chapter 2: Image Region Selection Exploration"
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

  /** Continue Description of ProtoTypeExploration. */
  private fun playExplorationDescription() {
    device.findObjectByText("CONTINUE")?.click()
  }

  /** Play FractionInputInteraction for PrototypeExporation. */
  private fun playFractionInputInteraction() {
    device.findObjectByRes("fraction_input_interaction_view")?.text = "1/2"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }
  /** Play first MultipleChoiceInteraction for PrototypeExporation. */
  private fun playMultipleChoiceIntearction1() {
    device.findObjectByText("Eagle")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }
  /** Play second MultipleChoiceInteraction for PrototypeExporation. */
  private fun playMultipleChoiceIntearction2() {
    device.findObjectByText("Green")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }

  /** Play second ItemSelectionInteraction for PrototypeExporation. */
  private fun playItemSelectionInteraction() {
    device.findObjectByText("Red")?.click()
    device.findObjectByText("Green")?.click()
    device.findObjectByText("Blue")?.click()
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }

  /** Play second NumericInputInteraction for PrototypeExporation. */
  private fun playNumericInputInteraction() {
    device.findObjectByRes("numeric_input_interaction_view")?.text = "121"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }

  /** Play second RatioInputInteraction for PrototypeExporation. */
  private fun playRatioInputInteraction() {
    device.findObjectByRes("ratio_input_interaction_view")?.text = "4:5"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }

  /** Play second TextInputInteraction for PrototypeExporation. */
  private fun playTextInputInteraction() {
    device.findObjectByRes("text_input_interaction_view")?.text = "Finnish"
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }

  /** Play second DragAndDropInteraction for PrototypeExporation. */
  private fun playDragAndDropInteraction() {
    device.findObjectByDesc("Move item down to 2")?.click()
    device.findObjectByDesc("Move item down to 3")?.click()
    device.findObjectByDesc("Move item down to 4")?.click()
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }

  /** Play second DragDropMergeInteraction for PrototypeExporation. */
  private fun playDragDropMergeInteraction() {
    device.findObjectByDesc("Link to item 2")?.click()
    device.findObjectByDesc("Move item down to 3")?.click()
    device.findObjectByText("SUBMIT")?.click()
    device.findObjectByText("CONTINUE")?.click()
  }

  /** End exploration for PrototypeExploration. */
  private fun endExploration() {
    device.findObjectByText("RETURN TO TOPIC")?.click()
  }

  /** Navigates and opens the Image Region Selection Exploration using the admin profile. */
  private fun navigateToImageRegionSelectionInteraction() {
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
    completePrototypeExploration()
    scrollRecyclerViewTextIntoView("Chapter 2: Image Region Selection Exploration")
    device.findObjectByText("Chapter 2: Image Region Selection Exploration")?.click()
  }
}
