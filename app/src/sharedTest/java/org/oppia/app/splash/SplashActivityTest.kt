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

  /**
   * Creates an {@link ActivityTestRule} for the Activity under test.
   *
   * @param SplashActivity    The activity under test. This must be a class in the instrumentation
   *                         targetPackage specified in the AndroidManifest.xml
   * @param initialTouchMode indicates whether the activity should be launched in touch mode.
   * @param launchActivity   true if the Activity should be launched once per
   *                         "http://junit.org/javadoc/latest/org/junit/Test.html"
   *                         <code>Test</code></a> method. It will be launched before the first
   *                         "http://junit.sourceforge.net/javadoc/org/junit/Before.html"
   *                         <code>Before</code></a> method, and terminated after the last
   *                         "http://junit.sourceforge.net/javadoc/org/junit/After.html"
   *                         <code>After</code></a> method.
   *                         false if the Activity should be launched explicitly within each test case
   *                         We can either enter null to use default intent or pass a custom intent as a parameter
   *                         https://jabknowsnothing.wordpress.com/2015/11/05/activitytestrule-espressos-test-lifecycle/
   */
  @get:Rule
  var activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(
    SplashActivity::class.java, true,
    false
  )

  @Before
  fun setUp() {
    //initial setup code
    Intents.init()
  }

  @Test
  fun testSplashActivity_initialOpen_routesToHomeActivity() {
    activityTestRule.launchActivity(null)

    intended(hasComponent(HomeActivity::class.java.getName()))
  }

  @After
  fun tearDown() {
    //clean up code
    Intents.release()
  }
}
