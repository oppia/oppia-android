package org.oppia.android.data.backends.gae.testing

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.MATCH_ALL
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.oppia.android.app.testing.activity.TestActivity
import org.oppia.android.data.backends.gae.BaseUrl
import org.oppia.android.data.backends.gae.NetworkApiKey
import org.oppia.android.data.backends.gae.XssiPrefix
import org.oppia.android.testing.assertThrows
import org.oppia.android.util.extensions.getVersionCode
import org.oppia.android.util.extensions.getVersionName
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

/** Tests for [NetworkConfigTestModule]. */
// Function name: test names are conventionally named with underscores.
@Suppress("FunctionName")
@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@Config(application = NetworkConfigTestModuleTest.TestApplication::class)
class NetworkConfigTestModuleTest {
  @Inject lateinit var context: Context
  @Inject lateinit var mockWebServerProvider: Provider<MockWebServer>
  @field:[Inject BaseUrl] lateinit var baseUrlProvider: Provider<String>
  @field:[Inject XssiPrefix] lateinit var xssiPrefixProvider: Provider<String>
  @field:[Inject NetworkApiKey] lateinit var networkApiKeyProvider: Provider<String>

  @Before
  fun setUp() {
    setUpTestApplicationComponent()
  }

  @Test
  fun testModule_providesWorkingMockWebServer() {
    val mockWebServer = mockWebServerProvider.get()
    mockWebServer.enqueue(MockResponse())

    // No exception should be thrown injecting & using the mock web server.
  }

  @Test
  fun testModule_withoutInjectingMockWebServer_doesNotSetAppTestVersionCode() {
    val versionCode = context.getVersionCode()

    assertThat(versionCode).isEqualTo(0)
  }

  @Test
  fun testModule_withoutInjectingMockWebServer_doesNotSetAppTestVersionName() {
    // NPE is thrown because versionName is unexpectedly null (which it never is in practice) due to
    // it not being initialized. The module will initialize it if MockWebServer is injected.
    assertThrows<NullPointerException> { context.getVersionName() }
  }

  @Test
  fun testModule_doesNotOverrideAlreadyRegisteredActivity() {
    TestActivity.registerWithPackageManager<SampleTestActivity>(context)
    mockWebServerProvider.get() // Starting the server initializes the test package.

    val intent = Intent(context, SampleTestActivity::class.java)
    val activityInfos = context.packageManager.queryIntentActivities(intent, MATCH_ALL)
    assertThat(activityInfos).hasSize(1)
    assertThat(activityInfos.first().activityInfo.name)
      .isEqualTo(SampleTestActivity::class.java.name)
  }

  @Test
  fun testModule_setsAppTestVersionCode() {
    mockWebServerProvider.get() // Starting the server initializes the app's version code.

    val versionCode = context.getVersionCode()
    assertThat(versionCode).isEqualTo(1)
  }

  @Test
  fun testModule_setsAppTestVersionName() {
    mockWebServerProvider.get() // Starting the server initializes the app's version name.

    val versionName = context.getVersionName()
    assertThat(versionName).isEqualTo("oppia-android-test-0123456789")
  }

  @Test
  fun testModule_providesBaseUrl() {
    val baseUrl = baseUrlProvider.get()

    // Specifically verify that the URL does NOT contain oppia.org (unlike the prod module).
    assertThat(baseUrl).contains("localhost")
    assertThat(baseUrl).doesNotContain("oppia.org")
  }

  @Test
  fun testModule_providesXssiPrefix() {
    val xssiPrefix = xssiPrefixProvider.get()

    assertThat(xssiPrefix).isEqualTo(")]}'")
  }

  @Test
  fun testModule_providesTestNetworkApiKey() {
    val networkApiKey = networkApiKeyProvider.get()

    assertThat(networkApiKey).isEqualTo("test_api_key")
  }

  private fun setUpTestApplicationComponent() {
    ApplicationProvider.getApplicationContext<TestApplication>().inject(this)
  }

  private class SampleTestActivity : TestActivity()

  @Module
  interface TestModule {
    @Binds
    fun provideContext(application: Application): Context
  }

  @Singleton
  @Component(modules = [TestModule::class, NetworkConfigTestModule::class])
  interface TestApplicationComponent {
    @Component.Builder
    interface Builder {
      @BindsInstance
      fun setApplication(application: Application): Builder
      fun build(): TestApplicationComponent
    }

    fun inject(test: NetworkConfigTestModuleTest)
  }

  class TestApplication : Application() {
    private val component: TestApplicationComponent by lazy {
      DaggerNetworkConfigTestModuleTest_TestApplicationComponent.builder()
        .setApplication(this)
        .build()
    }

    fun inject(test: NetworkConfigTestModuleTest) = component.inject(test)
  }
}
