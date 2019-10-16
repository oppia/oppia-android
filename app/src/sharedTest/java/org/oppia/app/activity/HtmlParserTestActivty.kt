package org.oppia.app.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R

/** Tests for [HtmlParserTestActivityTest]. */
@RunWith(AndroidJUnit4::class)
class HtmlParserTestActivityTest {
  @get:Rule
  var activityTestRule: ActivityTestRule<HtmlParserTestActivty> = ActivityTestRule(
    HtmlParserTestActivty::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Test
  fun testHtmlContent_replace_custom_oppia_tags_displaysStyleText() {
    ActivityScenario.launch(HtmlParserTestActivty::class.java).use {
      onView(withId(R.id.test_html_content_text_view)).check(matches(isDisplayed()))
    }
  }
}
