package org.oppia.android.domain.oppialogger.analytics

import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.Priority
import org.oppia.android.app.model.OppiaMetricLogs
import org.oppia.android.app.model.ScreenName
import org.oppia.android.data.persistence.PersistentCacheStore
import org.oppia.android.domain.oppialogger.PerformanceMetricsLogStorageCacheSize
import org.oppia.android.util.data.DataProvider
import org.oppia.android.util.logging.ConsoleLogger
import org.oppia.android.util.logging.ExceptionLogger
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsAssessor
import org.oppia.android.util.logging.performancemetrics.PerformanceMetricsEventLogger
import org.oppia.android.util.networking.ConnectionStatus
import org.oppia.android.util.networking.NetworkConnectionUtil
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import org.oppia.android.util.enumfilter.filterByEnumCondition

/**
 * Controller for handling performance metrics event logging.
 *
 * Callers should not use this class directly; instead, they should use ``PerformanceMetricsLogger``
 * which provides convenience log methods.
 */
@Singleton
class PerformanceMetricsController @Inject constructor(
  private val performanceMetricsAssessor: PerformanceMetricsAssessor,
  private val consoleLogger: ConsoleLogger,
  private val networkConnectionUtil: NetworkConnectionUtil,
  private val exceptionLogger: ExceptionLogger,
  private val performanceMetricsEventLogger: PerformanceMetricsEventLogger,
  cacheStoreFactory: PersistentCacheStore.Factory,
  @PerformanceMetricsLogStorageCacheSize private val metricLogStorageCacheSize: Int
) {

  private var isAppInForeground: Boolean = false

  private val metricLogStore =
    cacheStoreFactory.create("metric_logs", OppiaMetricLogs.getDefaultInstance())

  /**
   * Logs a performance metric occurring at [currentScreen] defined by [loggableMetric]
   * corresponding to a time [timestamp].
   *
   * This will schedule a background upload of the event if there's internet connectivity, otherwise
   * it will cache the event for a later upload.
   */
  fun logPerformanceMetricsEvent(
    timestamp: Long,
    currentScreen: ScreenName,
    loggableMetric: OppiaMetricLog.LoggableMetric,
    priority: Priority
  ) {
    uploadOrCacheLog(createMetricLog(timestamp, priority, currentScreen, loggableMetric))
  }

  /** Either uploads or caches [oppiaMetricLog] depending on current internet connectivity. */
  private fun uploadOrCacheLog(oppiaMetricLog: OppiaMetricLog) {
    when (networkConnectionUtil.getCurrentConnectionStatus()) {
      NetworkConnectionUtil.ProdConnectionStatus.NONE -> cacheMetricLog(oppiaMetricLog)
      else -> performanceMetricsEventLogger.logPerformanceMetric(oppiaMetricLog)
    }
  }

  /**
   * Adds a metric log to the storage.
   *
   * At first, it checks if the size of the store isn't exceeding [metricLogStorageCacheSize]. If
   * the limit is exceeded then the least recent event is removed from the [metricLogStore]. After
   * this, the [oppiaMetricLog] is added to the store.
   */
  private fun cacheMetricLog(oppiaMetricLog: OppiaMetricLog) {
    metricLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaMetricLogs ->
      val storeSize = oppiaMetricLogs.oppiaMetricLogList.size
      if (storeSize + 1 > metricLogStorageCacheSize) {
        val eventLogRemovalIndex = getLeastRecentMetricLogIndex(oppiaMetricLogs)
        if (eventLogRemovalIndex != null) {
          return@storeDataAsync oppiaMetricLogs.toBuilder()
            .removeOppiaMetricLog(eventLogRemovalIndex)
            .addOppiaMetricLog(oppiaMetricLog)
            .build()
        } else {
          val exception =
            IllegalStateException(
              "Least Recent Event index absent -- MetricLogStorageCacheSize is 0"
            )
          consoleLogger.e(
            "PerformanceMetricsController",
            "Failure while caching metric log.",
            exception
          )
          exceptionLogger.logException(exception)
        }
      }
      return@storeDataAsync oppiaMetricLogs.toBuilder().addOppiaMetricLog(oppiaMetricLog).build()
    }.invokeOnCompletion {
      it?.let { consoleLogger.e("PerformanceMetricsController", "Failed to store metric log.", it) }
    }
  }

  /** Returns a metric log containing relevant data for metric log reporting. */
  private fun createMetricLog(
    timestamp: Long,
    priority: Priority,
    currentScreen: ScreenName,
    loggableMetric: OppiaMetricLog.LoggableMetric
  ): OppiaMetricLog {
    return OppiaMetricLog.newBuilder().apply {
      this.timestampMillis = timestamp
      this.priority = priority
      this.currentScreen = currentScreen
      this.loggableMetric = loggableMetric
      this.isAppInForeground = this@PerformanceMetricsController.isAppInForeground
      this.storageTier = performanceMetricsAssessor.getDeviceStorageTier()
      this.memoryTier = performanceMetricsAssessor.getDeviceMemoryTier()
      this.networkType = networkConnectionUtil.getCurrentConnectionStatus().toNetworkType()
    }.build()
  }

  /**
   * Returns the index of the least recent event from the existing store on the basis of recency and
   * priority.
   *
   * At first, it checks the index of the least recent event which has LOW priority. If that
   * returns null, then it checks the index of the least recent event which has MEDIUM priority. If
   * that returns null, then it checks the index of the least recent event regardless of the
   * priority is returned.
   */
  private fun getLeastRecentMetricLogIndex(oppiaMetricLogs: OppiaMetricLogs): Int? =
    filterByEnumCondition(
      oppiaMetricLogs.oppiaMetricLogList.withIndex().toList(),
      {it.value.priority},
      {it==Priority.LOW_PRIORITY}
    ).minByOrNull{it.value.timestampMillis}?.index
      ?:getLeastRecentMediumPriorityEventIndex(oppiaMetricLogs)

  /**
   * Returns the index of the least recent event from the existing store on the basis of recency and
   * priority.
   *
   * At first, it checks the index of the least recent event which has MEDIUM priority. If that
   * returns null, then it checks the index of the least recent event regardless of the
   * priority is returned.
   */
  private fun getLeastRecentMediumPriorityEventIndex(oppiaMetricLogs: OppiaMetricLogs): Int? =
    filterByEnumCondition(
      oppiaMetricLogs.oppiaMetricLogList.withIndex().toList(),
      {it.value.priority},
      {it==Priority.MEDIUM_PRIORITY}
    ).minByOrNull{it.value.timestampMillis}?.index
      ?:getLeastRecentGeneralEventIndex(oppiaMetricLogs)


  /** Returns the index of the least recent event regardless of their priority. */
  private fun getLeastRecentGeneralEventIndex(oppiaMetricLogs: OppiaMetricLogs): Int? =
    oppiaMetricLogs.oppiaMetricLogList.withIndex()
      .minByOrNull { it.value.timestampMillis }?.index

  /** Returns a data provider for log reports that have been recorded for upload. */
  fun getMetricLogStore(): DataProvider<OppiaMetricLogs> = metricLogStore

  /**
   * Returns a list of metric log reports that have been recorded for upload.
   *
   * As we are using the await call on the deferred output of readDataAsync, the failure case would
   * be caught and it'll throw an error.
   */
  suspend fun getMetricLogStoreList(): List<OppiaMetricLog> {
    return metricLogStore.readDataAsync().await().oppiaMetricLogList
  }

  /** Removes the first metric log report that had been recorded for upload. */
  fun removeFirstMetricLogFromStore() {
    metricLogStore.storeDataAsync(updateInMemoryCache = true) { oppiaEventLogs ->
      return@storeDataAsync oppiaEventLogs.toBuilder().removeOppiaMetricLog(0).build()
    }.invokeOnCompletion {
      it?.let {
        consoleLogger.e(
          "PerformanceMetricsController",
          "Failed to remove metric log.",
          it
        )
      }
    }
  }

  /** Sets [isAppInForeground] to true when application is in or returns to foreground. */
  fun setAppInForeground() {
    this.isAppInForeground = true
  }

  /** Sets [isAppInForeground] to false when application goes to background. */
  fun setAppInBackground() {
    this.isAppInForeground = false
  }

  /** Returns a boolean value indicating whether the application is currently in foreground or not. */
  fun getIsAppInForeground() = this.isAppInForeground

  private fun ConnectionStatus.toNetworkType(): OppiaMetricLog.NetworkType {
    return when (this) {
      NetworkConnectionUtil.ProdConnectionStatus.NONE -> OppiaMetricLog.NetworkType.NONE
      NetworkConnectionUtil.ProdConnectionStatus.LOCAL -> OppiaMetricLog.NetworkType.WIFI
      NetworkConnectionUtil.ProdConnectionStatus.CELLULAR -> OppiaMetricLog.NetworkType.CELLULAR
      else -> OppiaMetricLog.NetworkType.UNRECOGNIZED
    }
  }
}
