package org.oppia.app.p2p

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.net.wifi.p2p.WifiP2pManager
import android.widget.Toast

class WifiDirectBroadcastReceiver constructor(
  private val manager: WifiP2pManager,
  private val channel: WifiP2pManager.Channel,
  private val activity: WifiDirectDemoActivity
): BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
    when (intent?.action) {
      WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
        if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
          Toast.makeText(context, "Wifi is ON", Toast.LENGTH_SHORT).show()
        } else {
          Toast.makeText(context, "Wifi is OFF", Toast.LENGTH_SHORT).show()
        }
      }
      WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
        manager.requestPeers(channel, activity.peerListListener)
      }
      WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
        val networkInfo = intent.getParcelableExtra<NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)
        if(networkInfo.isConnected) {
          manager.requestConnectionInfo(channel, activity.connectionInfoListener)
        } else {
          activity.connectionStatusText.text = "Device Disconnected"
        }
      }
      WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {

      }
    }
  }
}