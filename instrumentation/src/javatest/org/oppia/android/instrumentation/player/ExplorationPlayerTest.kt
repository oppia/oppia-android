package org.oppia.android.instrumentation.player

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiCollection
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.oppia.android.instrumentation.EndToEndTestHelper.scrollToText
import org.oppia.android.instrumentation.EndToEndTestHelper.startOppiaFromScratch
import org.oppia.android.instrumentation.EndToEndTestHelper.waitForRes

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
    val explorationTitle = device.findObject(By.res("$OPPIA_PACKAGE:id/exploration_toolbar_title"))
    assertEquals("Prototype Exploration", explorationTitle.text)
  }

  @Test
  fun testProtoTypeExploration_answerAllInteractionsAndCompleteExploration() {
    navigateToPrototypeExploration()
    completePrototypeExploration()

    // Assert Topic Completed.
    scrollToText("Chapter 1: Prototype Exploration")
    val chapters = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    val prototypeExplorationChapter = chapters.getChildByText(
      UiSelector().className("android.widget.FrameLayout"),
      "Chapter 1: Prototype Exploration"
    )
    val chapterCompletedTick = prototypeExplorationChapter.getChild(
      UiSelector().resourceId(
        "$OPPIA_PACKAGE:id/chapter_completed_tick"
      )
    )
    assertTrue(chapterCompletedTick.exists())
  }

  @Test
  fun testImageRegionSelectionInteraction_answerAllInteractionsAndCompleteExploration() {
    navigateToImageRegionSelectionInteraction()

    // / Image Region Selection Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/interaction_container_frame_layout")
    val imageSelectionView = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/interaction_container_frame_layout"
      )
    )
    imageSelectionView.children[2].click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // End Exploration.
    device.waitForRes("$OPPIA_PACKAGE:id/return_to_topic_button")
    device.findObject(UiSelector().text("RETURN TO TOPIC")).click()

    // Assert Topic Completed.
    scrollToText("Chapter 2: Image Region Selection Exploration")
    val chapters = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    val prototypeExplorationChapter = chapters.getChildByText(
      UiSelector().className("android.widget.FrameLayout"),
      "Chapter 2: Image Region Selection Exploration"
    )
    val chapterCompletedTick = prototypeExplorationChapter.getChild(
      UiSelector().resourceId(
        "$OPPIA_PACKAGE:id/chapter_completed_tick"
      )
    )
    assertTrue(chapterCompletedTick.exists())
  }

  /** Navigates and opens the Prototype Exploration using the admin profile. */
  private fun navigateToPrototypeExploration() {
    val skip_button = device.findObject(By.res("$OPPIA_PACKAGE:id/skip_text_view"))
    skip_button?.let {
      it.click()
      device.waitForRes("$OPPIA_PACKAGE:id/get_started_button")
      device.findObject(By.res("$OPPIA_PACKAGE:id/get_started_button"))
        .click()
    }
    device.waitForRes("$OPPIA_PACKAGE:id/profile_select_text")
    val profiles = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    profiles.getChildByText(UiSelector().className("android.widget.LinearLayout"), "Admin").click()
    scrollToText("First Test Topic")
    device.findObject(UiSelector().text("First Test Topic")).click()
    device.findObject(UiSelector().text("LESSONS")).click()
    device.findObject(UiSelector().text("First Story")).click()
    scrollToText("Chapter 1: Prototype Exploration")
    device.findObject(UiSelector().text("Chapter 1: Prototype Exploration")).click()
    device.waitForRes("$OPPIA_PACKAGE:id/exploration_toolbar_title")
  }

  /** Answers all the interactions and ends the exploration. */
  private fun completePrototypeExploration() {
    // Exploration description.
    device.findObject(UiSelector().text("CONTINUE")).click()

    // Fraction Input Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/exploration_toolbar_title")
    val fractionInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/fraction_input_interaction_view"
      )
    )
    fractionInputInteraction.text = "1/2"
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // Multiple Choice Interaction - 1.
    device.waitForRes("$OPPIA_PACKAGE:id/selection_interaction_recyclerview")
    device.findObject(UiSelector().text("Eagle")).click()
    clickContinueButton()

    // Multiple Choice Interaction - 2.
    device.waitForRes("$OPPIA_PACKAGE:id/selection_interaction_recyclerview")
    device.findObject(UiSelector().text("Green")).click()
    clickContinueButton()

    // Item Selection Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/selection_interaction_recyclerview")
    device.findObject(UiSelector().text("Red")).click()
    device.findObject(UiSelector().text("Green")).click()
    device.findObject(UiSelector().text("Blue")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // Numeric Input Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/numeric_input_interaction_view")
    val numericInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/numeric_input_interaction_view"
      )
    )
    numericInputInteraction.text = "121"
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // Ratio Input Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/ratio_input_interaction_view")
    val ratioInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/ratio_input_interaction_view"
      )
    )
    ratioInputInteraction.text = "4:5"
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // Text Input Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/text_input_interaction_view")
    val textInputInteraction = device.findObject(
      By.res(
        "$OPPIA_PACKAGE:id/text_input_interaction_view"
      )
    )
    textInputInteraction.text = "Finnish"
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // Drag And Drop Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")
    device.findObject(UiSelector().description("Move item down to 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().description("Move item down to 4")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // Drag Drop Merge Interaction.
    device.waitForRes("$OPPIA_PACKAGE:id/drag_drop_interaction_recycler_view")
    device.findObject(UiSelector().description("Link to item 2")).click()
    device.findObject(UiSelector().description("Move item down to 3")).click()
    device.findObject(UiSelector().text("SUBMIT")).click()
    clickContinueButton()

    // End Exploration.
    device.waitForRes("$OPPIA_PACKAGE:id/return_to_topic_button")
    device.findObject(UiSelector().text("RETURN TO TOPIC")).click()
  }
  /** Navigates and opens the Image Region Selection Exploration using the admin profile. */
  private fun navigateToImageRegionSelectionInteraction() {
    val skip_button = device.findObject(By.res("$OPPIA_PACKAGE:id/skip_text_view"))
    skip_button?.let {
      it.click()
      device.waitForRes("$OPPIA_PACKAGE:id/get_started_button")
      device.findObject(By.res("$OPPIA_PACKAGE:id/get_started_button"))
        .click()
    }
    device.waitForRes("$OPPIA_PACKAGE:id/profile_select_text")
    val profiles = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    profiles.getChildByText(UiSelector().className("android.widget.LinearLayout"), "Admin").click()
    scrollToText("First Test Topic")
    device.findObject(UiSelector().text("First Test Topic")).click()
    device.findObject(UiSelector().text("LESSONS")).click()
    device.findObject(UiSelector().text("First Story")).click()
    scrollToText("Chapter 1: Prototype Exploration")
    val chapters = UiCollection(UiSelector().className("androidx.recyclerview.widget.RecyclerView"))
    val prototypeExplorationChapter = chapters.getChildByText(
      UiSelector().className("android.widget.FrameLayout"),
      "Chapter 1: Prototype Exploration"
    )
    val chapterCompletedTick = prototypeExplorationChapter.getChild(
      UiSelector().resourceId(
        "$OPPIA_PACKAGE:id/chapter_completed_tick"
      )
    )
    if (!chapterCompletedTick.exists()) {
      device.findObject(UiSelector().text("Chapter 1: Prototype Exploration")).click()
      device.waitForRes("$OPPIA_PACKAGE:id/exploration_toolbar_title")
      completePrototypeExploration()
    }
    scrollToText("Chapter 2: Image Region Selection Exploration")
    device.findObject(UiSelector().text("Chapter 2: Image Region Selection Exploration")).click()
  }

  private fun clickContinueButton() {
    device.waitForRes("$OPPIA_PACKAGE:id/continue_navigation_button")
    device.findObject(UiSelector().text("CONTINUE")).click()
  }
}
