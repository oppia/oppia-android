package org.oppia.app.p2p

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.databinding.WifiDirectDemoActivityBinding
import javax.inject.Inject

@ActivityScope
class WifiDirectDemoActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
){
  fun handleOnCreate() {
    val binding = DataBindingUtil.setContentView<WifiDirectDemoActivityBinding>(
      activity,
      R.layout.wifi_direct_demo_activity
    )
  }
}