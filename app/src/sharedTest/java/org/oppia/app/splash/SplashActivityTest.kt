package org.oppia.app.splash

import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.home.HomeActivity

/** Tests for [SplashActivity]. */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(
    SplashActivity::class.java, true,
    false
  )

  @Before
  fun setUp() {
    Intents.init()
  }

  @After
  fun tearDown() {
    Intents.release()
  }

  @Test
  fun testSplashActivity_initialOpen_routesToHomeActivity() {

    activityTestRule.launchActivity(null)

    intended(hasComponent(HomeActivity::class.java.getName()))
  }
}
