package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.domain.oppialogger.ApplicationStartupListener

interface AppHealthMetricController: ApplicationStartupListener {

  override fun onCreate() {}

  fun logHighPriorityEvent()

  fun logMediumPriorityEvent()

  fun logLowPriorityEvent()

  fun uploadOrCacheLog()

  fun cacheMetricLog()

  fun createMetricLog()

  fun removeFirstLog()

  fun getStore()
}