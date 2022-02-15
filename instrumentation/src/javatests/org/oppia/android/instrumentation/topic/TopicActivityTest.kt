package org.oppia.android.instrumentation.topic

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Before
import org.junit.Test
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.findObjectByRes
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.findObjectByText
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.scrollRecyclerViewTextIntoView
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.startOppiaFromScratch
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.waitForRes
import org.oppia.android.instrumentation.testing.EndToEndTestHelper.waitForText

/** Tests for TopicActivity. */
class TopicActivityTest {
  private lateinit var device: UiDevice

  @Before
  fun setUp() {
    // Initialize UiDevice instance
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.startOppiaFromScratch()
  }

  @Test
  fun testTopicActivity_exploredFully_finishedSuccessfully() {
    navigateToTopicActivity()
    device.findObjectByText("See More").click()
    device.waitForText("See Less")
    device.findObjectByText("See Less").click()

    device.findObjectByText("LESSONS").click()
    device.findObjectByRes("expand_list_icon").click()
    device.waitForRes("chapter_list_container")
    device.findObjectByText("What is a Fraction?").click()
    device.waitForText("CONTINUE")
    device.pressBack()
    device.waitForRes("expand_list_icon")
    device.findObjectByRes("expand_list_icon").click()
    device.findObjectByRes("story_name_text_view").click()
    device.waitForRes("story_toolbar_title")
    device.pressBack()

    device.findObjectByText("PRACTICE").click()
    checkSkillsForPracticeFragment()
    device.findObjectByText("START").click()
    device.waitForText("PRACTICE MODE")
    device.pressBack()
    device.findObjectByText("LEAVE").click()

    device.findObjectByText("REVISION").click()
    device.findObjectByText("Fractions of a group").click()
    device.findObjectByText("RETURN TO TOPIC").click()
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
