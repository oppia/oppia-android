package org.oppia.android.data.backends.gae

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
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
  @OppiaRetrofit
  @Provides
  @Singleton
  fun provideRetrofitInstance(
    remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor,
    networkLoggingInterceptor: NetworkLoggingInterceptor,
    jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor,
    @BaseUrl baseUrl: String
  ): Retrofit {
    return Retrofit.Builder().apply {
      baseUrl(baseUrl)
      addConverterFactory(MoshiConverterFactory.create())
      client(
        OkHttpClient.Builder().apply {
          // This is in a specific order. The auth modifies a request, so it happens first. The
          // prefix remover executes other interceptors before changing the response, so it's
          // registered last so that the network logging interceptor receives a response with the
          // XSSI prefix correctly removed.
          addInterceptor(remoteAuthNetworkInterceptor)
          addInterceptor(networkLoggingInterceptor)
          addInterceptor(jsonPrefixNetworkInterceptor)
        }.build()
      )
    }.build()
  }

  @Provides
  @Singleton
  fun provideFeedbackReportingService(
    @OppiaRetrofit retrofit: Retrofit
  ): FeedbackReportingService {
    return retrofit.create(FeedbackReportingService::class.java)
  }

  @Provides
  @Singleton
  fun providePlatformParameterService(
    @OppiaRetrofit retrofit: Retrofit
  ): PlatformParameterService {
    return retrofit.create(PlatformParameterService::class.java)
  }

  // Provides the API key to use in authenticating remote messages sent or received. This will be
  // replaced with a secret key in production builds.
  @Provides
  @NetworkApiKey
  fun provideNetworkApiKey(): String = ""
}
