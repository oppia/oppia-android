package org.oppia.data.backends.test

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.data.backends.FakeJsonResponse
import org.oppia.data.backends.gae.NetworkInterceptor
import org.oppia.data.backends.gae.NetworkModule
import javax.inject.Singleton

/** Tests for [NetworkInterceptor] */
@RunWith(AndroidJUnit4::class)
class NetworkInterceptorTest {
  @Test
  fun testNetworkInterceptor_withXssiPrefix_removesXssiPrefix() {
    val networkInterceptor = NetworkInterceptor()
    val rawJson: String =
      networkInterceptor.removeXSSIPrefix(FakeJsonResponse.DUMMY_RESPONSE_WITH_XSSI_PREFIX)

    assertThat(rawJson).isEqualTo(FakeJsonResponse.DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX)
  }

  @Test
  fun testNetworkInterceptor_withoutXssiPrefix_removesXssiPrefix() {
    val networkInterceptor = NetworkInterceptor()
    val rawJson: String =
      networkInterceptor.removeXSSIPrefix(FakeJsonResponse.DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX)

    assertThat(rawJson).isEqualTo(FakeJsonResponse.DUMMY_RESPONSE_WITHOUT_XSSI_PREFIX)
  }

  private fun setUpTestApplicationComponent() {
    DaggerNetworkInterceptorTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

  @Singleton
  @Component(modules = [NetworkModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun getNetworkInterceptorTest(): NetworkInterceptorTest
  }
}
