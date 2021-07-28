package org.oppia.android.testing.network

import dagger.Module
import dagger.Provides
import org.oppia.android.data.backends.gae.OppiaRetrofit
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import retrofit2.Retrofit
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
  fun provideMockRetrofit(@OppiaRetrofit retrofit: Retrofit): MockRetrofit {
    val behavior = NetworkBehavior.create()
    behavior.setFailurePercent(0)

    return MockRetrofit.Builder(retrofit).apply {
      networkBehavior(behavior)
    }.build()
  }
  @Provides
//    @PlatformParameterServiceTest.MockPlatformParameterService
  fun provideMockPlatformParameterService(mockRetrofit: MockRetrofit): PlatformParameterService {
    val delegate = mockRetrofit.create(PlatformParameterService::class.java)
    return MockPlatformParameterService(delegate)
  }
}
