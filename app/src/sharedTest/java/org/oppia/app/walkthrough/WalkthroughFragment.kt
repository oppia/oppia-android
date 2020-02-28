package org.oppia.app.walkthrough

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.ProgressMatcher.withProgress

/** Tests for [WalkthroughFragment]. */
@RunWith(AndroidJUnit4::class)
class OnboardingFragmentTest {

  @Test
  fun testWalkthroughFragment_skipNextButtonIsVisible() {
    ActivityScenario.launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.next_button)).check(matches(isDisplayed()))
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(isDisplayed()))
    }
  }
  @Test
  fun testWalkthroughFragment_increaseProgress() {
    ActivityScenario.launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.next_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseAndDecreaseProgress() {
    ActivityScenario.launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.next_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
      onView(withId(R.id.back_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(0)))
    }
  }
}
