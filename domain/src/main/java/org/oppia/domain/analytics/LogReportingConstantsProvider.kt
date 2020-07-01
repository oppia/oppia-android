package org.oppia.domain.analytics

import javax.inject.Inject
import javax.inject.Singleton

const val LOG_REPORTING_CACHE_SIZE = 10 // Represents 10 MB.

/** Provider to return any constants required during the storage of log reports. */
@Singleton
class LogReportingConstantsProvider @Inject constructor() {

  fun getLogReportingCacheSize(): Int = LOG_REPORTING_CACHE_SIZE
}
