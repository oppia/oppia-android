package org.oppia.android.data.backends.gae.api

import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body

/* Service that pushes data to access to feedback-reporting remote storage. */
interface FeedbackReportingService {
  @POST("feedbackreporting/incomingreport")
  fun postFeedbackReport(@Body report: GaeFeedbackReport)
  : Call<GaeFeedbackReport>
}