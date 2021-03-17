package org.oppia.android.testing.network

import okhttp3.OkHttpClient
import org.oppia.android.data.backends.gae.NetworkInterceptor
import org.oppia.android.data.backends.gae.NetworkSettings
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import javax.inject.Inject

/**
 * This helper allows us to create customized [MockRetrofit] instances.
 */
class MockRetrofitHelper @Inject constructor() {

  /**
   * This method returns a MockRetrofit object with [NetworkBehavior] that has 0 failurePercent.
   */
  fun createMockRetrofit(): MockRetrofit {
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
