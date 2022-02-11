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
  fun testTopicActivity_lessonsFragment_expandListIsClicked_ListExpands() {
    navigateToTopicActivity()
    device.findObjectByText("LESSONS").click()
    device.findObjectByRes("expand_list_icon").click()
    // Assert list expanded
    val expandedList = device.findObjectByRes("chapter_list_container")
    assertThat(expandedList).isNotNull()
  }

  @Test
  fun testTopicActivity_lessonsFragment_storyNameIsClicked_storyFragmentOpens() {
    navigateToTopicActivity()
    device.findObjectByText("LESSONS").click()
    val storyNameText = device.findObjectByRes("story_name_text_view").getText()
    device.findObjectByRes("story_name_text_view").click()
    // Assert story fragment is opened.
    // Find a better way?
    val titleText = device.findObjectByRes("story_toolbar_title").getText()
    assertThat(titleText).isEqualTo(storyNameText)
  }

// @Test
//  fun testTopicActivity_lessonsFragment_chapterNameIsClicked_ChapterOpens(){
//
//  }

  @Test
  fun testTopicActivity_practiceFragment_nothingChecked_startButtonsIsDisabled() {
    navigateToTopicActivity()
    device.findObjectByText("PRACTICE").click()
    // Asserting button is disabled
    val startButtonStatus = device.findObjectByText("START").isEnabled()
    assertThat(startButtonStatus).isFalse()
  }

  @Test
  fun testTopicActivity_practiceFragment_ItemsChecked_startButtonsIsEnabled() {
    navigateToTopicActivity()
    device.findObjectByText("PRACTICE").click()
    device.findObjectByText("Mixed Numbers").click()
    // Asserting button is Enabled
    val startButtonStatus = device.findObjectByText("START").isEnabled()
    assertThat(startButtonStatus).isTrue()
  }
  /*
      @Test
      fun testTopicActivity_practiceFragment_startButtonIsClicked_practiceModeOpens(){

      }

      @Test
      fun testTopicActivity_revisionFragment_itemIsClicked_revisionStarts(){

      }
    */
  private fun navigateToTopicActivity() {
    device.findObjectByRes("skip_text_view").click()
    device.findObjectByRes("get_started_button").click()
    device.waitForRes("profile_select_text")
    device.findObjectByText("Admin").click()
    scrollRecyclerViewTextIntoView("Fractions")
    device.findObjectByText("Fractions").click()
  }
}
