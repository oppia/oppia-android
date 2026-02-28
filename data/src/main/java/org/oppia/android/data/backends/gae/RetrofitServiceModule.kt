package org.oppia.android.data.backends.gae

import dagger.Module
import dagger.Provides
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterDebugService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import retrofit2.Retrofit
import javax.inject.Singleton

/** Module which provides Retrofit services. */
@Module
class RetrofitServiceModule {
  @Provides
  @Singleton
  fun provideFeedbackReportingService(@OppiaRetrofit retrofit: Retrofit): FeedbackReportingService =
    retrofit.create(FeedbackReportingService::class.java)

  // TODO: Move this to a separate RetrofitServiceDebugModule once debug deps don't affect all tests.
  @Provides
  @Singleton
  fun providePlatformParameterDebugService(
    @OppiaRetrofit retrofit: Retrofit
  ): PlatformParameterDebugService = retrofit.create(PlatformParameterDebugService::class.java)

  @Provides
  @Singleton
  fun providePlatformParameterService(@OppiaRetrofit retrofit: Retrofit): PlatformParameterService =
    retrofit.create(PlatformParameterService::class.java)
}
