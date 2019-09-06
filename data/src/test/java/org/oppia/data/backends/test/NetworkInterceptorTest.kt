package org.oppia.data.backends.test

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.data.backends.FakeJsonResponse
import org.oppia.data.backends.api.MockTopicService
import org.oppia.data.backends.gae.NetworkInterceptor
import org.oppia.data.backends.gae.NetworkModule
import org.oppia.data.backends.gae.NetworkSettings
import org.oppia.data.persistence.DaggerPersistentCacheStoreTest_TestApplicationComponent
import org.oppia.data.persistence.PersistentCacheStoreTest
import org.oppia.util.threading.BackgroundDispatcher
import org.oppia.util.threading.BlockingDispatcher
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

/** Tests for [NetworkInterceptor] */
@RunWith(AndroidJUnit4::class)
class NetworkInterceptorTest {

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

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

  @Qualifier
  annotation class OppiaRetrofit

  // TODO(#89): Move this to a common test application component.
  @Module
  class TestNetworkModule {
    @OppiaRetrofit
    @Provides
    @Singleton
    fun provideRetrofitInstance(): Retrofit {
      val client = OkHttpClient.Builder()
      client.addInterceptor(NetworkInterceptor())

      return retrofit2.Retrofit.Builder()
        .baseUrl(NetworkSettings.getBaseUrl())
        .addConverterFactory(MoshiConverterFactory.create())
        .client(client.build())
        .build()
    }

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
  }
}
