package org.oppia.android.app.feedbackreporting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import org.oppia.android.R
import org.oppia.android.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** The first activity displayed for feedback reporting. */
class FeedbackReportingEntryActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var feedbackReportingEntryActivityPresenter: FeedbackReportingEntryActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    val isFromNavigationDrawer = intent.getBooleanExtra(
      BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY,
      /* defaultValue= */ false
    )
    feedbackReportingEntryActivityPresenter.handleOnCreate(isFromNavigationDrawer)
    title = getString(R.string.menu_send_feedback)
  }

  companion object {
    // TODO(#1655): Re-restrict access to fields in tests post-Gradle.
    const val BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY =
      "BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY"

    fun createFeedbackReportingEntrytActivityIntent(
      context: Context,
      profileId: Int?,
      isFromNavigationDrawer: Boolean
    ): Intent {
      val intent = Intent(context, FeedbackReportingEntryActivity::class.java)
      intent.putExtra(BOOL_IS_FROM_NAVIGATION_DRAWER_EXTRA_KEY, isFromNavigationDrawer)
      return intent
    }
  }
}