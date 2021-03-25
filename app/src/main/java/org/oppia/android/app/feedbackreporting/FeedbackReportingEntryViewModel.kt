package org.oppia.android.app.feedbackreporting

import androidx.fragment.app.Fragment
import org.oppia.android.R
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/**
 * View model for the [FeedbackReportingEntryFragment] that handles when a user selectss a suggestion
 * or issue to submit feedback on.
 */
class FeedbackReportingEntryViewModel @Inject constructor(
  private val fragment: Fragment,
) : ObservableViewModel() {

  fun showPopupMenu() {
    val listener = fragment as FeedbackReportInteractionListener
    listener.onItemSelected(R.id.feedback_report_type_dropdown)
  }

  /** Click handler for the "Suggestion" button, initializes the feedback report for submitting a suggestion. */
  fun onSuggestionButtonClicked() {
    // Start report for suggestion
  }

  /** Click handler for the "Suggestion" button, initializes the feedback report for submitting an issue. */
  fun onIssueButtonClicked() {
    // Start report for issue
  }
}
