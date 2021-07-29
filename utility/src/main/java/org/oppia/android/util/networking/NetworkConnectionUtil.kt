package org.oppia.android.util.networking

/** Utility to get the current connection status of the device. */
interface NetworkConnectionUtil {
  /** Enum to distinguish different connection statuses for the device. */
  enum class ConnectionStatus {
    /** Connected to WIFI or Ethernet. */
    LOCAL,

    /** Connected to Mobile or WiMax. */
    CELLULAR,

    /** Not connected to a network. */
    NONE
  }

  /** Returns a [ConnectionStatus] indicating the current connection status of the device. */
  fun getCurrentConnectionStatus(): ConnectionStatus
}
