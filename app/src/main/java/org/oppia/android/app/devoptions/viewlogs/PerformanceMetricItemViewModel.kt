package org.oppia.android.app.devoptions.viewlogs

import org.oppia.android.app.R
import org.oppia.android.app.model.OppiaMetricLog
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.APK_SIZE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.CPU_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.LOGGABLEMETRICTYPE_NOT_SET
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.MEMORY_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.NETWORK_USAGE_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STARTUP_LATENCY_METRIC
import org.oppia.android.app.model.OppiaMetricLog.LoggableMetric.LoggableMetricTypeCase.STORAGE_USAGE_METRIC
import org.oppia.android.app.translation.AppLanguageResourceHandler
import org.oppia.android.app.viewmodel.ObservableViewModel

/** A performance metric rendered as a single RecyclerView row. */
class PerformanceMetricItemViewModel(
  val metric: OppiaMetricLog,
  private val resourceHandler: AppLanguageResourceHandler
) : ObservableViewModel() {
  fun timestampLabel(): String = resourceHandler.computeDateTimeString(metric.timestampMillis)
  fun priorityLabel(): String = metric.priority.name
  fun screenLabel(): String = metric.currentScreen.name
  fun metricLabel(): String = with(metric.loggableMetric) {
    when (loggableMetricTypeCase) {
      APK_SIZE_METRIC -> formatMetric(
        R.string.performance_metric_apk_size, apkSizeMetric.apkSizeBytes.toString()
      )
      STORAGE_USAGE_METRIC -> formatMetric(
        R.string.performance_metric_storage_usage, storageUsageMetric.storageUsageBytes.toString()
      )
      STARTUP_LATENCY_METRIC -> formatMetric(
        R.string.performance_metric_startup_latency,
        startupLatencyMetric.startupLatencyMillis.toString()
      )
      MEMORY_USAGE_METRIC -> formatMetric(
        R.string.performance_metric_memory_usage, memoryUsageMetric.totalPssBytes.toString()
      )
      CPU_USAGE_METRIC -> formatMetric(
        R.string.performance_metric_cpu_usage, cpuUsageMetric.cpuUsageMetric.toString()
      )
      NETWORK_USAGE_METRIC -> formatMetric(
        R.string.performance_metric_network_usage,
        networkUsageMetric.bytesSent.toString(), networkUsageMetric.bytesReceived.toString()
      )
      null,
      LOGGABLEMETRICTYPE_NOT_SET -> resourceHandler.getStringInLocale(
        R.string.performance_metric_unavailable
      )
    }
  }
  fun contextLabel(): String = buildString {
    append("Foreground: ").append(metric.isAppInForeground)
    append(" · Memory: ").append(metric.memoryTier.name)
    append(" · Storage: ").append(metric.storageTier.name)
    append(" · Network: ").append(metric.networkType.name)
  }

  private fun formatMetric(stringId: Int, vararg values: CharSequence): String =
    resourceHandler.getStringInLocaleWithoutWrapping(stringId, *values)
}
