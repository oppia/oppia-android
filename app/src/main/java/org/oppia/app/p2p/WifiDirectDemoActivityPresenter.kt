package org.oppia.app.p2p

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.AsyncTask
import android.os.Handler
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.WifiDirectDemoActivityBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject

const val MESSAGE_READ = 1

@ActivityScope
class WifiDirectDemoActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
){
  private lateinit var readMessageText: TextView
  lateinit var connectionStatusText: TextView
  private lateinit var listView: ListView
  private lateinit var wifiManager: WifiManager
  private lateinit var manager: WifiP2pManager
  private lateinit var channel: WifiP2pManager.Channel
  private lateinit var broadcastReceiver: WifiDirectBroadcastReceiver
  private lateinit var intentFilter: IntentFilter

  private val peers = mutableListOf<WifiP2pDevice>()

  private lateinit var sendReceive: SendReceive


  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<WifiDirectDemoActivityBinding>(
      activity,
      R.layout.wifi_direct_demo_activity
    )

    readMessageText = binding.readMessage
    connectionStatusText = binding.connectionStatus
    listView = binding.listView

    wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    manager = activity.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
    channel = manager.initialize(activity, activity.mainLooper, null)
    broadcastReceiver = WifiDirectBroadcastReceiver(manager, channel, activity as WifiDirectDemoActivity)

    intentFilter = IntentFilter()
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)


    binding.onOffButton.setOnClickListener {
      if (wifiManager.isWifiEnabled()) {
        binding.onOffButton.text = "OFF"
        wifiManager.setWifiEnabled(false)
      } else {
        binding.onOffButton.text = "ON"
        wifiManager.setWifiEnabled(true)
      }
    }

    binding.discoverButton.setOnClickListener {
      manager.discoverPeers(channel, object: WifiP2pManager.ActionListener {
        override fun onSuccess() {
          connectionStatusText.text = "Discovery Started"
        }

        override fun onFailure(p0: Int) {
          connectionStatusText.text = "Discovery Starting Failed"
        }
      })
    }

    binding.sendButton.setOnClickListener {
      val msg = binding.writeMessage.text.toString()
      AsyncTask.execute {
        sendReceive.write(msg.toByteArray())
      }
    }

    listView.setOnItemClickListener(object: AdapterView.OnItemClickListener{
      override fun onItemClick(p0: AdapterView<*>?, p1: View?, index: Int, p3: Long) {
        val device = peers[index]
        val config = WifiP2pConfig()
        config.deviceAddress = device.deviceAddress

        manager.connect(channel, config, object: WifiP2pManager.ActionListener {
          override fun onSuccess() {
            Toast.makeText(activity.applicationContext, "Connected to " + device.deviceName, Toast.LENGTH_SHORT).show()
          }

          override fun onFailure(p0: Int) {
            Toast.makeText(activity.applicationContext, "Not Connected", Toast.LENGTH_SHORT).show()
          }
        })
      }

    })
  }

  val peerListListener = WifiP2pManager.PeerListListener {
    if (it.deviceList != peers) {
      peers.clear()
      peers.addAll(it.deviceList)
      val names = peers.map {device -> device.deviceName}
      val adapter = ArrayAdapter<String>(activity.applicationContext, R.layout.dialog_select_device, names)
      listView.adapter = adapter
    }

    if (peers.size == 0) {
      Toast.makeText(activity.applicationContext, "No Device Found", Toast.LENGTH_SHORT).show()
      return@PeerListListener
    }
  }

  val connectionInfoListener = WifiP2pManager.ConnectionInfoListener {
    val groupOwnerAddress: InetAddress = it.groupOwnerAddress

    if (it.groupFormed && it.isGroupOwner) {
      connectionStatusText.text = "Host"
      ServerClass().start()
    } else if (it.groupFormed) {
      connectionStatusText.text = "Client"
      ClientClass(groupOwnerAddress).start()
    }
  }

  val handler = Handler(Handler.Callback {
    when(it.what) {
      MESSAGE_READ -> {
        val readBuff = it.obj as ByteArray
        val tempMsg = String(readBuff, 0, it.arg1)
        readMessageText.text = tempMsg
      }
    }
    true
  })

  fun handleOnResume() {
    activity.registerReceiver(broadcastReceiver, intentFilter)
  }

  fun handleOnPause() {
    activity.unregisterReceiver(broadcastReceiver)
  }

  inner class ServerClass : Thread() {
    private lateinit var socket: Socket
    private lateinit var serverSocket: ServerSocket

    override fun run() {
      super.run()
      try {
        serverSocket = ServerSocket(8888)
        socket = serverSocket.accept()
        sendReceive = SendReceive(socket)
        sendReceive.start()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  inner class ClientClass constructor(
    hostAddress: InetAddress
  ) : Thread() {
    private val socket = Socket()
    private var hostAdd = hostAddress.hostAddress

    override fun run() {
      super.run()
      try {
        socket.connect(InetSocketAddress(hostAdd, 8888),500)
        sendReceive = SendReceive(socket)
        sendReceive.start()
      } catch (e : IOException) {
        e.printStackTrace()
      }
    }
  }

  inner class SendReceive constructor(
    private val socket: Socket
  ) : Thread() {
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    init {
      try {
        inputStream = socket.getInputStream()
        outputStream = socket.getOutputStream()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }


    override fun run() {
      super.run()
      val buffer = ByteArray(1024)
      while (socket.isConnected) {
        var bytes = inputStream.read(buffer)
        if (bytes > 0) {
          handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget()
        }
      }
    }

    fun write(bytes: ByteArray) {
      try {
        outputStream.write(bytes)
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }
}