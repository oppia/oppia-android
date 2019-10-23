package org.oppia.app.player.state.testing

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import org.oppia.app.player.state.StateFragment
import javax.inject.Inject

/** The presenter for [StateFragmentTestActivity] */
@ActivityScope
class StateFragmentTestActivityPresenter @Inject constructor(
  private val activity: AppCompatActivity
) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.state_fragment_test_activity)
    if (getStateFragment() == null) {
      val stateFragment = StateFragment.newInstance("test")
      activity.supportFragmentManager.beginTransaction().add(
        R.id.state_fragment_placeholder,
        stateFragment
      ).commitNow()
    }
  }

  private fun getStateFragment(): StateFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.state_fragment_placeholder) as StateFragment?
  }
}
