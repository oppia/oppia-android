package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.home.HomeFragment
import org.oppia.android.app.home.RouteToTopicListener

/**
 * Test Activity for testing view models on the [HomeFragment]
 * Must implement [RouteToTopicListener] so the test can access a [HomeFragmnet]
 */
class HomeFragmentTestActivity : RouteToTopicListener, InjectableAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    supportFragmentManager.beginTransaction().add(
      HomeFragment(),
      "home_fragment_test_activity"
    ).commitNow()
  }

  companion object {
    fun createHomeFragmentTestActivity(context: Context): Intent {
      val intent = Intent(context, HomeFragmentTestActivity::class.java)
      return intent
    }
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {}
}
