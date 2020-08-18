package org.oppia.data.backends.gae

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Module which provides all required dependencies about network
 *
 * Sample resource: https://github.com/gahfy/Feed-Me/tree/unitTests
 */
@Module
class NetworkModule {

  @Qualifier
  private annotation class OppiaRetrofit

  @Inject
  private lateinit var moshiProviderShim: MoshiProviderShim

  /**
   * Provides the Retrofit object.
   * @return the Retrofit object
   */
  @OppiaRetrofit
  @Provides
  @Singleton
  fun provideRetrofitInstance(): Retrofit {
    val client = OkHttpClient.Builder()
    client.addInterceptor(NetworkInterceptor())

    return retrofit2.Retrofit.Builder()
      .baseUrl(NetworkSettings.getBaseUrl())
      .addConverterFactory(moshiProviderShim.getConverterFactory())
      .client(client.build())
      .build()
  }
}
