package org.oppia.app.help

import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario.launch
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions.close
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.help.faq.FAQListActivity
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPosition
import org.oppia.app.recyclerview.RecyclerViewMatcher.Companion.atPositionOnView
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape

@RunWith(AndroidJUnit4::class)
class HelpFragmentTest {
  @Before
  fun setUp() {
    Intents.init()
    FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
  }

  @Test
  fun openHelpActivity_scrollRecyclerViewToZeroPosition_showsFAQSuccessfully() {
    launch(HelpActivity::class.java).use {
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(R.id.help_fragment_recycler_view, 0, R.id.help_item_text_view)
      ).check(
        matches(withText(R.string.frequently_asked_questions_FAQ))
      )
    }
  }

  @Test
  fun openHelpActivity_configurationChanged_scrollRecyclerViewToZeroPosition_showsFAQSuccessfully() {
    launch(HelpActivity::class.java).use {
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.help_fragment_recycler_view)).perform(
        scrollToPosition<RecyclerView.ViewHolder>(0)
      )
      onView(
        atPositionOnView(
          R.id.help_fragment_recycler_view, 0, R.id.help_item_text_view
        )
      ).check(matches(withText(R.string.frequently_asked_questions_FAQ)))
    }
  }

  @Test
  fun openHelpActivity_selectFAQ_showFAQActivitySuccessfully() {
    launch(HelpActivity::class.java).use {
      onView(atPosition(R.id.help_fragment_recycler_view, 0)).perform(click())
      intended(hasComponent(FAQListActivity::class.java.name))
    }
  }

  @Test
  fun openHelpActivity_openNavigationDrawer_navigationDrawerOpeningIsVerifiedSuccessfully() {
    launch(HelpActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).check(
        matches(isCompletelyDisplayed())
      ).perform(click())
      onView(withId(R.id.help_fragment_placeholder))
        .check(matches(isCompletelyDisplayed()))
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun openHelpActivity_openNavigationDrawerAndClose_closingOfNavigationDrawerIsVerifiedSuccessfully() {
    launch(HelpActivity::class.java).use {
      onView(withContentDescription(R.string.drawer_open_content_description)).perform(click())
      onView(withId(R.id.help_activity_drawer_layout)).perform(close())
      onView(withId(R.id.help_activity_drawer_layout)).check(matches(isClosed()))
    }
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
