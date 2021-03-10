package org.oppia.android.domain.feedbackreporting

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

/**
 * Corresponds to an API key specific to Oppia Android for the backend to verify the origin of
 * requests sent from the app.
 */
@Qualifier
annotation class NetworkApiKey

/**
 * Module that provides constants needed in authentication when sending and receiving messages over
 * the network
 */
@Module
class NetworkAuthModule {
  /**
   * Provides the API key to validate messages received and messages being sent. A stronger, secret
   * key will be used when putting app out in production.
   */
  @Provides
  @NetworkApiKey
  fun provideNetworkApiKey(): String = ""
}
