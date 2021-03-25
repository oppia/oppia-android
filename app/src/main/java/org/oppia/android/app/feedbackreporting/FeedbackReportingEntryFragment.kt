package org.oppia.android.app.feedbackreporting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.R
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** First fragment in feedback reporting that prompts the user for a report type. */
class FeedbackReportingEntryFragment : InjectableFragment(), FeedbackReportInteractionListener {
  @Inject
  lateinit var feedbackReportingEntryFragmentPresenter: FeedbackReportingEntryFragmentPresenter

  override fun onAttach(context: Context) {
    super.onAttach(context)
    fragmentComponent.inject(this)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return feedbackReportingEntryFragmentPresenter.handleCreateView(inflater, container)
  }

  override fun onItemSelected(itemId: Int) {
    when (itemId) {
      R.id.feedback_report_type_dropdown -> feedbackReportingEntryFragmentPresenter.showPopup()
    }
  }
}
