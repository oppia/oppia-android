package org.oppia.app.player.state.testing

import android.os.Bundle
import org.oppia.app.activity.InjectableAppCompatActivity
import javax.inject.Inject

/** Test Activity used for testing StateFragment */
class StateFragmentTestActivity : InjectableAppCompatActivity() {

  @Inject lateinit var stateFragmentTestActivityController: StateFragmentTestActivityPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    activityComponent.inject(this)
    stateFragmentTestActivityController.handleOnCreate()
  }
}
