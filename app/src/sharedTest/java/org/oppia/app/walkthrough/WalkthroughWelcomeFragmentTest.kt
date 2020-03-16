package org.oppia.app.walkthrough

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.R
import org.oppia.app.onboarding.OnboardingActivity

/** Tests for [WalkthroughWelcomeFragment]. */
@RunWith(AndroidJUnit4::class)
class WalkthroughWelcomeFragmentTest {

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testWalkthoughWelcomeFragment_checkDescription_isCorrect() {
    ActivityScenario.launch(OnboardingActivity::class.java).use {
      Espresso.onView(
        CoreMatchers.allOf(
          ViewMatchers.withId(R.id.walkthrough_welcome_description_text_view),
          ViewMatchers.isCompletelyDisplayed()
        )
      ).check(ViewAssertions.matches(ViewMatchers.withText(R.string.walkthrough_welcome_description)))
    }
  }
}