package org.oppia.app.p2p

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class BluetoothDemoActivity : InjectableAppCompatActivity() {
  @Inject lateinit var bluetoothDemoActivityPresenter: BluetoothDemoActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    bluetoothDemoActivityPresenter.handleOnCreate()
  }
}