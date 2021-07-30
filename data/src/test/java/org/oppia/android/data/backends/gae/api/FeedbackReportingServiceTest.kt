package org.oppia.android.data.backends.gae.api

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.data.backends.gae.model.GaeFeedbackReport
import org.oppia.android.testing.network.ApiMockLoader
import org.oppia.android.testing.network.MockFeedbackReportingService
import org.oppia.android.testing.network.RetrofitTestModule
import org.robolectric.annotation.LooperMode
import retrofit2.mock.MockRetrofit
import javax.inject.Inject
import javax.inject.Singleton

/** Test for [FeedbackReportingService] retrofit instance using a [MockFeedbackReportingService]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FeedbackReportingServiceTest {

  @Inject
  lateinit var mockRetrofit: MockRetrofit

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
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
    val feedbackReportJson = ApiMockLoader.getFakeJson("feedback_reporting.json")
    val moshi = Moshi.Builder().build()

    val adapter: JsonAdapter<GaeFeedbackReport> = moshi.adapter(GaeFeedbackReport::class.java)
    val mockGaeFeedbackReport = adapter.fromJson(feedbackReportJson)
    return mockGaeFeedbackReport!!
  }

  private fun setUpTestApplicationComponent() {
    DaggerFeedbackReportingServiceTest_TestApplicationComponent
      .builder()
      .setApplication(ApplicationProvider.getApplicationContext()).build().inject(this)
  }

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestModule {
    @Provides
    @Singleton
    fun provideContext(application: Application): Context {
      return application
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [TestModule::class, NetworkModule::class, RetrofitTestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(test: FeedbackReportingServiceTest)
  }
}
