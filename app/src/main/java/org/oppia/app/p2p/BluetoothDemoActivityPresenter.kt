package org.oppia.app.p2p

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.BluetoothDemoActivityBinding
import javax.inject.Inject

@ActivityScope
class BluetoothDemoActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
){
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<BluetoothDemoActivityBinding>(
      activity,
      R.layout.bluetooth_demo_activity
    )
  }
}