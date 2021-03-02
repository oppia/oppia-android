package org.oppia.android.data.backends.test

import androidx.test.ext.junit.runners.AndroidJUnit4
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.model.AudioLanguage
import org.oppia.android.app.model.FeedbackReport
import org.oppia.android.app.model.FeedbackReportingAppContext.EntryPoint.NAVIGATION_DRAWER
import org.oppia.android.app.model.FeedbackReportingAppContext
import org.oppia.android.app.model.ReadingTextSize
import org.oppia.android.data.backends.api.MockFeedbackReportingService
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior

/** Test for [FeedbackReportingService] retrofit instance using a [MockFeedbackReportingService]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class MockFeedbackReportingTest {
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
  fun testFeedbackReportingService_singFakeJson_postSuccesfulResponseReceived() {
    val delegate = mockRetrofit.create(FeedbackReportingService::class.java)
    val mockService = MockFeedbackReportingService(delegate)

    val appContext = FeedbackReportingAppContext.newBuilder()
      .setEntryPoint(NAVIGATION_DRAWER)
      .setAudioLanguage(AudioLanguage.ENGLISH_AUDIO_LANGUAGE)
      .setTextLanguage()
      .setTextSize(ReadingTextSize.LARGE_TEXT_SIZE)
      .setDeviceSettings()
      .setIsAdmin(false)
    }
    val report = FeedbackReport.newBuilder().apply {
      reportCreationTimestampMs = 1610519337000;
      userSuppliedInfo = ,
      systemContext = {},
      deviceContext = {},
      appContext = appContext
    }
    val reponse = mockService.postFeedbackReport()
  }
}