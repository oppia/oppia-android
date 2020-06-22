package org.oppia.data.backends.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.data.backends.ApiUtils
import org.oppia.data.backends.api.MockTopicService
import org.oppia.data.backends.gae.NetworkInterceptor
import org.oppia.data.backends.gae.NetworkModule
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [NetworkInterceptor] */
@RunWith(AndroidJUnit4::class)
class NetworkInterceptorTest {

  @Inject
  lateinit var networkInterceptor: NetworkInterceptor

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  private fun removeSpaces(raw: String): String {
    return raw.replace(" ", "")
  }

  @Test
  fun testNetworkInterceptor_withXssiPrefix_removesXssiPrefix() {
    val rawJson: String =
      networkInterceptor.removeXSSIPrefix(
        ApiUtils.getFakeJson(
          "dummy_response_with_xssi_prefix.json"
        )
      ).trim()

    assertThat(removeSpaces(rawJson)).isEqualTo(
      ApiUtils.getFakeJson(
        "dummy_response_without_xssi_prefix.json"
      )
    )
  }

  @Test
  fun testNetworkInterceptor_withoutXssiPrefix_removesXssiPrefix() {
    val rawJson: String =
      networkInterceptor.removeXSSIPrefix(
        ApiUtils.getFakeJson(
          "dummy_response_without_xssi_prefix.json"
        )
      )

    assertThat(rawJson).isEqualTo(
      ApiUtils.getFakeJson(
        "dummy_response_without_xssi_prefix.json"
      )
    )
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkInterceptorTest_TestApplicationComponent.builder()
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
    fun provideMockTopicService(@OppiaRetrofit retrofit: Retrofit): MockTopicService {
      return retrofit.create(MockTopicService::class.java)
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

    fun inject(networkInterceptorTest: NetworkInterceptorTest)
    fun inject(networkInterceptor: NetworkInterceptor)
  }
}
