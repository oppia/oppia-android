package org.oppia.data.backends.gae

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.oppia.data.backends.gae.api.ClassroomService
import org.oppia.data.backends.gae.api.TopicService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client.build())
      .build()
  }

  /**
   * Provides the Topic service implementation.
   * @param retrofit the Retrofit object used to instantiate the service
   * @return the Topic service implementation.
   */
  @Provides
  @Singleton
  fun provideTopicService(@OppiaRetrofit retrofit: Retrofit): TopicService {
    return retrofit.create(TopicService::class.java)
  }

  /**
   * Provides the Classroom service implementation.
   * @param retrofit the Retrofit object used to instantiate the service
   * @return the Classroom service implementation.
   */
  @Provides
  @Singleton
  fun provideClassroomService(@OppiaRetrofit retrofit: Retrofit): ClassroomService {
    return retrofit.create(ClassroomService::class.java)
  }
}
