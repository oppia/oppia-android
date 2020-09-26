package org.oppia.android.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.home.HomeActivityPresenter
import org.oppia.android.app.home.RouteToTopicListener
import org.oppia.android.app.topic.TopicActivity
import javax.inject.Inject

class NavigationDrawerTestActivity : InjectableAppCompatActivity(), RouteToTopicListener {
  @Inject
  lateinit var homeActivityPresenter: HomeActivityPresenter
  private var internalProfileId: Int = -1

  companion object {
    fun createNavigationDrawerTestActivity(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, NavigationDrawerTestActivity::class.java)
      intent.putExtra(KEY_NAVIGATION_PROFILE_ID, profileId)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    internalProfileId = intent?.getIntExtra(KEY_NAVIGATION_PROFILE_ID, -1)!!
    homeActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_home)
  }

  override fun routeToTopic(internalProfileId: Int, topicId: String) {
    startActivity(TopicActivity.createTopicActivityIntent(this, internalProfileId, topicId))
  }
}
