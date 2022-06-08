package org.oppia.android.util.metriccollection

import org.oppia.android.app.model.OppiaMetricLog

interface AppStartupMetrics {
  fun getApkSize(): Long
  fun getUsedStorage(): Long
  fun getDeviceStorageTier(): OppiaMetricLog.StorageTier
}
