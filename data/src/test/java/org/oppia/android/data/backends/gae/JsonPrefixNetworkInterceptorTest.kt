package org.oppia.android.data.backends.gae

import android.app.Application
import android.content.Context
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
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Singleton

/** Tests for [JsonPrefixNetworkInterceptor]. */
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class JsonPrefixNetworkInterceptorTest {
  @Inject lateinit var context: Context
  @Inject lateinit var jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor

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
    openAssetInputStream(filename).bufferedReader().use { it.readText() }

  private fun loadFormattedFakeJson(filename: String): String =
    formatJson(loadUnformattedFakeJson(filename))

  private fun openAssetInputStream(jsonPath: String) = context.assets.open("api_mocks/$jsonPath")

  private fun formatJson(rawJson: String): String = JSONObject(rawJson).toString()

  private fun setUpTestApplicationComponent() {
    DaggerJsonPrefixNetworkInterceptorTest_TestApplicationComponent.builder()
      .setApplication(ApplicationProvider.getApplicationContext())
      .build()
      .inject(this)
  }

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
  @Component(modules = [TestModule::class, NetworkConfigProdModule::class])
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
