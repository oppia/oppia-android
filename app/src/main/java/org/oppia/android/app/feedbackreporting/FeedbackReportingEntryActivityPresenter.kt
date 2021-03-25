package org.oppia.android.app.feedbackreporting

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import org.oppia.android.R
import org.oppia.android.app.drawer.NavigationDrawerFragment
import javax.inject.Inject

/** The presenter for the [FeedbackReportingEntryActivity]. */
class FeedbackReportingEntryActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  private lateinit var navigationDrawerFragment: NavigationDrawerFragment
  private lateinit var toolbar: Toolbar

  fun handleOnCreate(isFromNavigationDrawer: Boolean) {
    activity.setContentView(R.layout.feedback_reporting_entry_activity)
    setUpToolbar()
    if (isFromNavigationDrawer) {
      setUpNavigationDrawer()
    } else {
      activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
      toolbar.setNavigationOnClickListener {
        activity.finish()
      }
    }
    if (getFeedbackReportingEntryFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.feedback_reporting_entry_fragment_placeholder,
        FeedbackReportingEntryFragment()
      ).commitNow()
    }
  }

  private fun setUpToolbar() {
    toolbar = activity.findViewById<View>(R.id.feedback_reporting_entry_activity_toolbar) as Toolbar
    activity.setSupportActionBar(toolbar)
  }

  private fun setUpNavigationDrawer() {
    activity.supportActionBar!!.setDisplayShowHomeEnabled(true)
    navigationDrawerFragment = activity
      .supportFragmentManager
      .findFragmentById(
        R.id.feedback_reporting_entry_activity_fragment_navigation_drawer
      ) as NavigationDrawerFragment
    navigationDrawerFragment.setUpDrawer(
      activity.findViewById<View>(
        R.id.feedback_reporting_entry_activity_drawer_layout
      ) as DrawerLayout,
      toolbar, R.id.nav_send_feedback
    )
  }

  private fun getFeedbackReportingEntryFragment(): FeedbackReportingEntryFragment? {
    return activity.supportFragmentManager.findFragmentById(
      R.id.feedback_reporting_entry_fragment_placeholder
    ) as FeedbackReportingEntryFragment?
  }
}
