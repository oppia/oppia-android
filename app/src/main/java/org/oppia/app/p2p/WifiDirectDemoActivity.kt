package org.oppia.app.p2p

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class WifiDirectDemoActivity : InjectableAppCompatActivity() {
  @Inject lateinit var wifiDirectDemoActivityPresenter: WifiDirectDemoActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    wifiDirectDemoActivityPresenter.handleOnCreate()
  }
}