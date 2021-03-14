package org.oppia.android.data.backends.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import okhttp3.Request
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.data.backends.api.MockFeedbackReportingService
import org.oppia.android.data.backends.gae.NetworkModule
import org.oppia.android.data.backends.gae.RemoteAuthNetworkInterceptor
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [RemoteAuthNetworkInterceptor] */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class RemoteAuthNetworkInterceptorTest {

  @Inject
  lateinit var networkInterceptor: RemoteAuthNetworkInterceptor

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testNetworkInterceptor_withoutHeader_addsHeader() {
    val requestBuilder = Request.Builder()
  }

  private fun setUpTestApplicationComponent() {
    DaggerRemoteAuthNetworkInterceptorTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Qualifier
  annotation class OppiaRetrofit

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestNetworkModule {
    @Provides
    @Singleton
    fun provideMockFeedbackReportingService(
      @OppiaRetrofit retrofit: Retrofit
    ): MockFeedbackReportingService {
      return retrofit.create(MockFeedbackReportingService::class.java)
    }
  }

  // TODO(#89): Move this to a common test application component.
  @Singleton
  @Component(modules = [NetworkModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(networkInterceptorTest: RemoteAuthNetworkInterceptorTest)
    fun inject(networkInterceptor: RemoteAuthNetworkInterceptor)
  }
}
