package org.oppia.android.util.networking

/** Debug-only utility to get and set the current connection status of the device. */
interface NetworkConnectionDebugUtil {

  /** Enum corresponding to default connection statuses for the device. */
  enum class DebugConnectionStatus : ConnectionStatus {
    /** Refers to the actual connection status of the device. */
    DEFAULT
  }

  /** Forces [connectionStatus] as the current connection status of the device. */
  fun setCurrentConnectionStatus(connectionStatus: ConnectionStatus)

  /**
   * Returns the [ConnectionStatus] indicating whether the connection status was forced or not. If
   * no connection is forced then it will return [DEFAULT] as the [ConnectionStatus].
   */
  fun getForcedConnectionStatus(): ConnectionStatus
}
