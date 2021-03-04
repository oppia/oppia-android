package org.oppia.android.data.backends.api

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.data.backends.ApiUtils
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import retrofit2.Call
import retrofit2.mock.BehaviorDelegate

/** Mock FeedbackReportingService to check that the service is properly requested. */
class MockFeedbackReportingService(
  private val delegate: BehaviorDelegate<FeedbackReportingService>
) : FeedbackReportingService {
  override fun postFeedbackReport(report: FeedbackReport): Call<Unit> {
    return delegate.returningResponse(kotlin.Unit).postFeedbackReport(report)
  }
}
