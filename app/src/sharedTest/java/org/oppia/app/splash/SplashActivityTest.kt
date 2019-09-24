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

/** Tests for [SplashActivity].
 * https://jabknowsnothing.wordpress.com/2015/11/05/activitytestrule-espressos-test-lifecycle */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {

  @get:Rule
  var activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(
    SplashActivity::class.java, true, // initialTouchMode. True to launch activity in touch mode
    false  // launchActivity. False to set intent per test
  )

  @Before
  fun setUp() {
    Intents.init()
  }

  @Test
  fun testSplashActivity_initialOpen_routesToHomeActivity() {
    activityTestRule.launchActivity(null)
    intended(hasComponent(HomeActivity::class.java.getName()))
  }

  @After
  fun tearDown() {
    Intents.release()
  }
}
