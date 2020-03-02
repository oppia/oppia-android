package org.oppia.app.help

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.faq.FAQActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.testing.NavigationDrawerTestActivity

@RunWith(AndroidJUnit4::class)
class HelpFragmentTest {
  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun openNavigationDrawer_selectHelpMenuInNavigationDrawer_scrollRecyclerViewToZeroPosition_showsFAQSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(withId(R.id.help_fragment_recycler_view)).perform(scrollToPosition<RecyclerView.ViewHolder>(0))
      onView(atPositionOnView(R.id.help_fragment_recycler_view, 0, R.id.recycler_item_text_view)).check(
        matches(
          withText(R.string.frequently_asked_questions_FAQ)
        )
      )
    }
  }

  @Test
  fun openNavigationDrawer_selectHelpMenuInNavigationDrawer_selectFAQ_showFAQActivitySuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(atPosition(R.id.help_fragment_recycler_view, 0)).perform(click())
      intended(hasComponent(FAQActivity::class.java.getName()))
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
