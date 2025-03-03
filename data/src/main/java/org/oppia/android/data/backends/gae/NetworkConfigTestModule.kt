package org.oppia.android.data.backends.gae

import dagger.Module
import dagger.Provides
import javax.inject.Singleton
import okhttp3.mockwebserver.MockWebServer

/** Provides test-only network-specific constants. */
@Module
class NetworkConfigTestModule {

  @Provides
  @Singleton
  fun provideMockWebServer(): MockWebServer = MockWebServer()

  @Provides
  @BaseUrl
  fun provideNetworkBaseUrl(mockWebServer: MockWebServer): String =
    mockWebServer.url("/").toUrl().toString()

  @Provides
  @XssiPrefix
  fun provideXssiPrefix(): String {
    return ")]}'"
  }
}
