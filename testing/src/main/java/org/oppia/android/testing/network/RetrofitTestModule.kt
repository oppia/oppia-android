package org.oppia.android.testing.network

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import javax.inject.Singleton

/**
 * Dagger [Module] that provides specifically configured [MockRetrofit] instance to have zero
 * network failures to avoid unwanted test flakes. Tests may override this behavior if they
 * wish to test uncertain network conditions.
 */
@Module
class RetrofitTestModule {
  @Provides
  @Singleton
  fun provideMockRetrofit(): MockRetrofit {
    val client = OkHttpClient.Builder()
    client.addInterceptor(NetworkInterceptor())

    val retrofit = Retrofit.Builder()
      .baseUrl(NetworkSettings.getBaseUrl())
      .addConverterFactory(MoshiConverterFactory.create())
      .client(client.build())
      .build()

    val behavior = NetworkBehavior.create()
    behavior.setFailurePercent(0)

    return MockRetrofit.Builder(retrofit)
      .networkBehavior(behavior)
      .build()
  }
}
