package org.oppia.app.p2p

import android.content.Intent
import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class NFCDemoActivity : InjectableAppCompatActivity(), NFCActivity {
  @Inject lateinit var nfcDemoActivityPresenter: NFCDemoActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    nfcDemoActivityPresenter.handleOnCreate()
  }

  override fun onResume() {
    super.onResume()
    nfcDemoActivityPresenter.handleOnResume()
  }

  override fun onPause() {
    super.onPause()
    nfcDemoActivityPresenter.handleOnPause()
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    nfcDemoActivityPresenter.handleOnNewIntent(intent)
  }

  override fun getOutgoingMessage(): String = nfcDemoActivityPresenter.handleGetOutgoingMessage()
  override fun signalResult(): Unit = nfcDemoActivityPresenter.handleSignalResult()
}