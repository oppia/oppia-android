package org.oppia.app.testing

import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.contrib.DrawerMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.recyclerview.RecyclerViewMatcher

/** Tests for [NavigationDrawerTestActivity]. */
@RunWith(AndroidJUnit4::class)
class NavigationDrawerTestActivityTest {
  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHandBurger_navigationDrawerIsOpen() {
    ActivityScenario.launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isDisplayed())
      ).perform(click())
      onView(withId(R.id.home_fragment_placeholder))
        .check(matches(isDisplayed()))
      onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHandBurger_closingOfNavigationDrawerIsVerifiedSucessfully() {
    ActivityScenario.launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isDisplayed())
      ).perform(click())
      onView(withId(R.id.drawer_layout)).perform(DrawerActions.close())
      onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isClosed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHandBurger_clickHelpMenuInNavigationDrawer_showsHelpFragment() {
    ActivityScenario.launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(ViewMatchers.withText("Help")).check(matches(isDisplayed())).perform(click())
      onView(
        Matchers.allOf(
          Matchers.instanceOf(TextView::class.java),
          ViewMatchers.withParent(withId(R.id.toolbar))
        )
      )
        .check(matches(ViewMatchers.withText("Help")))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHandBurger_clickHelpMenuInNavigationDrawer_clickNavigationDrawerHandBurger_navigationDrawerIsOpenedAndVerified() {
    ActivityScenario.launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(ViewMatchers.withText("Help")).check(matches(isDisplayed())).perform(click())
      onView(
        Matchers.allOf(
          Matchers.instanceOf(TextView::class.java),
          ViewMatchers.withParent(withId(R.id.toolbar))
        )
      )
        .check(matches(ViewMatchers.withText("Help")))
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isDisplayed())
      ).perform(click())
      onView(withId(R.id.home_fragment_placeholder))
        .check(matches(isDisplayed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHandBurger_clickHelpMenuInNavigationDrawer_openingAndClosingOfDrawerIsVerified_navigationDrawerIsOpenAndVerified() {
    ActivityScenario.launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(ViewMatchers.withText("Help")).check(matches(isDisplayed())).perform(click())
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(withId(R.id.drawer_layout)).perform(DrawerActions.close())
      onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isClosed()))
      onView(
        Matchers.allOf(
          Matchers.instanceOf(TextView::class.java),
          ViewMatchers.withParent(withId(R.id.toolbar))
        )
      )
        .check(matches(ViewMatchers.withText("Help")))
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isDisplayed())
      ).perform(click())
      onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
      onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isOpen()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHandBurger_clickHelpMenuInNavigationDrawer_navigationDrawerClosingIsVerified() {
    ActivityScenario.launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(ViewMatchers.withText("Help")).check(matches(isDisplayed())).perform(click())
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(
        Matchers.allOf(
          Matchers.instanceOf(TextView::class.java),
          ViewMatchers.withParent(withId(R.id.toolbar))
        )
      )
        .check(matches(ViewMatchers.withText("Help")))
      onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
      onView(withId(R.id.drawer_layout)).perform(DrawerActions.close())
      onView(withId(R.id.drawer_layout)).check(matches(DrawerMatchers.isClosed()))
    }
  }

  @Test
  fun testNavigationDrawerTestActivity_clickNavigationDrawerHandBurger_clickHelpMenuInNavigationDrawer_clickHomeMenuInNavigationDrawer_showsHomeFragment() {
    ActivityScenario.launch(NavigationDrawerTestActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(ViewMatchers.withText("Help")).check(matches(isDisplayed())).perform(click())
      onView(withContentDescription(R.string.drawer_open_content_description))
        .perform(click())
      onView(ViewMatchers.withText("Home")).check(matches(isDisplayed())).perform(click())
      onView(
        Matchers.allOf(
          Matchers.instanceOf(TextView::class.java),
          ViewMatchers.withParent(withId(R.id.toolbar))
        )
      )
        .check(matches(ViewMatchers.withText("Home")))
      onView(
        RecyclerViewMatcher.atPositionOnView(
          R.id.home_recycler_view,
          0,
          R.id.welcome_text_view
        )
      ).check(matches(ViewMatchers.withText("Welcome to Oppia!")))
    }
  }
}
