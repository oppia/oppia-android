package org.oppia.android.util.logging

import javax.inject.Inject

/**
 *
 */
class FeatureFlagSyncStatusToIntegerConverter @Inject constructor() {
  /**
   * Converts a feature flag name to an integer.
   *
   * @param flagName The flag name to convert.
   * @return An integer representation of the event name.
   */
  fun convertToInteger(flagName: String): Int {
    return when (flagName) {
      "SYNC_STATUS_UNSPECIFIED" -> 1
      "NOT_SYNCED_FROM_SERVER" -> 2
      "SYNCED_FROM_SERVER" -> 3
      else -> -1
    }
  }
}
