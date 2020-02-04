package org.oppia.app.p2p

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

class P2PDemoActivity : InjectableAppCompatActivity() {
  @Inject lateinit var p2PDemoActivityPresenter: P2PDemoActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    p2PDemoActivityPresenter.handleOnCreate()
  }
}