package org.oppia.data.backends.gae

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
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
      .addConverterFactory(GsonConverterFactory.create())
      .client(client.build())
      .build()
  }

//TODO: Put TopicService and ClassroomService back!!
}
