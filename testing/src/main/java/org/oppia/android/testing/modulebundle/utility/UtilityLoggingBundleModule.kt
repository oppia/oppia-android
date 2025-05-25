package org.oppia.android.testing.modulebundle.utility

import dagger.Module
import org.oppia.android.testing.LogReportingTestModule
import org.oppia.android.testing.logging.SyncStatusTestModule
import org.oppia.android.util.logging.LoggerModule
import org.oppia.android.util.logging.firebase.FirebaseLogUploaderModule
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsConfigurationsModule
import org.oppia.android.util.logging.performancemetrics.testing.PerformanceMetricsAssessorTestModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for console and analytics
 * logging at the utility layer level.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(
  includes = [
    FirebaseLogUploaderModule::class, LoggerModule::class, LogReportingTestModule::class,
    PerformanceMetricsAssessorTestModule::class,
    // TODO: Is PerformanceMetricsConfigurationsModule actually needed?
    PerformanceMetricsConfigurationsModule::class, SyncStatusTestModule::class
  ]
)
interface UtilityLoggingBundleModule
