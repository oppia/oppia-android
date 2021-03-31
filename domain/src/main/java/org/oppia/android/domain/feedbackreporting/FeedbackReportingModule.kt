package org.oppia.android.domain.feedbackreporting

import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

/** Qualifier for the current report schema supported by the Android app. */
@Qualifier
annotation class ReportSchemaVersion

/** Module to provide dependencies used in feedback reporting. */
@Module
class FeedbackReportingModule {
  // Provides the Feedback Reporting service implementation.
  @Provides
  @ReportSchemaVersion
  fun provideReportSchemaVersion(): Int = 1
}
