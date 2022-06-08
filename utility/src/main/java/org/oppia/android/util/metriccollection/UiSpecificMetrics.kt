package org.oppia.android.util.metriccollection

import org.oppia.android.app.model.OppiaMetricLog

interface UiSpecificMetrics {
  fun getTotalPssUsed(): Long
  fun getDeviceMemoryTier(): OppiaMetricLog.MemoryTier
}
