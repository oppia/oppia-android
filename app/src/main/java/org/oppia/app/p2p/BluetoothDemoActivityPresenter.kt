package org.oppia.app.p2p

import android.app.AlertDialog
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.BluetoothDemoActivityBinding
import java.io.IOException
import java.util.*
import javax.inject.Inject

val uuid: UUID = UUID.fromString("8989063a-c9af-463a-b3f1-f21d9b2b827b")
var message = ""
val devices = mutableListOf<BluetoothDevice>()


@ActivityScope
class BluetoothDemoActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
){
  private lateinit var receivedMessage: TextView
  private var devicesMap = mutableMapOf<String, BluetoothDevice>()
  private var arrayAdapter: ArrayAdapter<String>? = null


  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<BluetoothDemoActivityBinding>(
      activity,
      R.layout.bluetooth_demo_activity
    )
    receivedMessage = binding.receivedMessage
    arrayAdapter = ArrayAdapter(activity, R.layout.dialog_select_device)
    val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
    activity.registerReceiver(bluetoothReceiver, filter)
    binding.sendMessageButton.setOnClickListener {
      if (BluetoothAdapter.getDefaultAdapter() == null) {
        Toast.makeText(activity, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show()
      } else {
        devicesMap.clear()
        devices.clear()
        arrayAdapter?.clear()

        message = binding.inputText.text.toString()
        binding.inputText.text.clear()

        for (device in BluetoothAdapter.getDefaultAdapter().bondedDevices) {
          devicesMap[device.address] = device
          devices.add(device)
          arrayAdapter?.add(device.name + " " + device.address + " Paired")
        }

        if (BluetoothAdapter.getDefaultAdapter().startDiscovery()) {
          arrayAdapter?.let {
            val dialog = SelectDeviceDialog(it)
            dialog.show(activity.supportFragmentManager, "SELECT_DEVICE")
          }
        }
      }
    }
    BluetoothServerController(activity as BluetoothDemoActivity).start()
  }

  fun handleOnDestroy() {
    activity.unregisterReceiver(bluetoothReceiver)
  }

  fun appendText(text: String) {
    activity.runOnUiThread {
      Log.e("James", text)
      receivedMessage.text = receivedMessage.text.toString() + "\n" + text
    }
  }


  private val bluetoothReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
      intent?.let {
        if (it.action == BluetoothDevice.ACTION_FOUND) {
          val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
          val pairedDevice = devicesMap[device.address]
          pairedDevice?.let { device ->
            val foundDevice = devices.find {
              it.address == device.address
            }
            if (foundDevice != null) {
              arrayAdapter?.add(foundDevice.name + " " + foundDevice.address)
            } else {
              devices.add(device)
              arrayAdapter?.add(device.name + " " + device.address)
            }
          }
        }
      }
    }
  }
}

class BluetoothServerController(private val activity: BluetoothDemoActivity): Thread() {
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
        BluetoothServer(activity, socket).start()
      }
    }
  }
}

class BluetoothServer(private val activity: BluetoothDemoActivity, private val socket: BluetoothSocket): Thread() {
  private val inputStream = socket.inputStream
  private val outputStream = socket.outputStream

  override fun run() {
    try {
      val available = inputStream.available()
      val bytes = ByteArray(available)
      inputStream.read(bytes, 0, available)
      val text = String(bytes)
      activity.appendText(text)
    } catch (e: Exception)  {

    } finally {
      inputStream.close()
      outputStream.close()
      socket.close()
    }
  }
}

class BluetoothClient(device: BluetoothDevice): Thread() {
  private val socket = device.createRfcommSocketToServiceRecord(uuid)

  override fun run() {
    socket.connect()

    val outputStream = socket.outputStream
    val inputStream = socket.inputStream

    try {
      outputStream.write(message.toByteArray())
      outputStream.flush()
    } catch(e: Exception) {

    } finally {
      outputStream.close()
      inputStream.close()
      socket.close()
    }
  }
}

class SelectDeviceDialog(private val arrayAdapter: ArrayAdapter<String>) : DialogFragment() {
  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("Send message to")
    builder.setAdapter(arrayAdapter) {_, which: Int ->
      BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
      BluetoothClient(devices[which]).start()
    }
    return builder.create()
  }
}