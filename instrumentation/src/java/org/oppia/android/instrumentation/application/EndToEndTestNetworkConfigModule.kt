package org.oppia.android.instrumentation.application

import dagger.Module
import dagger.Provides
import org.oppia.android.data.backends.gae.BaseUrl
import org.oppia.android.data.backends.gae.XssiPrefix

/** Provides network-specific constants needed to configure end-to-end tests. */
@Module
class EndToEndTestNetworkConfigModule {

  /** Provides BaseUrl that connects to development server. */
  @Provides
  @BaseUrl
  fun provideNetworkBaseUrl(): String {
    return "http://localhost:8181"
  }

  /**
   * Prefix in Json response for extra layer of security in API calls
   * https://github.com/oppia/oppia/blob/8f9eed/feconf.py#L319
   * Remove this prefix from every Json response which is achieved in [NetworkInterceptor]
   */
  @Provides
  @XssiPrefix
  fun provideXssiPrefix(): String {
    return ")]}'"
  }
}
