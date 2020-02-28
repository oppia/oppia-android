package org.oppia.app.walkthrough

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.utility.ProgressMatcher.Companion.withProgress

/** Tests for [WalkthroughFragment]. */
@RunWith(AndroidJUnit4::class)
class OnboardingFragmentTest {

  @Test
  fun testWalkthroughFragment_checkSkipButton_skipNextButton_progressBar_IsVisible() {
    ActivityScenario.launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.next_button)).check(matches(isDisplayed()))
      onView(withId(R.id.back_button)).check(matches(isDisplayed()))
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(isDisplayed()))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_worksCorrectly() {
    ActivityScenario.launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.next_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
    }
  }

  @Test
  fun testWalkthroughFragment_increaseProgress_decreaseProgress_progressWorksCorrectly() {
    ActivityScenario.launch(WalkthroughActivity::class.java).use {
      onView(withId(R.id.next_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(1)))
      onView(withId(R.id.back_button)).perform(click())
      onView(withId(R.id.walkthrough_progress_bar)).check(matches(withProgress(0)))
    }
  }
}
