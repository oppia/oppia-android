package org.oppia.app.p2p

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.NfcDemoActivityBinding
import javax.inject.Inject
import kotlin.math.ln1p

@ActivityScope
class NFCDemoActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity,
  private val outgoingNfcManager: OutgoingNfcManager
) {
  private lateinit var outgoingMessage: TextView
  private lateinit var receivedMessage: TextView
  private var nfcAdapter: NfcAdapter? = null

  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<NfcDemoActivityBinding>(
      activity,
      R.layout.nfc_demo_activity
    )
    outgoingMessage = binding.outgoingMessage
    receivedMessage = binding.receivedMessage

    binding.setMessageButton.setOnClickListener {
      binding.outgoingMessage.text = binding.inputText.text
    }

    if (!isNfcSupported()) {
      Toast.makeText(activity, "Nfc is not supported on this device", Toast.LENGTH_SHORT).show()
      activity.finish()
    }
    if (!nfcAdapter!!.isEnabled) {
      Toast.makeText(activity, "NFC disabled on this device. Turn on to proceed", Toast.LENGTH_SHORT)
        .show()
    }

    nfcAdapter?.setOnNdefPushCompleteCallback(outgoingNfcManager, activity)
    nfcAdapter?.setNdefPushMessageCallback(outgoingNfcManager, activity)
  }

  fun handleOnResume() {
    enableForegroundDispatch()
    receiveMessageFromDevice(activity.intent)
  }

  fun handleOnPause() {
    disableForegroundDispatch()
  }

  fun handleOnNewIntent(intent: Intent?) {
    activity.intent = intent
    receiveMessageFromDevice(intent)
  }

  private fun isNfcSupported(): Boolean {
    nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
    return nfcAdapter != null
  }

  private fun receiveMessageFromDevice(intent: Intent?) {
    intent?.let {
      val action = it.action
      if (action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
        val parcelables = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        val inNdefMessage = parcelables[0] as NdefMessage
        val inNdefRecords = inNdefMessage.records
        val ndefRecord_0 = inNdefRecords[0]

        val inMessage = ndefRecord_0.payload.toString()
        receivedMessage.text = inMessage
      }
    }
  }

  private fun enableForegroundDispatch() {
    val intent = Intent(activity.applicationContext, activity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP

    val pendingIntent = PendingIntent.getActivity(activity.applicationContext, 0, intent, 0)

    val filters = arrayOfNulls<IntentFilter>(1)
    val techList = arrayOf<Array<String>>()

    filters[0] = IntentFilter()
    filters[0]?.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED)
    filters[0]?.addCategory(Intent.CATEGORY_DEFAULT)
    try {
      filters[0]?.addDataType(MIME_TEXT_PLAIN)
    } catch (ex: IntentFilter.MalformedMimeTypeException) {
      throw RuntimeException("Check your MIME type")
    }
    nfcAdapter?.enableForegroundDispatch(activity, pendingIntent, filters, techList)
  }

  private fun disableForegroundDispatch() {
    nfcAdapter?.disableForegroundDispatch(activity)
  }

  fun handleGetOutgoingMessage(): String = outgoingMessage.text.toString()

  fun handleSignalResult() {
    activity.runOnUiThread {
      Toast.makeText(
        activity,
        "Beaming Complete",
        Toast.LENGTH_SHORT
      ).show()
    }
  }
}