package org.oppia.android.domain.oppialogger.analytics

import javax.inject.Qualifier

/**
 * Corresponds to an injectable application-level [Long] that corresponds to the number of
 * milliseconds in which the app needs to be in the background before the user is considered
 * 'inactive' from an analytics perspective.
 */
@Qualifier annotation class LearnerAnalyticsInactivityLimitMillis
