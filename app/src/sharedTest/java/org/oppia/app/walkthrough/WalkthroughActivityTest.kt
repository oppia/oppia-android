package org.oppia.app.walkthrough

import androidx.test.core.app.ActivityScenario.launch
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.allOf
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.OrientationChangeAction.Companion.orientationLandscape
import org.oppia.app.utility.ProgressMatcher.Companion.withProgress
import org.robolectric.annotation.LooperMode

/** Tests for [WalkthroughActivity]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class WalkthroughActivityTest {

  @Test
  fun testWalkthroughFragment_defaultProgress_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
    }
  }

  @Test
  fun testWalkthroughFragment_checkFrameLayout_backButton_progressBar_IsVisible() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_fragment_placeholder)).check(matches(isDisplayed()))
      onView(withId(R.id.back_button)).check(matches(isDisplayed()))
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_configurationChanged_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
    }
  }

  @Test
  fun testWalkthroughFragment_nextBtn_configurationChanged_backBtn_worksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(isRoot()).perform(orientationLandscape())
      onView(withId(R.id.back_button)).perform(click())
      onView(
        allOf(
          withId(R.id.walkthrough_welcome_description_text_view),
          isCompletelyDisplayed()
        )
      ).check(matches(withText(R.string.walkthrough_welcome_description)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_onBackPressed_decreaseProgress_progressWorksCorrectly() { // ktlint-disable max-line-length
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
      pressBack()
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_decreaseProgress_progressWorksCorrectly() {
    launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.walkthrough_welcome_next_button)).perform(scrollTo(), click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(2)))
      onView(withId(R.id.back_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
    }
  }
}
