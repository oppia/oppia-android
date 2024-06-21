package org.oppia.android.app.classroom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
import org.oppia.android.app.activity.route.ActivityRouter
import org.oppia.android.app.drawer.ExitProfileDialogFragment
import org.oppia.android.app.drawer.NAVIGATION_PROFILE_ID_ARGUMENT_KEY
import org.oppia.android.app.drawer.TAG_SWITCH_PROFILE_DIALOG
import org.oppia.android.app.model.ExitProfileDialogArguments
import org.oppia.android.app.model.HighlightItem
import org.oppia.android.app.model.ScreenName.CLASSROOM_LIST_ACTIVITY
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.util.logging.CurrentAppScreenNameIntentDecorator.decorateWithScreenName
import javax.inject.Inject

/** The activity for displaying [ClassroomListFragment]. */
class ClassroomListActivity : InjectableAutoLocalizedAppCompatActivity() {
  @Inject
  lateinit var classroomListActivityPresenter: ClassroomListActivityPresenter

  @Inject
  lateinit var resourceHandler: AppLanguageResourceHandler

  @Inject
  lateinit var activityRouter: ActivityRouter

  companion object {
    /** Returns a new [Intent] to route to [ClassroomListActivity] for a specified [profileId]. */
    fun createClassroomListActivity(context: Context, profileId: Int?): Intent {
      return Intent(context, ClassroomListActivity::class.java).apply {
        putExtra(NAVIGATION_PROFILE_ID_ARGUMENT_KEY, profileId)
        decorateWithScreenName(CLASSROOM_LIST_ACTIVITY)
      }
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    classroomListActivityPresenter.handleOnCreate()
    title = resourceHandler.getStringInLocale(R.string.classroom_list_activity_title)
  }

  override fun onRestart() {
    super.onRestart()
    classroomListActivityPresenter.handleOnRestart()
  }

  override fun onBackPressed() {
    val previousFragment =
      supportFragmentManager.findFragmentByTag(TAG_SWITCH_PROFILE_DIALOG)
    if (previousFragment != null) {
      supportFragmentManager.beginTransaction().remove(previousFragment).commitNow()
    }
    val exitProfileDialogArguments =
      ExitProfileDialogArguments
        .newBuilder()
        .setHighlightItem(HighlightItem.NONE)
        .build()
    val dialogFragment = ExitProfileDialogFragment
      .newInstance(exitProfileDialogArguments = exitProfileDialogArguments)
    dialogFragment.showNow(supportFragmentManager, TAG_SWITCH_PROFILE_DIALOG)
  }
}
