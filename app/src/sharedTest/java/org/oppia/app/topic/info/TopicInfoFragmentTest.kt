package org.oppia.app.topic.info

import android.content.pm.ActivityInfo
import android.text.SpannedString
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.containsString
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.topic.TopicActivity
import org.oppia.app.utility.EspressoTestsMatchers.withDrawable

private const val TEST_TOPIC_ID = "GJ2rLXRKD5hw"
private const val TOPIC_NAME = "Fractions"
private const val TOPIC_DESCRIPTION =
  "You'll often need to talk about part of an object or group. For example, a jar of milk might be half-full, or " +
      "some of the eggs in a box might have broken. In these lessons, you'll learn to use fractions to describe " +
      "situations like these."

// NOTE TO DEVELOPERS: this test must be annotated with @LooperMode(LooperMode.MODE.PAUSED) to pass on Robolectric.
/** Tests for [TopicInfoFragment]. */
@RunWith(AndroidJUnit4::class)
class TopicInfoFragmentTest {
  private val topicThumbnail = R.drawable.lesson_thumbnail_graphic_child_with_fractions_homework

  @Test
  fun testTopicInfoFragment_loadFragment_checkTopicName_isCorrect() {
    launchTopicActivityIntent(TEST_TOPIC_ID).use {
      onView(withId(R.id.topic_name_text_view)).check(matches(withText(containsString(TOPIC_NAME))))
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragmentWithTestTopicId1_checkTopicDescription_isCorrect() {
    launchTopicActivityIntent(TEST_TOPIC_ID).use {
      onView(withId(R.id.topic_description_text_view)).check(matches(withText(containsString(TOPIC_DESCRIPTION))))
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragmentWithTestTopicId1_checkTopicDescription_hasRichText() {
    launchTopicActivityIntent(TEST_TOPIC_ID).use { scenario ->
      scenario.onActivity { activity ->
        val descriptionTextView: TextView = activity.findViewById(R.id.topic_description_text_view)
        val descriptionText = descriptionTextView.text as SpannedString
        val spans = descriptionText.getSpans(0, descriptionText.length, StyleSpan::class.java)
        assertThat(spans).isNotEmpty()
      }
    }
  }

  @Test
  fun testTopicInfoFragment_loadFragment_configurationChange_checkTopicThumbnail_isCorrect() {
    launchTopicActivityIntent(TEST_TOPIC_ID).use {
      onView(withId(R.id.topic_thumbnail_image_view)).check(matches(withDrawable(topicThumbnail)))
    }
  }

  @Test
  @Ignore("Landscape not properly supported") // TODO(#56): Reenable once landscape is supported.
  fun testTopicInfoFragment_loadFragment_configurationChange_checkTopicName_isCorrect() {
    launchTopicActivityIntent(TEST_TOPIC_ID).use { scenario ->
      scenario.onActivity {  activity ->
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
      }

      onView(withId(R.id.topic_name_text_view)).check(matches(withText(containsString(TOPIC_NAME))))
    }
  }

  private fun launchTopicActivityIntent(topicId: String): ActivityScenario<TopicActivity> {
    val intent = TopicActivity.createTopicActivityIntent(ApplicationProvider.getApplicationContext(), topicId)
    return ActivityScenario.launch(intent)
  }
}
