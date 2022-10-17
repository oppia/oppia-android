package org.oppia.android.domain.oppialogger.analytics

import javax.inject.Qualifier

/**
 * Corresponds to an injectable application-level [Long] that corresponds to the number of
 * milliseconds in which the foregrounded app logs another cpu usage metric event.
 */
@Qualifier
annotation class ForegroundCpuLoggingTimePeriodMillis

/**
 * Corresponds to an injectable application-level [Long] that corresponds to the number of
 * milliseconds in which the backgrounded app logs another cpu usage metric event.
 */
@Qualifier
annotation class BackgroundCpuLoggingTimePeriodMillis
