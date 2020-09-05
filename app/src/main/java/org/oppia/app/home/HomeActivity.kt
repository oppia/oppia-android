package org.oppia.app.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.drawer.ExitProfileDialogFragment
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.drawer.TAG_SWITCH_PROFILE_DIALOG
import org.oppia.app.topic.TopicActivity
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
      supportFragmentManager.findFragmentByTag(TAG_SWITCH_PROFILE_DIALOG)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val dialogFragment = ExitProfileDialogFragment
      .newInstance(
        isFromNavigationDrawer = false,
        isAdministratorControlsSelected = false,
        lastCheckedItemId = -1
      )
    dialogFragment.showNow(supportFragmentManager, TAG_SWITCH_PROFILE_DIALOG)
  }
}
