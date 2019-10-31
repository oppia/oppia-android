package org.oppia.app.player.state

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.home.HomeActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition

// TODO(#277): Add test case for image parsing once PR #277 is merged.
/** Tests for [VIEW_TYPE_CONTENT]. */
@RunWith(AndroidJUnit4::class)
class StateFragmentContentCardTest {

  @Test
  fun testContentCard_forDemoExploration_withCustomOppiaTags_displaysParsedHtml() {
    ActivityScenario.launch(HomeActivity::class.java).use {
      onView(withId(R.id.play_exploration_button)).perform(click())
      val htmlResult =  "Hi, welcome to Oppia! is a tool that helps you create interactive learning activities that can be continually improved over time.\n\n" +
            "Incidentally, do you know where the name 'Oppia' comes from?\n\n"
      onView(atPosition(R.id.state_recycler_view, 0)).check(matches(hasDescendant(withText(htmlResult))))
    }
  }
}
