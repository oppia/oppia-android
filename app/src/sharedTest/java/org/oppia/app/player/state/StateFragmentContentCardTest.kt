package org.oppia.app.player.state

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
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
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition

/** Tests for [VIEW_TYPE_CONTENT]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentContentCardTest {

  private lateinit var launchedActivity: Activity

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
  fun testContentCard_loadExplorationTest5_handleCustomOppiaTags_parsedHtmlDisplaysCorrectly() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      val htmlResult =  "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be continually improved over time.\n\n" +
            "Incidentally, do you know where the name 'Oppia' comes from?\n\n"
      onView(atPosition(R.id.state_recycler_view, 0)).check(matches(hasDescendant(withText(htmlResult))))
    }
  }
  @After
  fun tearDown() {
    Intents.release()
  }
}
