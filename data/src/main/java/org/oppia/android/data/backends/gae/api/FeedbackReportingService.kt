package org.oppia.android.data.backends.gae.api

import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body

/**
 * Service that posts data to feedback-reporting remote storage, receiving the data class
 * representing the report on success. */
interface FeedbackReportingService {
  @POST("feedbackreporting/incomingreport")
  fun postFeedbackReport(@Body report: FeedbackReport)
  : Call<GaeFeedbackReport>
}
