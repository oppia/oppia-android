package org.oppia.android.domain.feedbackreporting

import dagger.Module
import dagger.Provides

/** Module to provide dependencies used in feedback reporting. */
@Module
class FeedbackReportingModule {
  // Provides the Feedback Reporting service implementation.
  @Provides
  @ReportSchemaVersion
  fun provideReportSchemaVersion(): Int = 1
}
