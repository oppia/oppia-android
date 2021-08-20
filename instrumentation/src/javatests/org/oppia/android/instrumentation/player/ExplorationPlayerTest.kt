package org.oppia.android.instrumentation.player

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Ignore
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
  fun testPlayExploration_prototypeExploration_playedFullyThrough_finishesSuccessfully() {
    navigateToPrototypeExploration()

    // Play through all interactions.
    playContinueInteraction()
    playFractionInputInteraction()
    playMultipleChoiceIntearction1()
    playMultipleChoiceIntearction2()
    playItemSelectionInteraction()
    playNumericInputInteraction()
    playRatioInputInteraction()
    playTextInputInteraction()
    playDragAndDropInteraction()
    playDragDropMergeInteraction()
    playEndExplorationInteraction()

    // Assert Topic Completed.
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration")
    val chapterCompletedTick = device.findObjectByText(
      "Chapter 1: Prototype Exploration"
    ).parent.findObjectByRes("chapter_completed_tick")
    assertThat(chapterCompletedTick).isNotNull()
  }

  // TODO(#3697): Update e2e tests when backend support is introduced.
  @Test
  @Ignore("Need backend connection support to test the ImageRegionSelectionInteraction")
  fun testPlayExploration_imageRegionInteractionExp_playedFullyThrough_finishesSuccessfully() {
    navigateToImageRegionSelectionInteraction()

    // Image Region Selection Interaction.
    val imageSelectionView = device.findObjectByRes("interaction_container_frame_layout")
    device.waitForRes("image_click_interaction_image_view")
    // TODO(#3712): Use content description to fetch the image region.
    imageSelectionView.children.get(2).click()
    device.findObjectByText("SUBMIT").click()
    device.findObjectByText("CONTINUE").click()

    // End Exploration.
    playEndExplorationInteraction()

    // Assert Topic Completed.
    scrollRecyclerViewTextIntoView("Chapter 2: Image Region Selection Exploration")
    val chapterCompletedTick = device.findObjectByText(
      "Chapter 2: Image Region Selection Exploration"
    ).parent.findObjectByRes("chapter_completed_tick")
    assertThat(chapterCompletedTick).isNotNull()
  }

  /** Navigates and opens the Prototype Exploration using the admin profile. */
  private fun navigateToPrototypeExploration() {
    device.findObjectByRes("skip_text_view").click()
    device.findObjectByRes("get_started_button").click()
    device.waitForRes("profile_select_text")
    device.findObjectByText("Admin").click()
    scrollRecyclerViewTextIntoView("First Test Topic")
    device.findObjectByText("First Test Topic").click()
    device.findObjectByText("LESSONS").click()
    device.findObjectByText("First Story").click()
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration")
    device.findObjectByText("Chapter 1: Prototype Exploration").click()
  }

  private fun completePrototypeExploration() {
    playContinueInteraction()
    playFractionInputInteraction()
    playMultipleChoiceIntearction1()
    playMultipleChoiceIntearction2()
    playItemSelectionInteraction()
    playNumericInputInteraction()
    playRatioInputInteraction()
    playTextInputInteraction()
    playDragAndDropInteraction()
    playDragDropMergeInteraction()
    playEndExplorationInteraction()
  }

  private fun playContinueInteraction() {
    device.findObjectByText("CONTINUE").click()
  }

  private fun playFractionInputInteraction() {
    device.findObjectByRes("fraction_input_interaction_view").text = "1/2"
    device.findObjectByText("SUBMIT").click()
    playContinueInteraction()
  }

  private fun playMultipleChoiceIntearction1() {
    device.findObjectByText("Eagle").click()
    playContinueInteraction()
  }

  private fun playMultipleChoiceIntearction2() {
    device.findObjectByText("Green").click()
    playContinueInteraction()
  }

  private fun playItemSelectionInteraction() {
    device.findObjectByText("Red").click()
    device.findObjectByText("Green").click()
    device.findObjectByText("Blue").click()
    device.findObjectByText("SUBMIT").click()
    playContinueInteraction()
  }

  private fun playNumericInputInteraction() {
    device.findObjectByRes("numeric_input_interaction_view").text = "121"
    device.findObjectByText("SUBMIT").click()
    playContinueInteraction()
  }

  private fun playRatioInputInteraction() {
    device.findObjectByRes("ratio_input_interaction_view").text = "4:5"
    device.findObjectByText("SUBMIT").click()
    playContinueInteraction()
  }

  private fun playTextInputInteraction() {
    device.findObjectByRes("text_input_interaction_view").text = "Finnish"
    device.findObjectByText("SUBMIT").click()
    playContinueInteraction()
  }

  private fun playDragAndDropInteraction() {
    device.findObjectByDesc("Move item down to 2").click()
    device.findObjectByDesc("Move item down to 3").click()
    device.findObjectByDesc("Move item down to 4").click()
    device.findObjectByText("SUBMIT").click()
    playContinueInteraction()
  }

  private fun playDragDropMergeInteraction() {
    device.findObjectByDesc("Link to item 2").click()
    device.findObjectByDesc("Move item down to 3").click()
    device.findObjectByText("SUBMIT").click()
    playContinueInteraction()
  }

  private fun playEndExplorationInteraction() {
    device.findObjectByText("RETURN TO TOPIC").click()
  }

  /** Navigates and opens the Image Region Selection Exploration using the admin profile. */
  private fun navigateToImageRegionSelectionInteraction() {
    device.findObjectByRes("skip_text_view").click()
    device.findObjectByRes("get_started_button").click()
    device.waitForRes("profile_select_text")
    device.findObjectByText("Admin").click()
    scrollRecyclerViewTextIntoView("First Test Topic")
    device.findObjectByText("First Test Topic").click()
    device.findObjectByText("LESSONS").click()
    device.findObjectByText("First Story").click()
    scrollRecyclerViewTextIntoView("Chapter 1: Prototype Exploration")
    device.findObjectByText("Chapter 1: Prototype Exploration").click()
    completePrototypeExploration()
    scrollRecyclerViewTextIntoView("Chapter 2: Image Region Selection Exploration")
    device.findObjectByText("Chapter 2: Image Region Selection Exploration").click()
  }
}
