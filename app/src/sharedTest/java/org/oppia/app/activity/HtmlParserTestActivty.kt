package org.oppia.app.activity

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.text.Spannable
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import junit.framework.Assert.assertEquals
import junit.framework.TestCase.assertNotSame
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.util.parser.HtmlParser

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
  fun testHtmlContent_handleCustomOppiaTags_parsedHtmldisplaysStyledText() {
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlResult: Spannable = HtmlParser(
      ApplicationProvider.getApplicationContext(), "", ""
    ).parseOppiaHtml(
      "\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e"
      , textView
    )
    assertEquals(htmlResult.toString(), textView.text.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(isDisplayed()))
      .check(matches(withText(htmlResult.toString())))
  }

  @Test
  fun testHtmlContent_nonCustomOppiaTags_notParsed() {
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    activityTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlResult: Spannable = HtmlParser(
      ApplicationProvider.getApplicationContext(), "", ""
    ).parseOppiaHtml(
      "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-interactive-image alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e"
      , textView
    )
    assertNotSame(htmlResult.toString(), textView.text.toString())
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
