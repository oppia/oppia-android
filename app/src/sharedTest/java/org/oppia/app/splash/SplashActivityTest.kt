package org.oppia.app.splash

import android.app.PendingIntent.getActivity
import android.widget.FrameLayout
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.android.synthetic.main.activity_splash.view.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.app.HomeActivity
import org.oppia.app.R
import androidx.test.rule.ActivityTestRule



/** Tests for [SplashActivity]. */
@RunWith(AndroidJUnit4::class)
class SplashActivityTest {


  @get:Rule
  var activityTestRule: ActivityTestRule<SplashActivity> = ActivityTestRule(SplashActivity::class.java)


  private var splashActivity: SplashActivity? = null
  private var supportFM: FragmentTransaction? = null

  @Before
  fun setUp() {
    splashActivity = activityTestRule.getActivity()
    supportFM = splashActivity!!.supportFragmentManager.beginTransaction();


  }

  @Test
  fun testEvent() {

      supportFM!!.add(R.id.fragment_container, SplashFragment()).commitNow()

  }


}

