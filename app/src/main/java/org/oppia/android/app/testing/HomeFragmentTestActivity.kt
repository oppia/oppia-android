package org.oppia.android.app.testing

import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.HomeFragment

/** Test Activity for testing view models on the HomeFragment */
class HomeFragmentTestActivity : InjectableAppCompatActivity() {

  private val homeFragment = HomeFragment()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    supportFragmentManager.beginTransaction().add(
      HomeFragment(),
      "home_fragment_test_activity"
    ).commitNow()
  }

//  companion object {
//    fun createHomeFragmentTestActivity(context: Context): Intent {
//      val intent = Intent(context, HomeFragmentTestActivity::class.java)
//      return intent
//    }
//  }
}
