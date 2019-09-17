package org.oppia.app.splash

import androidx.fragment.app.Fragment
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.HomeActivity

/** Tests for [SplashActivity]. */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {

  @get:Rule
  var mActivityRule: IntentsTestRule<SplashActivity> = IntentsTestRule(
    SplashActivity::class.java
  )
  var mainActivity: SplashActivity? = null
  var mainActivityFragment: SplashFragment? = null

  @Before
  fun setUp() {
    mainActivity = mActivityRule.getActivity();
    mainActivityFragment = SplashFragment()
    startFragment(mainActivityFragment!!)
  }

  @Test
  fun testMainActivity() {
    Assert.assertNotNull(mainActivity)
  }

  private fun startFragment(fragment: Fragment) {
    val fragmentManager = mainActivity?.getSupportFragmentManager()
    val fragmentTransaction = fragmentManager?.beginTransaction()
    fragmentTransaction?.add(fragment, null)
    fragmentTransaction?.commit()

    intended(hasComponent(HomeActivity::class.java.getName()))
  }

}
