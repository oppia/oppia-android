package org.oppia.app.splash

import androidx.fragment.app.Fragment
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.HomeActivity
import androidx.test.espresso.intent.rule.IntentsTestRule
import org.oppia.app.R

/** Tests for [SplashActivity]. */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {
//  @get:Rule
//  val activityActivityTestRule = ActivityScenarioRule(SplashActivity::class.java)

  @Test
  fun testSplashFragment_isDisplayed() {
//    launchFragmentInContainer<SplashFragment>()
//    onView(withId(R.id.fragment_container)).check(matches(isDisplayed()));

    val scenario = launchFragmentInContainer<SplashFragment>()
    scenario.moveToState(Lifecycle.State.CREATED)
//    intended(hasComponent(HomeActivity::class.java.getName()))
  }


//  @get:Rule
//  var mActivityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(SplashActivity::class.java)
//
//  @Rule
//  var mActivityRule: IntentsTestRule<SplashActivity> = IntentsTestRule(
//    SplashActivity::class.java
//  )
//  var mainActivity: SplashActivity? = null
//  var mainActivityFragment: SplashFragment? = null
//
//  @Before
//  fun setUp() {
//    mainActivity = mActivityTestRule.getActivity();
//    mainActivityFragment = SplashFragment()
//    startFragment(mainActivityFragment!!)
//  }
//
//  @Test
//  fun testMainActivity() {
//    Assert.assertNotNull(mainActivity)
//  }
//
//
//  private fun startFragment(fragment: Fragment) {
//    val fragmentManager = mainActivity?.getSupportFragmentManager()
//    val fragmentTransaction = fragmentManager?.beginTransaction()
//    fragmentTransaction?.add(fragment, null)
//    fragmentTransaction?.commit()
//
//    intended(hasComponent(HomeActivity::class.java.getName()))
//  }
}
