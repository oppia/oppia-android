package org.oppia.app.parser

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.text.Spannable
import android.widget.TextView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.testing.HtmlParserTestActivity
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Tests for [HtmlParser]. */
@RunWith(AndroidJUnit4::class)
class HtmlParserTest {

  private lateinit var launchedActivity: Activity
  @Inject
  lateinit var htmlParserFactory: HtmlParser.Factory

  @get:Rule
  var activityTestRule: ActivityTestRule<HtmlParserTestActivity> = ActivityTestRule(
    HtmlParserTestActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }

  @Test
  fun testHtmlContent_handleCustomOppiaTags_parsedHtmlDisplaysStyledText() {
    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(/* entityType= */ "", /* entityId= */ "")
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
      "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia-noninteractive-image " +
          "alt-with-value=\"\u0026amp;quot;Pineapple" +
          " cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\" filepath-with-value=\"\u0026amp;quot;" +
          "pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/" +
          "oppia-noninteractive-image\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrong\u003eQuestion 6\u003c/strong\u003e: What " +
          "fraction of the cake has big red cherries in the pineapple slices?\u003c/p\u003e",
      textView
    )
    assertThat(textView.text.toString()).isEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(isDisplayed()))
    onView(withId(R.id.test_html_content_text_view)).check(matches(withText(textView.text.toString())))
  }

  @Test
  fun testHtmlContent_nonCustomOppiaTags_notParsed() {
    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
    val htmlParser = htmlParserFactory.create(/* entityType= */ "", /* entityId= */ "")
    val htmlResult: Spannable = htmlParser.parseOppiaHtml(
         "\u003cp\u003e\"Let's try one last question,\" said Mr. Baker. \"Here's a pineapple cake cut into pieces.\"\u003c/p\u003e\u003coppia--image " +
             "alt-with-value=\"\u0026amp;quot;Pineapple cake with 7/9 having cherries.\u0026amp;quot;\" caption-with-value=\"\u0026amp;quot;\u0026amp;quot;\"" +
             " filepath-value=\"\u0026amp;quot;pineapple_cake_height_479_width_480.png\u0026amp;quot;\"\u003e\u003c/oppia-noninteractive-image" +
             "\u003e\u003cp\u003e\u00a0\u003c/p\u003e\u003cp\u003e\u003cstrongQuestion 6\u003c/strong\u003e: What fraction of the cake has big " +
             "red cherries in the pineapple slices?\u003c/p\u003e",
         textView
       )
    // The two strings aren't equal because this HTML contains a Non-Oppia/Non-Html tag e.g. <image> tag and attributes "filepath-value" which isn't parsed.
    assertThat(textView.text.toString()).isNotEqualTo(htmlResult.toString())
    onView(withId(R.id.test_html_content_text_view)).check(matches(not(textView.text.toString())))
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
