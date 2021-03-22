package org.oppia.android.app.feedbackreporting

import org.oppia.android.app.activity.InjectableAppCompatActivity

/** The first activity displayed for feedback reporting. */
class FeedbackReportingEntryActivity : InjectableAppCompatActivity() {
  @Inject
  lateinit var feedbackReportingEntryActivityPresenter: FeedbackReportingEntryActivityPresenter

}