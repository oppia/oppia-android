package org.oppia.android.data.backends.gae

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.testing.network.ApiMockLoader
import org.oppia.android.testing.network.MockTopicService
import org.robolectric.annotation.LooperMode
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [JsonPrefixNetworkInterceptorTest] */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class JsonPrefixNetworkInterceptorTest {

  @Inject
  lateinit var jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor

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
      jsonPrefixNetworkInterceptor.removeXssiPrefix(
        loadUnformattedFakeJson(
          "dummy_response_with_xssi_prefix.json"
        )
      ).trim()

    assertThat(removeSpaces(formatJson(rawJson))).isEqualTo(
      loadFormattedFakeJson(
        "dummy_response_without_xssi_prefix.json"
      )
    )
  }

  @Test
  fun testNetworkInterceptor_withoutXssiPrefix_removesXssiPrefix() {
    val rawJson: String =
      jsonPrefixNetworkInterceptor.removeXssiPrefix(
        loadUnformattedFakeJson(
          "dummy_response_without_xssi_prefix.json"
        )
      )

    assertThat(formatJson(rawJson)).isEqualTo(
      loadFormattedFakeJson(
        "dummy_response_without_xssi_prefix.json"
      )
    )
  }

  private fun loadUnformattedFakeJson(filename: String): String =
    ApiMockLoader.getFakeJson(filename)

  private fun loadFormattedFakeJson(filename: String): String =
    formatJson(loadUnformattedFakeJson(filename))

  private fun formatJson(rawJson: String): String = JSONObject(rawJson).toString()

  private fun setUpTestApplicationComponent() {
    DaggerJsonPrefixNetworkInterceptorTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

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
  @Component(modules = [NetworkModule::class, NetworkConfigModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder

      fun build(): TestApplicationComponent
    }

    fun inject(jsonPrefixNetworkInterceptorTest: JsonPrefixNetworkInterceptorTest)
  }
}
