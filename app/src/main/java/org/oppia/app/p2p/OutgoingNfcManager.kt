package org.oppia.app.p2p

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

const val MIME_TEXT_PLAIN = "text/plain"

@ActivityScope
class OutgoingNfcManager @Inject constructor(
  private val activity: AppCompatActivity
) : NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {
  override fun createNdefMessage(p0: NfcEvent?): NdefMessage {
    val outString = (activity as NFCActivity).getOutgoingMessage()
    val outBytes = outString.toByteArray()
    val outRecord = NdefRecord.createMime(MIME_TEXT_PLAIN, outBytes)
    return NdefMessage(outRecord)
  }

  override fun onNdefPushComplete(p0: NfcEvent?) {
    (activity as NFCActivity).signalResult()
  }
}

interface NFCActivity {
  fun getOutgoingMessage(): String
  fun signalResult()
}