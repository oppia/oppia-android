package org.oppia.android.util.logging

import org.oppia.android.app.model.PlatformParameter.SyncStatus
import javax.inject.Inject

/**
 *
 */
class FeatureFlagSyncStatusToIntegerConverter @Inject constructor() {
  /**
   * Converts a feature flag name to an integer.
   *
   * @param syncStatus The flag name to convert.
   * @return An integer representation of the event name.
   */
  fun convertToInteger(syncStatus: SyncStatus): Int {
    return when (syncStatus.toString()) {
      "SYNC_STATUS_UNSPECIFIED" -> 1
      "NOT_SYNCED_FROM_SERVER" -> 2
      "SYNCED_FROM_SERVER" -> 3
      else -> -1
    }
  }
}
