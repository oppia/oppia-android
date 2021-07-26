package org.oppia.android.instrumentation

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.oppia.android.data.backends.gae.JsonPrefixNetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkApiKey
import org.oppia.android.data.backends.gae.OppiaRetrofit
import org.oppia.android.data.backends.gae.RemoteAuthNetworkInterceptor
import org.oppia.android.data.backends.gae.api.ClassroomService
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
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
class TestNetworkModule {

  const val TEST_DEVELOPMENT_URL = "http://localhost:8181/"

  /**
   * Provides the Retrofit object.
   * @return the Retrofit object
   */
  @OppiaRetrofit
  @Provides
  @Singleton
  fun provideRetrofitInstance(
    jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor,
    remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor
  ): Retrofit {
    val client = OkHttpClient.Builder()
      .addInterceptor(jsonPrefixNetworkInterceptor)
      .addInterceptor(remoteAuthNetworkInterceptor)
      .build()

    return Retrofit.Builder()
      .baseUrl(TEST_DEVELOPMENT_URL)
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

  // Provides the API key to use in authenticating remote messages sent or received. This will be
  // replaced with a secret key in production.
  @Provides
  @NetworkApiKey
  fun provideNetworkApiKey(): String = ""
}
