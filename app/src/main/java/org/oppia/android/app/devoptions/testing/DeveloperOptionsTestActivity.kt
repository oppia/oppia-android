package org.oppia.android.app.devoptions.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import org.oppia.android.app.devoptions.DeveloperOptionsActivity
import org.oppia.android.app.devoptions.DeveloperOptionsFragment
import org.oppia.android.app.devoptions.ForceCrashButtonClickListener
import org.oppia.android.app.devoptions.RouteToMarkChaptersCompletedListener
import org.oppia.android.app.devoptions.RouteToMarkStoriesCompletedListener
import org.oppia.android.app.devoptions.RouteToMarkTopicsCompletedListener
import org.oppia.android.app.devoptions.RouteToViewEventLogsListener
import org.oppia.android.app.devoptions.markchapterscompleted.MarkChaptersCompletedActivity
import org.oppia.android.app.devoptions.markstoriescompleted.MarkStoriesCompletedActivity
import org.oppia.android.app.devoptions.marktopicscompleted.MarkTopicsCompletedActivity
import org.oppia.android.app.devoptions.vieweventlogs.ViewEventLogsActivity

/** Activity for testing [DeveloperOptionsFragment]. */
class DeveloperOptionsTestActivity :
  InjectableAppCompatActivity(),
  ForceCrashButtonClickListener,
  RouteToMarkChaptersCompletedListener,
  RouteToMarkStoriesCompletedListener,
  RouteToMarkTopicsCompletedListener,
  RouteToViewEventLogsListener {

  private var internalProfileId = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    setContentView(R.layout.developer_options_activity)
    internalProfileId = intent.getIntExtra(DEVELOPER_OPTIONS_TEST_ACTIVITY_PROFILE_ID_KEY, -1)
    if (getDeveloperOptionsFragment() == null) {
      supportFragmentManager.beginTransaction().add(
        R.id.developer_options_fragment_placeholder,
        DeveloperOptionsFragment.newInstance()
      ).commitNow()
    }
  }

  private fun getDeveloperOptionsFragment(): DeveloperOptionsFragment? {
    return supportFragmentManager.findFragmentById(
      R.id.developer_options_fragment_placeholder
    ) as DeveloperOptionsFragment?
  }

  override fun routeToMarkChaptersCompleted() {
    startActivity(
      MarkChaptersCompletedActivity
        .createMarkChaptersCompletedIntent(this, internalProfileId)
    )
  }

  override fun routeToMarkStoriesCompleted() {
    startActivity(
      MarkStoriesCompletedActivity
        .createMarkStoriesCompletedIntent(this, internalProfileId)
    )
  }

  override fun routeToMarkTopicsCompleted() {
    startActivity(
      MarkTopicsCompletedActivity
        .createMarkTopicsCompletedIntent(this, internalProfileId)
    )
  }

  override fun routeToViewEventLogs() {
    startActivity(ViewEventLogsActivity.createViewEventLogsActivityIntent(this))
  }

  override fun forceCrash() {
    throw RuntimeException("Force crash occurred")
  }

  companion object {
    const val DEVELOPER_OPTIONS_TEST_ACTIVITY_PROFILE_ID_KEY =
      "DeveloperOptionsTestActivity.internal_profile_id"

    /** Returns [Intent] for [DeveloperOptionsTestActivity]. */
    fun createDeveloperOptionsTestIntent(context: Context, internalProfileId: Int): Intent {
      val intent = Intent(context, DeveloperOptionsActivity::class.java)
      intent.putExtra(DEVELOPER_OPTIONS_TEST_ACTIVITY_PROFILE_ID_KEY, internalProfileId)
      return intent
    }
  }
}
