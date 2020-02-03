package org.oppia.app.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.home.HomeActivityPresenter
import org.oppia.app.home.KEY_HOME_PROFILE_ID
import org.oppia.app.home.RouteToTopicListener
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

class NavigationDrawerTestActivity : InjectableAppCompatActivity(), RouteToTopicListener {
  @Inject lateinit var homeActivityPresenter: HomeActivityPresenter

  companion object {
    fun createNavigationDrawerTestActivity(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, NavigationDrawerTestActivity::class.java)
      intent.putExtra(KEY_HOME_PROFILE_ID, profileId)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    homeActivityPresenter.handleOnCreate()
    title = getString(R.string.menu_home)
  }

  override fun routeToTopic(topicId: String) {
    startActivity(TopicActivity.createTopicActivityIntent(this, topicId))
  }
}
