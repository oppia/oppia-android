package org.oppia.app.player.state

import android.app.Activity
import android.content.Intent
import android.text.Spannable
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.app.player.exploration.ExplorationActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.util.parser.HtmlParser
import javax.inject.Inject
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.espresso.matcher.ViewMatchers
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher

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

  @Test
  fun testHtmlContent_handleCustomOppiaTags_parsedHtmldisplaysStyledText() {

    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(ViewActions.click())
      onView(withId(R.id.state_recycler_view)).check(matches(ViewMatchers.isDisplayed()))
      onView(atPosition(R.id.state_recycler_view, 0)).check(matches(hasDescendant(withId(R.id.content_text_view))))
      val textView = activityTestRule.activity.findViewById(R.id.content_text_view) as TextView
    val htmlResult: Spannable = htmlParserFactory.create( /* entityType= */ "",  /* entityId= */ "")
      .parseOppiaHtml(
        "<p>Hi, welcome to Oppia! <oppia-noninteractive-link text-with-value=\"&quot;Oppia&quot;\" url-with-value=\"&quot;https://oppia.github.io&quot;\"></oppia-noninteractive-link> is a tool that helps you create interactive learning activities that can be continually improved over time.<br><br>Incidentally, do you know where the name 'Oppia' comes from?<br></p>",
      textView
    )

      onView(atPosition(R.id.state_recycler_view, 1)).check(matches(hasDescendant(withText(htmlResult.toString()))))
    }
  }

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
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(ViewActions.click())
      val htmlResult ="<p>Hi, welcome to Oppia! <oppia-noninteractive-link text-with-value=\"&quot;Oppia&quot;\" url-with-value=\"&quot;https://oppia.github.io&quot;\"></oppia-noninteractive-link> is a tool that helps you create interactive learning activities that can be continually improved over time.<br><br>Incidentally, do you know where the name 'Oppia' comes from?<br></p>"

//        "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be continually improved over time.\n" +
//            "\n" +
//            "    Incidentally, do you know where the name 'Oppia' comes from?"
      onView(atPosition(R.id.state_recycler_view, 0)).check(matches(hasDescendant(withText(htmlResult))))
    }
  }


  @After
  fun tearDown() {
    Intents.release()
  }
}
