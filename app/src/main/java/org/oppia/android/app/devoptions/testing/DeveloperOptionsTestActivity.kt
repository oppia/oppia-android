package org.oppia.android.app.devoptions.testing

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.ActivityComponentImpl
import org.oppia.android.app.activity.InjectableAutoLocalizedAppCompatActivity
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
import org.oppia.android.app.model.ProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.decorateWithUserProfileId
import org.oppia.android.util.profile.CurrentUserProfileIdIntentDecorator.extractCurrentUserProfileId

/** Activity for testing [DeveloperOptionsFragment]. */
class DeveloperOptionsTestActivity :
  InjectableAutoLocalizedAppCompatActivity(),
  ForceCrashButtonClickListener,
  RouteToMarkChaptersCompletedListener,
  RouteToMarkStoriesCompletedListener,
  RouteToMarkTopicsCompletedListener,
  RouteToViewEventLogsListener {

  private var profileId: ProfileId = ProfileId.newBuilder().setLoggedOut(true).build()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    (activityComponent as ActivityComponentImpl).inject(this)
    setContentView(R.layout.developer_options_activity)

    profileId =
      intent?.extractCurrentUserProfileId() ?: ProfileId.newBuilder().setLoggedOut(true).build()

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
      MarkChaptersCompletedActivity.createMarkChaptersCompletedIntent(
        context = this, profileId.loggedInInternalProfileId, showConfirmationNotice = false
      )
    )
  }

  override fun routeToMarkStoriesCompleted() {
    startActivity(
      MarkStoriesCompletedActivity
        .createMarkStoriesCompletedIntent(this, profileId.loggedInInternalProfileId)
    )
  }

  override fun routeToMarkTopicsCompleted() {
    startActivity(
      MarkTopicsCompletedActivity
        .createMarkTopicsCompletedIntent(this, profileId.loggedInInternalProfileId)
    )
  }

  override fun routeToViewEventLogs() {
    startActivity(ViewEventLogsActivity.createViewEventLogsActivityIntent(this))
  }

  override fun forceCrash() {
    throw RuntimeException("Force crash occurred")
  }

  companion object {
    /** Returns [Intent] for [DeveloperOptionsTestActivity]. */
    fun createDeveloperOptionsTestIntent(context: Context, internalProfileId: Int): Intent {
      val profileId = ProfileId.newBuilder().setLoggedInInternalProfileId(internalProfileId).build()
      val intent = Intent(context, DeveloperOptionsActivity::class.java)
      intent.decorateWithUserProfileId(profileId)
      return intent
    }
  }
}
