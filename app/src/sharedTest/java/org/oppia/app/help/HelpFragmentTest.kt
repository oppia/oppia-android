package org.oppia.app.help

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.open
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.FAQ.FAQActivity
import org.oppia.app.testing.NavigationDrawerTestActivity

@RunWith(AndroidJUnit4::class)
class HelpFragmentTest {
  @Test
  fun openNavigationDrawer_selectHelpMenuInNavigationDrawer_showsFAQSuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(withText(R.string.frequently_asked_questions_FAQ)).check(matches(withText(R.string.frequently_asked_questions_FAQ)))
    }
  }

  @Test
  fun openNavigationDrawer_selectHelpMenuInNavigationDrawer_selectFAQ_showFAQActivitySuccessfully() {
    launch(NavigationDrawerTestActivity::class.java).use {
      onView(withId(R.id.home_activity_drawer_layout)).perform(open())
      onView(withText(R.string.menu_help)).perform(click())
      onView(withText(R.string.frequently_asked_questions_FAQ)).perform(click())
      intended(hasComponent(FAQActivity::class.java.name))
    }
  }
}