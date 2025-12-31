package org.oppia.android.data.backends.gae

import dagger.Module
import dagger.Provides
import org.oppia.android.data.backends.gae.api.FeedbackReportingService
import org.oppia.android.data.backends.gae.api.PlatformParameterService
import retrofit2.Retrofit
import javax.inject.Singleton
import org.oppia.android.data.backends.gae.api.PlatformParameterDebugService

@Module
class RetrofitServiceDebugModule {
  @Provides
  @Singleton
  fun provideFeedbackReportingService(@OppiaRetrofit retrofit: Retrofit): FeedbackReportingService =
    retrofit.create(FeedbackReportingService::class.java)

  @Provides
  @Singleton
  fun providePlatformParameterDebugService(
    @OppiaRetrofit retrofit: Retrofit
  ): PlatformParameterDebugService = retrofit.create(PlatformParameterDebugService::class.java)

  @Provides
  fun providePlatformParameterService(
    platformParameterDebugService: PlatformParameterDebugService
  ): PlatformParameterService = platformParameterDebugService
}
