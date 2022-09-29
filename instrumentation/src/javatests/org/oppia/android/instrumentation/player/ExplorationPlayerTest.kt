package org.oppia.android.instrumentation.player

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.R
import org.oppia.android.instrumentation.testing.SearchableInteractable
import org.oppia.android.instrumentation.testing.launchOppia

/** End-to-end tests for explorations. */
// FunctionName: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
class ExplorationPlayerTest {
  private lateinit var device: UiDevice

  @Before
  fun setUp() {
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
  }

  @Test
  fun testPlayExploration_prototypeExploration_playedFullyThrough_finishesSuccessfully() {
    device.launchOppia {
      navigateToPrototypeExploration()

      // Play through all interactions.
      playContinueInteraction()
      playFractionInputInteraction()
      playMultipleChoiceInteraction1()
      playMultipleChoiceInteraction2()
      playItemSelectionInteraction()
      playNumericInputInteraction()
      playRatioInputInteraction()
      playTextInputInteraction()
      playDragAndDropInteraction()
      playDragDropMergeInteraction()
      playEndExplorationInteraction()

      // Ensure the topic is completed by verifying that a completed tick is visible.
      // TODO: Fix this check.
      withChildByText("Chapter 1: Prototype Exploration") {
        withChildById(R.id.chapter_completed_tick) {
          waitToAppear(scrollTo = true)
        }
      }
    }
  }

  // TODO(#3697): Update e2e tests when backend support is introduced.
  @Test
  @Ignore("Need backend connection support to test the ImageRegionSelectionInteraction")
  fun testPlayExploration_imageRegionInteractionExp_playedFullyThrough_finishesSuccessfully() {
    device.launchOppia {
      navigateToImageRegionSelectionInteraction()

      // Image Region Selection Interaction.
      withChildById(R.id.image_click_interaction_image_view) {
        // TODO(#3712): Use content description to fetch the image region.
        // TODO: Reintroduce something like this.
        // imageSelectionView.children.get(2).click()
        clickChildWithId(R.id.default_selected_region)
      }
      clickChildWithText("Submit")
      clickChildWithText("Continue")

      // End Exploration.
      playEndExplorationInteraction()

      // Ensure the topic is completed by verifying that a completed tick is visible.
      withChildByText("Chapter 2: Image Region Selection Exploration") {
        withChildById(R.id.chapter_completed_tick) {
          waitToAppear(scrollTo = true)
        }
      }
    }
  }

  /** Navigates and opens the Prototype Exploration using the admin profile. */
  private fun SearchableInteractable.navigateToPrototypeExploration() {
    clickChildWithId(R.id.skip_text_view)
    clickChildWithId(R.id.get_started_button)
    clickChildWithText("Admin")
    clickChildWithText("First Test Topic")
    clickChildWithText("Lessons")
    clickChildWithText("First Story")
    clickChildWithText("Chapter 1: Prototype Exploration")
  }

  private fun SearchableInteractable.completePrototypeExploration() {
    playContinueInteraction()
    playFractionInputInteraction()
    playMultipleChoiceInteraction1()
    playMultipleChoiceInteraction2()
    playItemSelectionInteraction()
    playNumericInputInteraction()
    playRatioInputInteraction()
    playTextInputInteraction()
    playDragAndDropInteraction()
    playDragDropMergeInteraction()
    playEndExplorationInteraction()
  }

  private fun SearchableInteractable.playContinueInteraction() {
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playFractionInputInteraction() {
    withChildById(R.id.fraction_input_interaction_view) { setText("1/2") }
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playMultipleChoiceInteraction1() {
    clickChildWithText("Eagle")
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playMultipleChoiceInteraction2() {
    clickChildWithText("Green")
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playItemSelectionInteraction() {
    clickChildWithText("Red")
    clickChildWithText("Green")
    clickChildWithText("Blue")
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playNumericInputInteraction() {
    withChildById(R.id.numeric_input_interaction_view) { setText("121") }
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playRatioInputInteraction() {
    withChildById(R.id.ratio_input_interaction_view) { setText("4:5") }
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playTextInputInteraction() {
    withChildById(R.id.text_input_interaction_view) { setText("Finnish") }
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playDragAndDropInteraction() {
    clickChildWithContentDescription("Move item down to 2")
    clickChildWithContentDescription("Move item down to 3")
    clickChildWithContentDescription("Move item down to 4")
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playDragDropMergeInteraction() {
    clickChildWithContentDescription("Link to item 2")
    clickChildWithContentDescription("Move item down to 3")
    clickChildWithText("Submit")
    clickChildWithText("Continue")
  }

  private fun SearchableInteractable.playEndExplorationInteraction() {
    // This requires more investigating, but for some reason the final 'Return to Topic' button is
    // sometimes simply not clicked (perhaps due to the confetti? It's not actually clear).
    // TODO: Determine if this is actually needed.
    Thread.sleep(5_000)
    clickChildWithId(R.id.return_to_topic_button)
  }

  /** Navigates and opens the Image Region Selection Exploration using the admin profile. */
  private fun SearchableInteractable.navigateToImageRegionSelectionInteraction() {
    navigateToPrototypeExploration()
    completePrototypeExploration()
    clickChildWithText("Chapter 2: Image Region Selection Exploration")
  }
}
