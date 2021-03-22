package org.oppia.android.app.feedbackreporting

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.oppia.android.app.fragment.InjectableFragment
import javax.inject.Inject

/** First fragment in feedback reporting that prompts the user for a report type. */
class FeedbackReportingEntryFragment : InjectableFragment() {
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
}
