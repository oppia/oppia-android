package org.oppia.android.testing.network

import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/** Mock [FeedbackReportingService] to check that the service is properly requested. */
class MockFeedbackReportingService(
  private val delegate: BehaviorDelegate<FeedbackReportingService>
) : FeedbackReportingService {
  override fun postFeedbackReport(report: GaeFeedbackReport): Call<Unit> {
    return delegate.returningResponse(kotlin.Unit).postFeedbackReport(report)
  }
}
