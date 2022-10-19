package org.oppia.android.domain.oppialogger.analytics

import javax.inject.Qualifier

/**
 * Corresponds to an injectable application-level [Long] that corresponds to the number of
 * milliseconds after which the foregrounded app logs another CPU usage metric event.
 */
@Qualifier
annotation class ForegroundCpuLoggingTimePeriodMillis

/**
 * Corresponds to an injectable application-level [Long] that corresponds to the number of
 * milliseconds after which the backgrounded app logs another CPU usage metric event.
 */
@Qualifier
annotation class BackgroundCpuLoggingTimePeriodMillis
