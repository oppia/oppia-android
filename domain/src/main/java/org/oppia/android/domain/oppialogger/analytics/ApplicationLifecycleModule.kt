package org.oppia.android.domain.oppialogger.analytics

import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet
import org.oppia.android.domain.oppialogger.ApplicationStartupListener
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

private const val SIXTY_MINUTES_IN_MILLIS = 60 * 60 * 1000L
private const val FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000L

@Qualifier annotation class ForegroundCpuLoggingTimePeriod

@Qualifier annotation class BackgroundCpuLoggingTimePeriod

/** Application-level module that provides application-bound domain utilities. */
@Module
class ApplicationLifecycleModule {
  @Provides
  @IntoSet
  fun bindLifecycleObserver(
    applicationLifecycleObserver: ApplicationLifecycleObserver
  ): ApplicationStartupListener = applicationLifecycleObserver

  @Provides
  @LearnerAnalyticsInactivityLimitMillis
  fun provideLearnerAnalyticsInactivityLimitMillis(): Long = TimeUnit.MINUTES.toMillis(30)

  @Provides
  @ForegroundCpuLoggingTimePeriod
  fun provideForegroundCpuLoggingTimePeriod(): Long = FIVE_MINUTES_IN_MILLIS

  @Provides
  @BackgroundCpuLoggingTimePeriod
  fun provideBackgroundCpuLoggingTimePeriod(): Long = SIXTY_MINUTES_IN_MILLIS
}
