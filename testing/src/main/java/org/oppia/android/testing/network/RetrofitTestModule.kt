package org.oppia.android.testing.network

import com.google.common.base.Optional
import dagger.Module
import dagger.Provides
import org.oppia.android.data.backends.gae.OppiaRetrofit
import retrofit2.Retrofit
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import javax.inject.Singleton

/**
 * Dagger [Module] that provides specifically configured [MockRetrofit] instance to have zero
 * network failures to avoid unwanted test flakes. Tests may override this behavior if they
 * wish to test uncertain network conditions.
 *
 * Note that Retrofit may be absent from the builds in certain environments (such as KitKat).
 * Attempting to inject [MockRetrofit] will verify that Retrofit is present, or otherwise fail. Do
 * not inject [MockRetrofit] for tests where Retrofit is supposed to be absent.
 */
@Module
class RetrofitTestModule {
  @Provides
  @Singleton
  fun provideMockRetrofit(@OppiaRetrofit retrofit: Optional<Retrofit>): MockRetrofit {
    check(retrofit.isPresent) {
      "Expected Retrofit to be present in order to create a MockRetrofit"
    }
    val behavior = NetworkBehavior.create()
    behavior.setFailurePercent(0)
    return MockRetrofit.Builder(retrofit.get()).apply {
      networkBehavior(behavior)
    }.build()
  }
}
