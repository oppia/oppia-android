package org.oppia.app.splash

import androidx.fragment.app.Fragment
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents
import androidx.test.rule.ActivityTestRule
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import org.oppia.app.HomeActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingResource
import org.junit.*

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
  @Throws(InterruptedException::class)
  fun testSplashActivity_initialOpen_routesToHomeActivity() {

    activityTestRule.launchActivity(null)

    intended(hasComponent(HomeActivity::class.java.getName()))
  }
}
