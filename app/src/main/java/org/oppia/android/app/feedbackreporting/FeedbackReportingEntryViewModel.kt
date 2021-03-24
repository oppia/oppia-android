package org.oppia.android.app.feedbackreporting

import androidx.appcompat.app.AppCompatActivity
import org.oppia.android.app.viewmodel.ObservableViewModel
import javax.inject.Inject

/**
 * View model for the [FeedbackReportingEntryFragment] that handles when a user selectss a suggestion
 * or issue to submit feedback on.
 */
class FeedbackReportingEntryViewModel @Inject constructor(
  val activity: AppCompatActivity
) : ObservableViewModel() {

  /** Click handler for the "Suggestion" button, initializes the feedback report for submitting a suggestion. */
  fun onSuggestionButtonClicked() {
    // Start report for suggestion
  }

  /** Click handler for the "Suggestion" button, initializes the feedback report for submitting an issue. */
  fun onIssueButtonClicked() {
    // Start report for issue
  }
}
