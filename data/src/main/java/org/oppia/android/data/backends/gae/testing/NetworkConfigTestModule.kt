package org.oppia.android.data.backends.gae.testing

import dagger.Module
import dagger.Provides
import okhttp3.mockwebserver.MockWebServer
import org.oppia.android.data.backends.gae.BaseUrl
import org.oppia.android.data.backends.gae.NetworkApiKey
import org.oppia.android.data.backends.gae.XssiPrefix
import javax.inject.Singleton

/** Provides network-specific constants specifically for tests. */
@Module
class NetworkConfigTestModule {
  @Provides
  @Singleton
  fun provideMockWebServer(): MockWebServer = MockWebServer()

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
}
