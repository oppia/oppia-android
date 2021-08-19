package org.oppia.android.data.backends.gae

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.oppia.android.data.backends.gae.api.ClassroomService
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import org.oppia.android.data.backends.gae.api.TopicService
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/**
 * Module which provides all required dependencies about network
 *
 * Sample resource: https://github.com/gahfy/Feed-Me/tree/unitTests
 */
@Module
class NetworkModule {

  /**
   * Provides the Retrofit object.
   * @return the Retrofit object
   */
  @OppiaRetrofit
  @Provides
  @Singleton
  fun provideRetrofitInstance(
    jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor,
    remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor,
    @BaseUrl baseUrl: String
  ): Retrofit {
    val client = OkHttpClient.Builder()
      .addInterceptor(jsonPrefixNetworkInterceptor)
      .addInterceptor(remoteAuthNetworkInterceptor)
      .build()

    return Retrofit.Builder()
      .baseUrl(baseUrl)
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client)
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

  // Provides the Feedback Reporting service implementation.
  @Provides
  @Singleton
  fun provideFeedbackReportingService(@OppiaRetrofit retrofit: Retrofit): FeedbackReportingService {
    return retrofit.create(FeedbackReportingService::class.java)
  }

  /**
   * Provides the [PlatformParameterService] implementation.
   *
   * @param retrofit the Retrofit object used to instantiate the service
   * @return the [PlatformParameterService] implementation
   */
  @Provides
  @Singleton
  fun providePlatformParameterService(@OppiaRetrofit retrofit: Retrofit): PlatformParameterService {
    return retrofit.create(PlatformParameterService::class.java)
  }

  // Provides the API key to use in authenticating remote messages sent or received. This will be
  // replaced with a secret key in production.
  @Provides
  @NetworkApiKey
  fun provideNetworkApiKey(): String = ""
}
