package org.oppia.android.app.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.drawer.ExitProfileDialogFragment
import org.oppia.android.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.android.app.drawer.SWITCH_PROFILE_DIALOG_ARGUMENT_KEY
import org.oppia.android.app.topic.TopicActivity
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity : InjectableAppCompatActivity(), RouteToTopicListener {
  @Inject
  lateinit var homeActivityPresenter: HomeActivityPresenter
  private var internalProfileId: Int = -1

  companion object {
    fun createHomeActivity(context: Context, profileId: Int?): Intent {
      val intent = Intent(context, HomeActivity::class.java)
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

  override fun onBackPressed() {
    val previousFragment =
      supportFragmentManager.findFragmentByTag(SWITCH_PROFILE_DIALOG_ARGUMENT_KEY)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = ExitProfileDialogFragment
      .newInstance(isFromNavigationDrawer = false)
    dialogFragment.showNow(supportFragmentManager, SWITCH_PROFILE_DIALOG_ARGUMENT_KEY)
  }
}
