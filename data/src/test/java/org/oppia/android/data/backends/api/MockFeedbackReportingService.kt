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

/** Mock FeedbackReportingService with dummy data from [feedback_reporting.json]. */
class MockFeedbackReportingService(
  private val delegate: BehaviorDelegate<FeedbackReportingService>
) : FeedbackReportingService {
  override fun postFeedbackReport(report: FeedbackReport): Call<GaeFeedbackReport> {
    val mockReport = createMockGaeFeedbackReport()
    return delegate.returningResponse(mockReport).postFeedbackReport(report)
  }

  // Creates a mock [GaeFeedbackReport] from dummy JSON data as a reponse to the
  // FeedbackReportinService post request.
  private fun createMockGaeFeedbackReport(): GaeFeedbackReport {
    val networkInterceptor = NetworkInterceptor()
    val reportResponseWithXssiPrefix =
      NetworkSettings.XSSI_PREFIX + ApiUtils.getFakeJson("feedback_reporting.json")
    val reportResponse = networkInterceptor.removeXSSIPrefix(reportResponseWithXssiPrefix)

    val moshi = Moshi.Builder().build()
    val adapter: JsonAdapter<GaeFeedbackReport> = moshi.adapter(GaeFeedbackReport::class.java)
    return adapter.fromJson(reportResponse)!!
  }
}
