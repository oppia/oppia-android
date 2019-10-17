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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.splash.SplashActivity

/** Tests for [HtmlParserTestActivityTest]. */
@RunWith(AndroidJUnit4::class)
class HtmlParserTestActivityTest {

  private lateinit var launchedActivity: Activity

  @get:Rule
  var activityTestRule: ActivityTestRule<HtmlParserTestActivty> = ActivityTestRule(
    HtmlParserTestActivty::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }

  @Test
  fun testHtmlContent_replaceCustomOppiaTags_displaysStyleText() {
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    onView(withId(R.id.test_html_content_text_view)).check(matches(isDisplayed()))
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
