package org.oppia.android.data.backends.gae

import android.annotation.SuppressLint
import android.os.Build
import com.google.common.base.Optional
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
  @SuppressLint("ObsoleteSdkInt") // AS warning is incorrect in this context.
  @OppiaRetrofit
  @Provides
  @Singleton
  fun provideRetrofitInstance(
    jsonPrefixNetworkInterceptor: JsonPrefixNetworkInterceptor,
    remoteAuthNetworkInterceptor: RemoteAuthNetworkInterceptor,
    @BaseUrl baseUrl: String
  ): Optional<Retrofit> {
    // TODO(#1720): Make this a compile-time dep once Hilt provides it as an option.
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      val client = OkHttpClient.Builder()
        .addInterceptor(jsonPrefixNetworkInterceptor)
        .addInterceptor(remoteAuthNetworkInterceptor)
        .build()

      Optional.of(
        Retrofit.Builder()
          .baseUrl(baseUrl)
          .addConverterFactory(MoshiConverterFactory.create())
          .client(client)
          .build()
      )
    } else Optional.absent()
  }

  @Provides
  @Singleton
  fun provideTopicService(@OppiaRetrofit retrofit: Optional<Retrofit>): Optional<TopicService> {
    return retrofit.map { it.create(TopicService::class.java) }
  }

  @Provides
  @Singleton
  fun provideClassroomService(
    @OppiaRetrofit retrofit: Optional<Retrofit>
  ): Optional<ClassroomService> {
    return retrofit.map { it.create(ClassroomService::class.java) }
  }

  @Provides
  @Singleton
  fun provideFeedbackReportingService(
    @OppiaRetrofit retrofit: Optional<Retrofit>
  ): Optional<FeedbackReportingService> {
    return retrofit.map { it.create(FeedbackReportingService::class.java) }
  }

  @Provides
  @Singleton
  fun providePlatformParameterService(
    @OppiaRetrofit retrofit: Optional<Retrofit>
  ): Optional<PlatformParameterService> {
    return retrofit.map { it.create(PlatformParameterService::class.java) }
  }

  // Provides the API key to use in authenticating remote messages sent or received. This will be
  // replaced with a secret key in production.
  @Provides
  @NetworkApiKey
  fun provideNetworkApiKey(): String = ""

  private companion object {
    private fun <T, V> Optional<T>.map(mapFunc: (T) -> V): Optional<V> =
      transform { mapFunc(checkNotNull(it)) } // Payload should never actually be null.
  }
}
