package org.oppia.app.p2p

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.P2pDemoActivityBinding
import javax.inject.Inject

@ActivityScope
class P2PDemoActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
){
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<P2pDemoActivityBinding>(
      activity,
      R.layout.p2p_demo_activity
    )

    binding.nfcButton.setOnClickListener {
      activity.startActivity(Intent(activity, NFCDemoActivity::class.java))
    }

    binding.bluetoothButton.setOnClickListener {
      activity.startActivity(Intent(activity, BluetoothDemoActivity::class.java))
    }

    binding.wifiDirectButton.setOnClickListener {
      activity.startActivity(Intent(activity, WifiDirectDemoActivity::class.java))
    }
  }
}