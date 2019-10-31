package org.oppia.app.home

import androidx.appcompat.app.AppCompatActivity
import org.oppia.app.R
import org.oppia.app.activity.ActivityScope
import javax.inject.Inject

/** The controller for [ContinuePlayingActivity]. */
@ActivityScope
class ContinuePlayingActivityPresenter @Inject constructor(private val activity: AppCompatActivity) {
  fun handleOnCreate() {
    activity.setContentView(R.layout.continue_playing_activity)
    if (getContinuePlayingFragment() == null) {
      activity.supportFragmentManager.beginTransaction().add(
        R.id.continue_playing_fragment_placeholder,
        ContinuePlayingFragment()
      ).commitNow()
    }
  }

  private fun getContinuePlayingFragment(): ContinuePlayingFragment? {
    return activity.supportFragmentManager.findFragmentById(R.id.continue_playing_fragment_placeholder) as ContinuePlayingFragment?
  }
}
