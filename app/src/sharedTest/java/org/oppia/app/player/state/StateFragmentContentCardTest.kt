package org.oppia.app.player.state

import android.app.Activity
import android.content.Intent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject

/** Tests for [VIEW_TYPE_CONTENT]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentContentCardTest {

  private lateinit var launchedActivity: Activity
  @Inject
  lateinit var htmlParserFactory : HtmlParser.Factory

  @get:Rule
  var activityTestRule: ActivityTestRule<HomeActivity> = ActivityTestRule(
    HomeActivity::class.java, /* initialTouchMode= */ true, /* launchActivity= */ false
  )

  @Before
  fun setUp() {
    Intents.init()
    val intent = Intent(Intent.ACTION_PICK)
    launchedActivity = activityTestRule.launchActivity(intent)
  }

//  @Test
//  fun testHtmlContent_handleCustomOppiaTags_parsedHtmldisplaysStyledText() {
//
//    val htmlResult = "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be continually improved over time.\n" +
//        "    \n" +
//        "    Incidentally, do you know where the name 'Oppia' comes from?"
//    onData(allOf(`is`(instanceOf(StateAdapter::class.java)),hasEntry(equalTo(htmlResult),
//      `is`("item: 0"))))
////    assertThat(textView.text.toString()).isEqualTo(htmlResult.toString())
////    onView(withId(R.id.test_html_content_text_view)).check(matches(isDisplayed()))
////    onView(withId(R.id.test_html_content_text_view)).check(matches(withText(textView.text.toString())))
//  }

  @Test
  fun testHtmlContent_nonCustomOppiaTags_notParsed() {
//    val textView = activityTestRule.activity.findViewById(R.id.test_html_content_text_view) as TextView
//    val htmlResult: Spannable = htmlParserFactory.create( /* entityType= */ "",  /* entityId= */ "")
//      .parseOppiaHtml(
//        "<p>Hi, welcome to Oppia! <oppia-noninteractive-link text-with-value=\"&quot;Oppia&quot;\" url-with-value=\"&quot;https://oppia.github.io&quot;\"></oppia-noninteractive-link> is a tool that helps you create interactive learning activities that can be continually improved over time.<br><br>Incidentally, do you know where the name 'Oppia' comes from?<br></p>",
//      textView
//    )
//    // The two strings aren't equal because this HTML contains a Non-Oppia/Non-Html tag e.g. <image> tag and attributes "filepath-value" which isn't parsed.
//    assertThat(textView.text.toString()).isNotEqualTo(htmlResult.toString())
//    onView(withId(R.id.test_html_content_text_view)).check(matches(not(textView.text.toString())))

    val htmlResult = "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be continually improved over time.\n" +
        "    \n" +
        "    Incidentally, do you know where the name 'Oppia' comes from?"
//    onData(allOf(`is`(instanceOf(StateAdapter::class.java)),hasEntry(equals(htmlResult),
//      `is`("0"))))
    onView(withRecyclerView(R.id.state_recycler_view).atPosition(0))
      .check(matches(hasDescendant(withText("whatever"))));
  }
  fun withRecyclerView(recyclerViewId: Int): RecyclerViewMatcher {
    return RecyclerViewMatcher(recyclerViewId)
  }
  @After
  fun tearDown() {
    Intents.release()
  }
}
