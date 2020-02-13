package org.oppia.app.p2p

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.BluetoothDemoActivityBinding
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.inject.Inject

val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
val devices = mutableListOf<BluetoothDevice>()


@ActivityScope
class BluetoothDemoActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
){
  private lateinit var receivedMessage: TextView
  private var devicesMap = mutableMapOf<String, BluetoothDevice>()
  private var arrayAdapter: ArrayAdapter<String>? = null
  private lateinit var sendReceive: SendReceive

  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<BluetoothDemoActivityBinding>(
      activity,
      R.layout.bluetooth_demo_activity
    )
    receivedMessage = binding.receivedMessage
    arrayAdapter = ArrayAdapter(activity, R.layout.dialog_select_device)
    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    activity.registerReceiver(bluetoothReceiver, filter)
    binding.findDevicesButton.setOnClickListener {
      if (BluetoothAdapter.getDefaultAdapter() == null) {
        Toast.makeText(activity, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
      } else {
        devicesMap.clear()
        devices.clear()
        arrayAdapter?.clear()

        for (device in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
          devicesMap[device.address] = device
          devices.add(device)
          arrayAdapter?.add(device.name + " " + device.address + " Paired")
        }
        BluetoothAdapter.getDefaultAdapter().startDiscovery()
        binding.listView.adapter = arrayAdapter
      }
    }

    binding.listView.setOnItemClickListener { adapterView, view, i, l ->
      BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
      BluetoothClient(devices[i]).start()
    }

    binding.sendMessageButton.setOnClickListener {
      val message = binding.inputText.text.toString()
      sendReceive.write(message.toByteArray())
    }
    BluetoothServer().start()
  }

  fun handleOnDestroy() {
    activity.unregisterReceiver(bluetoothReceiver)
  }

  fun appendText(text: String) {
    activity.runOnUiThread {
      Log.e("James", text)

    }
  }

  val handler = Handler(Handler.Callback {
    when(it.what) {
      MESSAGE_READ -> {
        val readBuff = it.obj as ByteArray
        val tempMsg = String(readBuff, 0, it.arg1)
        receivedMessage.text = receivedMessage.text.toString() + "\n" + tempMsg
      }
    }
    true
  })


  private val bluetoothReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == BluetoothDevice.ACTION_FOUND) {
        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        val pairedDevice = devicesMap[device.address]
        if (pairedDevice == null) {
          devices.add(device)
          arrayAdapter?.add(device.name + " " + device.address)
          arrayAdapter?.notifyDataSetChanged()
        }
      }
    }
  }


  inner class BluetoothServer : Thread() {
    private var cancelled = false
    private var serverSocket: BluetoothServerSocket? = null

    init {
      val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
      if (bluetoothAdapter != null) {
        serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("test", uuid)
        cancelled = false
      } else {
        serverSocket = null
        cancelled = true
      }
    }

    override fun run() {
      var socket: BluetoothSocket
      while (true) {
        if (cancelled) break
        try {
          socket = serverSocket!!.accept()
        } catch (e: IOException) {
          break
        }
        if (!cancelled && socket != null) {
          sendReceive = SendReceive(socket)
          sendReceive.start()
        }
      }
    }
  }

  inner class BluetoothClient(device: BluetoothDevice): Thread() {
    private val socket = device.createRfcommSocketToServiceRecord(uuid)

    override fun run() {
      socket.connect()
      sendReceive = SendReceive(socket)
      sendReceive.start()
    }
  }

  inner class SendReceive constructor(
    private val socket: BluetoothSocket
  ) : Thread() {
    private lateinit var inputStream: InputStream
    private lateinit var outputStream: OutputStream

    init {
      try {
        inputStream = socket.inputStream
        outputStream = socket.outputStream
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