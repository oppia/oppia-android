package org.oppia.android.data.backends.gae

import dagger.Module
import dagger.Provides

/** Provides network-specific constants. */
@Module
class NetworkConfigProdModule {

  /** Provides BaseUrl that connects to production server. */
  @Provides
  @BaseUrl
  fun provideNetworkBaseUrl(): String {
    return "https://oppia.org"
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
