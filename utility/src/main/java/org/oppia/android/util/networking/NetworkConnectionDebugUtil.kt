package org.oppia.android.util.networking

/** Debug-only utility to get and set the current connection status of the device. */
interface NetworkConnectionDebugUtil {

  /** Enum corresponding to default connection statuses for the device. */
  enum class ConnectionStatus : org.oppia.android.util.networking.ConnectionStatus {
    /** Refers to the actual connection status of the device. */
    DEFAULT
  }

  fun setCurrentConnectionStatus(
    connectionStatus: org.oppia.android.util.networking.ConnectionStatus
  )

  fun getForcedConnectionStatus(): org.oppia.android.util.networking.ConnectionStatus
}
