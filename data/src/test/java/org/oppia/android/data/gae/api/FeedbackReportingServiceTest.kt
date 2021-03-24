package org.oppia.android.data.gae.api

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.ApiUtils
import org.oppia.android.testing.network.MockFeedbackReportingService
import org.oppia.android.data.gae.gae.NetworkInterceptor
import org.oppia.android.data.gae.gae.NetworkSettings
import org.oppia.android.data.gae.gae.api.FeedbackReportingService
import org.oppia.android.data.gae.gae.model.GaeFeedbackReport
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/** Test for [FeedbackReportingService] retrofit instance using a [MockFeedbackReportingService]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FeedbackReportingServiceTest {
  private lateinit var mockRetrofit: MockRetrofit
  private lateinit var retrofit: Retrofit

  @Before
  fun setUp() {
    val client = OkHttpClient.Builder()
    client.addInterceptor(NetworkInterceptor())

    retrofit = retrofit2.Retrofit.Builder()
      .baseUrl(NetworkSettings.getBaseUrl())
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client.build())
      .build()

    val behavior = NetworkBehavior.create()
    mockRetrofit = MockRetrofit.Builder(retrofit)
      .networkBehavior(behavior)
      .build()
  }

  @Test
  fun testFeedbackReportingService_postRequest_successfulResponseReceived() {
    val delegate = mockRetrofit.create(FeedbackReportingService::class.java)
    val mockService = MockFeedbackReportingService(delegate)
    val mockGaeFeedbackReport = createMockGaeFeedbackReport()

    val response = mockService.postFeedbackReport(mockGaeFeedbackReport).execute()

    // Service returns a Unit type so no information is contained in the response.
    assertThat(response.isSuccessful).isTrue()
  }

  private fun createMockGaeFeedbackReport(): GaeFeedbackReport {
    val feedbackReportJson = ApiUtils.getFakeJson("feedback_reporting.json")
    val moshi = Moshi.Builder().build()

    val adapter: JsonAdapter<GaeFeedbackReport> = moshi.adapter(GaeFeedbackReport::class.java)
    val mockGaeFeedbackReport = adapter.fromJson(feedbackReportJson)
    return mockGaeFeedbackReport!!
  }
}
