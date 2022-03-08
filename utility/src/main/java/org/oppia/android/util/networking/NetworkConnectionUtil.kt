package org.oppia.android.util.networking

import androidx.annotation.StringRes
import org.oppia.android.util.R

/** Utility to get the current connection status of the device. */
interface NetworkConnectionUtil {

  /** Enum to distinguish different connection statuses for the device. */
  enum class ProdConnectionStatus(@StringRes var connectionName: Int) : ConnectionStatus {
    /** Connected to WIFI or Ethernet. */
    LOCAL(connectionName = R.string.network_connection_local),

    /** Connected to Mobile or WiMax. */
    CELLULAR(connectionName = R.string.network_connection_cellular),

    /** Not connected to a network. */
    NONE(connectionName = R.string.network_connection_none)
  }

  /** Returns a [ProdConnectionStatus] indicating the current connection status of the device. */
  fun getCurrentConnectionStatus(): ConnectionStatus
}
