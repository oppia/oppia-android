package org.oppia.android.instrumentation.topic

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
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.waitForText

/** Tests for the topic viewer screen. */
class TopicViewerTest {
  private lateinit var device: UiDevice

  @Before
  fun setUp() {
    // Initialize UiDevice instance
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.startOppiaFromScratch()
  }

  @Test
  fun testTopicActivity_exploreInfoTab_finishesSuccessfully() {
    navigateToTopicActivity()
    device.findObjectByText("See More").click()
    device.waitForText("See Less")
    device.findObjectByText("See Less").click()
    assertThat(device.findObjectByText("See More")).isNotNull()
  }

  @Test
  fun testTopicActivity_exploreLessonsTab_finishesSuccessfully() {
    navigateToTopicActivity()
    device.findObjectByText("LESSONS").click()
    device.findObjectByRes("expand_list_icon").click()
    device.waitForRes("chapter_list_container")
    device.findObjectByText("What is a Fraction?").click()
    device.waitForRes("continue_navigation_button")
    device.pressBack()
    // Asserting that lessons fragment is open
    assertThat(device.findObjectByText("Stories You Can Play")).isNotNull()
    assertThat(device.findObjectByRes("chapter_play_state_icon")).isNotNull()
    val contentDescription: String =
      device.findObjectByRes("story_progress_view").getParent().getContentDescription()
    assertThat(contentDescription).isEqualTo("0% stories completed and 1 chapters in progress.")

    device.waitForRes("expand_list_icon")
    device.findObjectByRes("expand_list_icon").click()
    device.findObjectByRes("story_name_text_view").click()
    device.waitForRes("story_toolbar_title")
    device.findObjectByText("Chapter 1: What is a Fraction?").click()
    device.findObjectByText("START OVER").click()
    device.waitForRes("continue_navigation_button")
    device.pressBack()
    device.pressBack()
    // Asserting that lessons fragment is open
    assertThat(device.findObjectByText("Stories You Can Play")).isNotNull()
  }

  @Test
  fun testTopicActivity_explorePracticeTab_finishesSuccessfully() {
    navigateToTopicActivity()
    device.findObjectByText("PRACTICE").click()
    assertThat(device.findObjectByText("START").isEnabled()).isFalse()
    checkSkillsForPracticeFragment()
    device.findObjectByText("START").click()
    device.waitForText("PRACTICE MODE")
    device.pressBack()
    device.findObjectByText("LEAVE").click()
    // Asserting that practice fragment is open
    assertThat(device.findObjectByText("Master These Skills")).isNotNull()
  }

  @Test
  fun testTopicActivity_exploreRevisionTab_finishesSuccessfully() {
    navigateToTopicActivity()
    device.findObjectByText("REVISION").click()
    device.findObjectByText("Fractions of a group").click()
    device.findObjectByText("RETURN TO TOPIC").click()
    // Asserting that revision fragment is open
    assertThat(device.findObjectByRes("subtopic_title")).isNotNull()
  }

  private fun checkSkillsForPracticeFragment() {
    device.findObjectByText("What is a Fraction?").click()
    device.findObjectByText("Fractions of a group").click()
    device.findObjectByText("Mixed Numbers").click()
    device.findObjectByText("Adding Fractions").click()
  }

  private fun navigateToTopicActivity() {
    device.findObjectByRes("skip_text_view").click()
    device.findObjectByRes("get_started_button").click()
    device.waitForRes("profile_select_text")
    device.findObjectByText("Admin").click()
    scrollRecyclerViewTextIntoView("Fractions")
    device.findObjectByText("Fractions").click()
  }
}
