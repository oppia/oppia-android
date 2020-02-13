package org.oppia.app.p2p

import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.widget.TextView
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class WifiDirectDemoActivity : InjectableAppCompatActivity() {
  @Inject lateinit var wifiDirectDemoActivityPresenter: WifiDirectDemoActivityPresenter
  lateinit var peerListListener: WifiP2pManager.PeerListListener
  lateinit var connectionInfoListener: WifiP2pManager.ConnectionInfoListener
  lateinit var connectionStatusText: TextView


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    wifiDirectDemoActivityPresenter.handleOnCreate()
    peerListListener = wifiDirectDemoActivityPresenter.peerListListener
    connectionInfoListener = wifiDirectDemoActivityPresenter.connectionInfoListener
    connectionStatusText = wifiDirectDemoActivityPresenter.connectionStatusText
  }

  override fun onResume() {
    super.onResume()
    wifiDirectDemoActivityPresenter.handleOnResume()
  }

  override fun onPause() {
    super.onPause()
    wifiDirectDemoActivityPresenter.handleOnPause()
  }

}