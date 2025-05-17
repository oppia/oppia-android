package org.oppia.android.data.backends.gae.testing

import android.content.Context
import android.content.pm.PackageManager
import androidx.test.core.content.pm.ApplicationInfoBuilder
import androidx.test.core.content.pm.PackageInfoBuilder
import dagger.Module
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import org.oppia.android.data.backends.gae.BaseUrl
import org.oppia.android.data.backends.gae.NetworkApiKey
import org.oppia.android.data.backends.gae.XssiPrefix
import org.robolectric.Shadows
import javax.inject.Singleton

/** Provides network-specific constants specifically for tests. */
@Module
class NetworkConfigTestModule {
  @Provides
  @Singleton
  fun provideMockWebServer(context: Context): MockWebServer {
    return MockWebServer().also {
      // NOTE TO DEVELOPER: It's a bit odd to do Robolectric initialization in a Dagger module,
      // particularly for this case. However, MockWebServer is only ever realistically going to be
      // used for interacting with Retrofit, and Retrofit's setup requires the following to be
      // configured (due to RemoteAuthNetworkInterceptor).
      val existingPackageInfo =
        context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
      val packageManager = Shadows.shadowOf(context.packageManager)
      val applicationInfo =
        ApplicationInfoBuilder.newBuilder()
          .setPackageName(context.packageName)
          .build()
      val packageInfo =
        PackageInfoBuilder.newBuilder()
          .setPackageName(context.packageName)
          .setApplicationInfo(applicationInfo)
          .build()
      packageInfo.versionName = TEST_APP_VERSION_NAME
      @Suppress("DEPRECATION") // versionCode is needed to test production code.
      packageInfo.versionCode = TEST_APP_VERSION_CODE
      packageManager.installPackage(packageInfo)

      // Reinstall all activities that were previously registered (since installPackage above
      // overrides them).
      existingPackageInfo.activities.forEach(packageManager::addOrUpdateActivity)
    }
  }

  @Provides
  @BaseUrl
  @Singleton // It's expected that the URL won't change, but this ensures determinism if it does.
  fun provideNetworkBaseUrl(mockWebServer: MockWebServer): String =
    mockWebServer.url("/").toUrl().toString()

  @Provides
  @XssiPrefix
  fun provideXssiPrefix(): String = ")]}'"

  @Provides
  @NetworkApiKey
  fun provideNetworkApiKey(): String = "test_api_key"

  private companion object {
    private const val TEST_APP_VERSION_NAME = "oppia-android-test-0123456789"
    private const val TEST_APP_VERSION_CODE = 1
  }
}
