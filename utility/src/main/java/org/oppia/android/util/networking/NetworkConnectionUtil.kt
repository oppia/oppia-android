package org.oppia.android.util.networking

/** Utility to get the current connection status of the device. */
interface NetworkConnectionUtil {

  /** Enum to distinguish different connection statuses for the device. */
  enum class ProdConnectionStatus(val logName: String) : ConnectionStatus {
    /** Connected to WIFI or Ethernet. */
    LOCAL(logName = "Local"),

    /** Connected to Mobile or WiMax. */
    CELLULAR(logName = "Cellular"),

    /** Not connected to a network. */
    NONE(logName = "None")
  }

  /** Returns a [ProdConnectionStatus] indicating the current connection status of the device. */
  fun getCurrentConnectionStatus(): ConnectionStatus
}
