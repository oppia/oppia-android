package org.oppia.android.data.backends.gae

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

/** Module which provides [Retrofit] (qualified by [OppiaRetrofit]). */
@Module
class RetrofitModule {
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
}
