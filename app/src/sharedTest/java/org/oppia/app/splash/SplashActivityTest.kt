package org.oppia.app.splash

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.HomeActivity
import org.oppia.app.R

/** Tests for [SplashActivity]. */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {
  @get:Rule
  val activityActivityTestRule = ActivityScenarioRule(SplashActivity::class.java)

  @Test
  fun testSplashFragment_isDisplayed() {
    onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));

    intended(hasComponent(HomeActivity::class.java.getName()))
  }
}
