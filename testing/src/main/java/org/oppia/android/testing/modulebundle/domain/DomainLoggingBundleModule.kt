package org.oppia.android.testing.modulebundle.domain

import dagger.Module
import org.oppia.android.domain.oppialogger.LogStorageModule
import org.oppia.android.domain.oppialogger.LoggingIdentifierModule
import org.oppia.android.domain.oppialogger.analytics.ApplicationLifecycleModule
import org.oppia.android.domain.oppialogger.analytics.CpuPerformanceSnapshotterModule
import org.oppia.android.domain.oppialogger.exceptions.UncaughtExceptionLoggerModule
import org.oppia.android.domain.oppialogger.logscheduler.MetricLogSchedulerModule
import org.oppia.android.domain.oppialogger.loguploader.LogReportWorkerModule
import org.oppia.android.testing.modulebundle.utility.UtilityLoggingBundleModule

/**
 * A Dagger bundle [Module] that includes all of the necessary modules for console and analytics
 * logging at the domain layer level.
 *
 * Note that there are no configuration varieties for these modules, so in most cases this module
 * can be included as-is.
 */
@Module(includes = [
  ApplicationLifecycleModule::class, CpuPerformanceSnapshotterModule::class,
  LoggingIdentifierModule::class, LogReportWorkerModule::class, LogStorageModule::class,
  MetricLogSchedulerModule::class, UncaughtExceptionLoggerModule::class,
  UtilityLoggingBundleModule::class
])
interface DomainLoggingBundleModule
