package org.oppia.app.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import org.oppia.app.R
import org.oppia.app.activity.InjectableAppCompatActivity
import org.oppia.app.drawer.KEY_NAVIGATION_PROFILE_ID
import org.oppia.app.profile.ProfileChooserActivity
import org.oppia.app.topic.TopicActivity
import javax.inject.Inject

/** The central activity for all users entering the app. */
class HomeActivity : InjectableAppCompatActivity(), RouteToTopicListener {
  @Inject lateinit var homeActivityPresenter: HomeActivityPresenter
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
    AlertDialog.Builder(this, R.style.AlertDialogTheme)
      .setMessage(R.string.home_activity_back_dialog_message)
      .setNegativeButton(R.string.home_activity_back_dialog_cancel) { dialog, _ ->
        dialog.dismiss()
      }
      .setPositiveButton(R.string.home_activity_back_dialog_exit) { _, _ ->
        // TODO(#322): Need to start intent for ProfileActivity to get update. Change to finish when live data bug is fixed.
        val intent = Intent(this, ProfileChooserActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
      }.create().show()
  }
}
